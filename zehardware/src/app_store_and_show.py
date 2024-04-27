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
        self.files = _updated_file_list()

    def run(self):
        self.os.subscribe('SERIAL_INPUT_RECEIVED', _input_received_handler)
        self.os.subscribe('up_pressed', self._load_previous)
        self.os.subscribe('down_pressed', self._load_next)

    def _load_next(self, os, topic, message):
        self.files = _updated_file_list()
        self.index = (self.index + 1) % len(self.files)

        file = self.files[self.index]
        _show_command(self.os, file, None)

    def _load_previous(self, os, topic, message):
        self.files = _updated_file_list()
        length = len(self.files)
        self.index = (self.index + length - 1) % length

        file = self.files[self.index]
        _show_command(self.os, file, None)


def _updated_file_list():
    return list(filter(lambda x: x.endswith('b64'), os.listdir('/')))


def _input_received_handler(os, message):
    command, meta, payload = message.value

    if command in COMMANDS:
        COMMANDS[command](os, meta, payload)
    else:
        os.messages.append(Message('error', 'Nope, not understood.'))


def _show_command(os, file_name, _):
    with open(f"{file_name}.b64", "rb") as file:
        payload = file.read()
        _preview_command(os, "", payload)


def _store_command(os, file_name, payload):
    with open(f"{file_name}.b64", "wb") as file:
        file.write(payload)


def _preview_command(os, meta, payload):
    import gc
    print(gc.mem_free())
    gc.collect()
    print(gc.mem_free())

    bitmap, palette = _decode_payload(payload)
    del payload

    os.messages.append(Message('info', 'previewing image'))
    os.messages.append(Message("UI_SHOW_BITMAP", (bitmap, palette)))


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
}
