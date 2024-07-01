import json

import displayio
import terminalio
from adafruit_display_text import label

import ui
import wifi
import zeos
from message import Message
from ui import MessageKey as UIKeys


class ZePassApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.subscription_ids = []
        self.method = ""

    def run(self):
        self.subscription_ids += [
            self.os.subscribe(wifi.MessageKey.CONNECT_RESULT, self._connected),
            self.os.subscribe(wifi.MessageKey.GET_RESULT, self._response_received),
            self.os.subscribe(wifi.MessageKey.POST_RESULT, self._response_received),

            self.os.subscribe(
                zeos.MessageKey.BUTTON_CHANGED,
                lambda os, message: self._buttons_changed(message.value)
            ),
        ]

    def unrun(self):
        for subscription in self.subscription_ids:
            self.os.unsubscribe(subscription)

    def _buttons_changed(self, changed):
        if 'up' in changed and not changed['up']:
            self._fetch_all_posts()

        if 'down' in changed and not changed['down']:
            self._create_new_post()

    def _fetch_all_posts(self):
        config = {
            'ssid': self.os.config['wifi.ssid'],
            'pwd': self.os.config['wifi.pwd']
        }
        self.method = "GET"
        self.os.messages.append(Message(zeos.MessageKey.INFO, f'Connecting to wifi {config} '))
        self.os.messages.append(
            Message(
                wifi.MessageKey.CONNECT,
                config
            )
        )

    def _connected(self, os: zeos.ZeBadgeOs, message):
        if message.value:
            config = {
                'ip': os.config['wifi.ip'],
                'url': os.config['wifi.url'],
                'host': os.config['wifi.host'],
                'port': os.config['wifi.port'],
            }

            if self.method == "GET":
                os.messages.append(Message(zeos.MessageKey.INFO, f'Connected, GETing posts {config}.'))
                os.messages.append(Message(wifi.MessageKey.GET, config))
            elif self.method == "POST":
                config['body'] = os.config['user.uuid']
                os.messages.append(Message(zeos.MessageKey.INFO, f'Connected, POSTing {config}.'))
                os.messages.append(Message(wifi.MessageKey.POST, config))
            else:
                os.messages.append(
                    Message(
                        zeos.MessageKey.ERROR,
                        f"Zepass method {self.method} not understood."
                    )
                )
                self.method = ''

        else:
            os.messages.append(Message(zeos.MessageKey.ERROR, 'Could not connect'))

    def _create_new_post(self):
        self.method = "POST"

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

    def _response_received(self, os: zeos.ZeBadgeOs, message):
        if self.method == "GET":
            self._update_all_posts(
                message.value.body
            )

        elif self.method == "POST":
            self.method = None
            self._fetch_all_posts()

    def _update_all_posts(self, raw_posts):
        group = displayio.Group()
        font = terminalio.FONT

        try:
            posts = json.loads(raw_posts)
        except ValueError as e:
            print(f'Could not parse response: {raw_posts}')
            return

        for index, post in enumerate(posts):
            # TODO: ADD FANCY USER LOGO HERE
            post_area = label.Label(
                font,
                text=post['message'],
                background_color=0x000000,
                color=0xFFFFFF,
            )
            if index % 2 == 0:
                post_area.x = 40
            else:
                post_area.x = int(296 / 2)

            post_area.y = 16 + index * 16

            group.append(post_area)
            if 'profileB64' in post and post['profileB64']:
                profile = post['profileB64']
                bitmap, palette = ui.decode_serialized_bitmap(profile, 32, 32)
                profile_grid = displayio.TileGrid(bitmap, pixel_shader=palette)
                if index % 2 == 0:
                    profile_grid.x = 0
                else:
                    profile_grid.x = 296 - 32

                profile_grid.y = index * 16
                group.append(profile_grid)
            else:
                print(f'no profile for message {index}.')

        self.os.messages.append(
            Message(
                UIKeys.SHOW_GROUP,
                group
            )
        )
