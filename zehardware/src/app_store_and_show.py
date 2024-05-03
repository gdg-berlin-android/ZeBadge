import zeos
from message import Message


class StoreAndShowApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.index = 0
        self.files = os.get_stored_files()

        self._subscription_ids = []

    def run(self):
        self._subscription_ids += [
            self.os.subscribe('system_button_up_released', lambda os, message: self._load_previous(os, message)),
            self.os.subscribe('system_button_down_released', lambda os, message: self._load_next(os, message)),
        ]

    def unrun(self):
        for subscription_id in self._subscription_ids:
            self.os.unsubscribe(subscription_id)

    def _load_next(self, os, _):
        self.files = os.get_stored_files()
        self.index = (self.index + 1) % len(self.files)

        file = self.files[self.index]
        self._show_file(file)

    def _load_previous(self, os, _):
        self.files = os.get_stored_files()
        length = len(self.files)
        self.index = (self.index + length - 1) % length

        file = self.files[self.index]
        self._show_file(file)

    def _show_file(self, filename):
        self.os.messages += Message("UI_SHOW_FILE", filename)


WIDTH = 296
HEIGHT = 128
