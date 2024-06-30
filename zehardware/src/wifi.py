import time

import board
import busio
import microcontroller

from message import Message


class MessageKey:
    SCAN = "SCAN"
    SCAN_RESULT = "SCAN_RESULT"
    CONNECT = "CONNECT"
    CONNECT_RESULT = "CONNECT_RESULT"
    GET = "GET"
    GET_RESULT = "GET_RESULT"
    POST = "POST"
    POST_RESULT = "POST_RESULT"


class Network:
    def __init__(self, ssid, mac, strength):
        self.ssid = ssid
        self.mac = mac
        self.strength = strength

    def __str__(self):
        return f"{self.ssid}@{self.mac}:{self.strength}"


class HttpResponse:
    def __init__(self, status, headers, body):
        self.status = status
        self.headers = headers
        self.body = body

    def __str__(self):
        return f"{self.status} {self.headers}\n{self.body}"


class ZeWifi:
    # A Wifi module connected through UART on GPIO4 and GPIO5.
    #
    # Please "deinit" the I2C on the badger if used for the first time.
    # The module attached needs to speak AT commands, and this class encapsulates those
    # into nice little methods.

    def __init__(self):
        self.uart = busio.UART(
            microcontroller.pin.GPIO4,
            microcontroller.pin.GPIO5,
            baudrate=115200,
            receiver_buffer_size=2048
        )

    def deinit(self):
        self.uart.deinit()

    def scan(self) -> map[str:list[Network]] | None:
        self.uart.write('AT+CWLAP\r\n')
        response = self.uart.read()

        if response and len(response) > 0:
            response = response.decode()
            result = {}
            for x in list(
                    map(
                        lambda found: Network(*[found.split(",")[i] for i in [1, 3, 2]]),
                        filter(
                            lambda wifi: '+CWLAP:' in wifi,
                            response.replace('\r', '').replace('"', '').splitlines()
                        )
                    )
            ):
                if x.ssid in result:
                    result[x.ssid].append(x)
                else:
                    result[x.ssid] = [x]
            return result
        else:
            return None

    def connect(self, ssid: str, pwd: str) -> bool:
        available_networks = self.scan()
        if not available_networks:
            available_networks = self.scan()

        if available_networks and ssid in available_networks:
            found = sorted(available_networks[ssid], key=lambda x: x.strength)[-1]

            self.uart.write(f'AT+CWJAP_CUR="{found.ssid}","{pwd}","{found.mac}"\r\n')
            response = self.uart.read()

            if len(response) <= 0 or "WIFI CONNECTED" not in response.decode():
                print(f"Not connected, couldn't get IP. Response was '{response}'.")
                return False
            else:
                return True
        else:
            print(f"Network '{ssid}' was not found. These are available: '{"' ".join(available_networks.keys())}'.")
            return False

    def _http_method(self, method: str, ip: str, url: str, host: str = "", port: int = 80,
                     body: str = "") -> HttpResponse | None:
        response = ""
        while len(response) == 0 or "STATUS:3" not in response.decode():
            # connect to ip
            self.uart.write(f'AT+CIPSTART="TCP","{ip}",{port}\r\n')
            response = self.uart.read()
            if len(response) <= 0 or "OK" not in response.decode():
                print(f"Couldn't connect: Response was '{response}'.")
                return None

            # check status
            self.uart.write('AT+CIPSTATUS\r\n')
            response = self.uart.read()

        # create http payload
        payload = (f"{method} {url} HTTP/1.1\r\n" +
                   f"Host: {ip}:{port}\r\n" +
                   f"User-Agent: ZeBadge/0.1337.0\r\n" +
                   f"Accept: */*\r\n")

        if len(body) > 0:
            payload += (f"Content-type: application/json\r\n" +
                        f"Content-Length: {len(body)}\r\n")

        payload += f"\r\n{body}"

        self.uart.write(f'AT+CIPSEND={len(payload)}\r\n')
        self.uart.read()

        self.uart.write(payload)
        response = self.uart.read()

        if len(response) >= 0:
            response = _parse_response(response.decode())
        else:
            response = None

        # closing
        self.uart.write("AT+CIPCLOSE\r\n")
        self.uart.read()

        return response

    def http_get(self, ip: str, url: str, host: str = "", port: int = 80) -> HttpResponse | None:
        return self._http_method("GET", ip, url, host, port)

    def http_post(self, ip: str, url: str, host: str = "", port: int = 80, body: str = "") -> HttpResponse | None:
        return self._http_method("POST", ip, url, host, port, body)


wifi = None


def init(os) -> bool:
    global wifi
    # disconnect the i2c, make it a UART
    # 👀
    board.I2C().deinit()
    time.sleep(0.4)

    wifi = ZeWifi()

    scan = wifi.scan()
    if not scan:
        scan = wifi.scan()

    if scan:
        os.subscribe(MessageKey.SCAN, lambda _, message: os.messages.append(
            Message(MessageKey.SCAN_RESULT, wifi.scan())))

        os.subscribe(MessageKey.CONNECT, lambda _, message: os.messages.append(
            Message(
                MessageKey.CONNECT_RESULT,
                wifi.connect(
                    message.value['ssid'],
                    message.value['pwd']
                )
            )
        ))

        os.subscribe(MessageKey.GET, lambda _, message: os.messages.append(
            Message(
                MessageKey.GET_RESULT,
                wifi.http_get(
                    message.value['ip'],
                    message.value['url'],
                    message.value['host'],
                    message.value['port']
                )
            )
        ))

        os.subscribe(MessageKey.POST, lambda _, message: os.messages.append(
            Message(
                MessageKey.POST_RESULT,
                wifi.http_post(
                    message.value['ip'],
                    message.value['url'],
                    message.value['host'],
                    message.value['port'],
                    message.value['body'],
                )
            )
        ))
        return True
    else:
        return False


def _parse_response(response: str) -> HttpResponse | None:
    parts = response.replace('\r\n', '\n').splitlines()[5:]

    status_code = parts.pop(0)
    if status_code.startswith('+IPD'):
        _, code = status_code.split(':')
        _, status_code, _ = code.split(' ')
        status_code = int(status_code)
    else:
        status_code = 444

    headers = {}
    for index, header in enumerate(parts):
        try:
            key, value = header.split(":", 1)
        except ValueError:
            break

        key = key.strip()
        value = value.strip()

        # nope, keep iterating through headers
        headers[key] = value.strip()

    body = parts[-1]
    response = HttpResponse(status_code, headers, body)
    return response
