import zeos
from message import Message


class FetchApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.started = False

    def run(self):
        if self.started:
            self.os.messages.append(Message('error', 'This app is already running.'))
            return

        self.os.subscribe('WIFI_SCAN_RESULT', self._scanned)
        self.os.subscribe('WIFI_CONNECT_RESULT', self._connected)
        self.os.subscribe('WIFI_GET_RESULT', self._getted)

        self.started = True
        self.os.messages.append(Message('info', 'scanning wifi'))
        self.os.messages.append(Message('WIFI_SCAN', {}))


def _scanned(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(Message('info', f"Found '{"', '".join(message.value)}' WiFis\nConnecting to wifi ..."))
        os.messages.append(Message('WIFI_CONNECT', {
            'ssid': os.config.wifi.ssid,
            'pwd': os.config.wifi.pwd
        }))
    else:
        os.messages.append(Message('error', 'Could not scan wifi'))


def _connected(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(Message('info', 'GET sample data ...'))
        os.messages.append(Message('WIFI_GET', {
            'ip': os.config.wifi.ip,
            'url': os.config.wifi.url,
            'host': os.config.wifi.host,
            'port': os.config.wifi.port,
        }))
    else:
        os.messages.append(Message('error', 'Could not connect'))

    def _getted(self, os: zeos.ZeBadgeOs, message):
        os.messages.append(Message('info', f'data received: {message.value.body}'))

        self.os.unsubscribe('WIFI_SCAN_RESULT')
        self.os.unsubscribe('WIFI_CONNECT_RESULT')
        self.os.unsubscribe('WIFI_GET_RESULT')

        self.started = False
