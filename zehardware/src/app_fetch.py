import zeos
import wifi
from message import Message


class FetchApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.subscription_ids = []

    def run(self):
        self.subscription_ids += [
            self.os.subscribe(wifi.MessageKey.SCAN, _scanned),
            self.os.subscribe(wifi.MessageKey.CONNECT_RESULT, _connected),
            self.os.subscribe(wifi.MessageKey.GET_RESULT, _getted),
        ]

        self.os.messages.append(Message(zeos.MessageKey.INFO, 'scanning wifi'))
        self.os.messages.append(Message(wifi.MessageKey.SCAN_RESULT))

    def unrun(self):
        for subscription in self.subscription_ids:
            self.os.unsubscribe(subscription)


def _scanned(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(
            Message(zeos.MessageKey.INFO, f"Found '{"', '".join(message.value)}' WiFis\nConnecting to wifi ..."))
        os.messages.append(Message(wifi.MessageKey.CONNECT, {
            'ssid': os.config.wifi.ssid,
            'pwd': os.config.wifi.pwd
        }))
    else:
        os.messages.append(Message(zeos.MessageKey.INFO, 'Could not scan wifi'))


def _connected(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(Message(zeos.MessageKey.INFO, 'GET sample data ...'))
        os.messages.append(Message(wifi.MessageKey.GET, {
            'ip': os.config.wifi.ip,
            'url': os.config.wifi.url,
            'host': os.config.wifi.host,
            'port': os.config.wifi.port,
        }))
    else:
        os.messages.append(Message(zeos.MessageKey.ERROR, 'Could not connect'))


def _getted(os: zeos.ZeBadgeOs, message):
    os.messages.append(Message(zeos.MessageKey.INFO, f'data received: {message.value.body}'))
