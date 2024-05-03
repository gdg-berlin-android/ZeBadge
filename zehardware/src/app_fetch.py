import zeos
from message import Message


class FetchApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.subscription_ids = []

    def run(self):
        self.subscription_ids += [
            self.os.subscribe('WIFI_SCAN_RESULT', _scanned),
            self.os.subscribe('WIFI_CONNECT_RESULT', _connected),
            self.os.subscribe('WIFI_GET_RESULT', _getted),
        ]

        self.os.messages.append(Message('info', 'scanning wifi'))
        self.os.messages.append(Message('WIFI_SCAN'))

    def unrun(self):
        for subscription in self.subscription_ids:
            self.os.unsubscribe(subscription)


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


def _getted(os: zeos.ZeBadgeOs, message):
    os.messages.append(Message('info', f'data received: {message.value.body}'))
