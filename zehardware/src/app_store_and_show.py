import displayio

import ui
import zeos
from message import Message
from ui import MessageKey as UIKeys


class StoreAndShowApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.index = 0
        self.files = os.get_stored_files()

        self._subscription_ids = []

    def run(self):
        self._subscription_ids += [
            self.os.subscribe(
                zeos.MessageKey.BUTTON_CHANGED,
                lambda os, message: self._buttons_changed(message.value)
            ),
        ]

        self._load_last_shown()

    def unrun(self):
        for subscription_id in self._subscription_ids:
            self.os.unsubscribe(subscription_id)

    def _buttons_changed(self, changed):
        if 'up' in changed and not changed['up']:
            self._load_previous()
        if 'down' in changed and not changed['down']:
            self._load_next()

    def _load_last_shown(self):
        try:
            last_badge_stored = open('.last_badge').read()
            if 'b64' in last_badge_stored:
                self._show_file(last_badge_stored)
            elif 'bmp' in last_badge_stored:
                odb = displayio.OnDiskBitmap(last_badge_stored)
                self.os.messages.append(
                    Message(
                        UIKeys.SHOW_BITMAP,
                        (odb, odb.pixel_shader)
                    )
                )
        except Exception:
            odb = displayio.OnDiskBitmap("first.bmp")
            self.os.messages.append(
                Message(
                    UIKeys.SHOW_BITMAP,
                    (odb, odb.pixel_shader)
                )
            )

    def _load_next(self):
        self.files = self.os.get_stored_files()
        self.index = (self.index + 1) % len(self.files)

        file = self.files[self.index]
        self._show_file(file)

    def _load_previous(self, ):
        self.files = self.os.get_stored_files()
        length = len(self.files)
        self.index = (self.index + length - 1) % length

        file = self.files[self.index]
        self._show_file(file)

    def _show_file(self, filename):
        try:
            open('.last_badge', 'w').write(filename)
        except OSError:
            print("OS Error (developer mode?)")
        self.os.messages.append(Message(ui.MessageKey.SHOW_FILE, filename))
