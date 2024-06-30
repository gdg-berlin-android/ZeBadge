import wifi
import zeos
from message import Message


class FetchApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.subscription_ids = []

    def run(self):
        self.subscription_ids += [
            self.os.subscribe(wifi.MessageKey.CONNECT_RESULT, _connected),
            self.os.subscribe(wifi.MessageKey.GET_RESULT, _getted),
            self.os.subscribe(
                zeos.MessageKey.BUTTON_CHANGED,
                lambda os, message: self._buttons_changed(message.value)
            ),
        ]

    def _buttons_changed(self, changed):
        if 'up' in changed and not changed['up']:
            config = {
                'ssid': self.os.config['wifi.ssid'],
                'pwd': self.os.config['wifi.pwd']
            }
            self.os.messages.append(Message(zeos.MessageKey.INFO, f'Trying to connect: {config}'))
            self.os.messages.append(
                Message(
                    wifi.MessageKey.CONNECT,
                    config
                )
            )

    def unrun(self):
        for subscription in self.subscription_ids:
            self.os.unsubscribe(subscription)


def _connected(os: zeos.ZeBadgeOs, message):
    if message.value:
        os.messages.append(Message(zeos.MessageKey.INFO, 'Connected, GETing sample data ...'))
        os.messages.append(Message(wifi.MessageKey.GET, {
            'ip': os.config['wifi.ip'],
            'url': os.config['wifi.url'],
            'host': os.config['wifi.host'],
            'port': os.config['wifi.port'],
        }))
    else:
        os.messages.append(Message(zeos.MessageKey.ERROR, 'Could not connect'))


def _getted(os: zeos.ZeBadgeOs, message):
    os.messages.append(Message(zeos.MessageKey.INFO, f'data received: {message.value}'))
