import zeos
from message import Message
import circuitpython_base64 as base64
import displayio  # TODO REMOVE ME
import zlib
import os
import gc
import sys
import traceback


class StoreAndShowApp:
    def __init__(self, os: zeos.ZeBadgeOs):
        self.os = os
        self.index = 0
        self.files = _get_stored_files()

        self._subscription_ids = []

    def run(self):
        self._subscription_ids += [
            self.os.subscribe('system_button_up_released', lambda os, message: self._load_previous(os, message)),
            self.os.subscribe('system_button_down_released', lambda os, message: self._load_next(os, message)),
        ]

    def _load_next(self, os, message):
        self.files = _get_stored_files()
    def unrun(self):
        for subscription_id in self._subscription_ids:
            self.os.unsubscribe(subscription_id)
        self.index = (self.index + 1) % len(self.files)

        file = self.files[self.index]
        print(f'found file {file}.')
        _show_command(self.os, file, None)

    def _load_previous(self, os, message):
        self.files = _get_stored_files()
        length = len(self.files)
        self.index = (self.index + length - 1) % length

        file = self.files[self.index]
        print(f'found file {file}.')
        _show_command(self.os, file, None)


def _get_stored_files():
    return list(filter(lambda x: x.endswith('b64'), os.listdir('/')))




WIDTH = 296
HEIGHT = 128


def _decode_payload(payload):
    compressed_bytes = base64.b64decode(payload)
    binarized_bytes = zlib.decompress(compressed_bytes)
    bitmap = displayio.Bitmap(WIDTH, HEIGHT, 2)
    palette = displayio.Palette(2)
    palette[0] = 0x000000
    palette[1] = 0xFFFFFF
    for y in range(HEIGHT):
        print("B", end="")
        for x in range(WIDTH):
            # Pretend you understand this part
            byte_index = (y * (WIDTH // 8)) + (x // 8)
            bit_index = 7 - (x % 8)
            pixel_value = (binarized_bytes[byte_index] >> bit_index) & 1
            bitmap[x, y] = pixel_value
    return bitmap, palette


COMMANDS = {
    'show': _show_command,
    'store': _store_command,
    'preview': _preview_command,
    'list': _list_command,
    'delete': _delete_command,
}
