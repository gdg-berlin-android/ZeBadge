import zeos
from message import Message

from wifi import MessageKey as WiFiKey
from zeos import MessageKey as OSKey


class FetchApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.subscription_ids = []

    def run(self):
        self.subscription_ids += [
            self.os.subscribe(WiFiKey.SCAN, _scanned),
            self.os.subscribe(WiFiKey.CONNECT_RESULT, _connected),
            self.os.subscribe(WiFiKey.GET_RESULT, _getted),
        ]

        self.os.messages.append(Message(OSKey.INFO, 'scanning wifi'))
        self.os.messages.append(Message(WiFiKey.SCAN_RESULT))

    def unrun(self):
        for subscription in self.subscription_ids:
            self.os.unsubscribe(subscription)


def _scanned(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(Message(OSKey.INFO, f"Found '{"', '".join(message.value)}' WiFis\nConnecting to wifi ..."))
        os.messages.append(Message(WiFiKey.CONNECT, {
            'ssid': os.config.wifi.ssid,
            'pwd': os.config.wifi.pwd
        }))
    else:
        os.messages.append(Message(OSKey.INFO, 'Could not scan wifi'))


def _connected(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(Message(OSKey.INFO, 'GET sample data ...'))
        os.messages.append(Message(WiFiKey.GET, {
            'ip': os.config.wifi.ip,
            'url': os.config.wifi.url,
            'host': os.config.wifi.host,
            'port': os.config.wifi.port,
        }))
    else:
        os.messages.append(Message(OSKey.ERROR, 'Could not connect'))


def _getted(os: zeos.ZeBadgeOs, message):
    os.messages.append(Message(OSKey.INFO, f'data received: {message.value.body}'))
