import board
import usb_hid
from message import Message
from adafruit_hid.keyboard import Keyboard
from adafruit_hid.keyboard_layout_us import KeyboardLayout
from adafruit_hid.keycode import Keycode

from message import Message
import zeos


class MessageKey:
    KEY_PRESSED = "key_pressed"


SPECIAL_CARD_KEYS = {
    0x08: Keycode.BACKSPACE,
    0x09: Keycode.TAB,
    0x0D: Keycode.ENTER,
    0x1B: Keycode.ESCAPE,
    0xB4: Keycode.LEFT_ARROW,
    0xB5: Keycode.UP_ARROW,
    0xB6: Keycode.DOWN_ARROW,
    0xB7: Keycode.RIGHT_ARROW,
    0x81: Keycode.F1,
    0x82: Keycode.F2,
    0x83: Keycode.F3,
    0x84: Keycode.F4,
    0x85: Keycode.F5,
    0x86: Keycode.F6,
    0x87: Keycode.F7,
    0x88: Keycode.F8,
    0x89: Keycode.F9,
    0x8A: Keycode.F10,
}

i2c = board.I2C()
keyboard = Keyboard(usb_hid.devices)
layout = KeyboardLayout(keyboard)


def init(os):
    os.tasks.append(update_keyboard)
    os.subscribe(MessageKey.KEY_PRESSED, on_key_pressed)


def update_keyboard(os):
    while not i2c.try_lock():
        continue

    buffer = bytearray(1)
    i2c.readfrom_into(95, buffer)
    i2c.unlock()

    if buffer[0]:
        key = buffer[0]
        os.messages.append(Message(MessageKey.KEY_PRESSED, key))


def on_key_pressed(os, message):
    key = message.value
    try:
        layout.write(chr(key))
    except (ValueError, IndexError):
        if key in SPECIAL_CARD_KEYS:
            keys = SPECIAL_CARD_KEYS[key]

            try:
                layout.write(keys)
            except TypeError:
                keyboard.send(keys)
        else:
            os.messages.append(Message(zeos.MessageKey.ERROR, f"Couldn't find {key} key."))
