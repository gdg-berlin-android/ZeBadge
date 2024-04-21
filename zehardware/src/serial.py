import re
import sys
import supervisor
from message import Message
import usb_cdc

MAX_OUTPUT_LEN = 10


def read_input(os):

    if not supervisor.runtime.usb_connected:
        log(os, "No USB connection, skipping read")
        return None
    if not supervisor.runtime.serial_connected:
        log(os, "No serial connection, skipping read")
        return None

    total_bytes = bytearray()
    # while (usb_cdc.data.in_waiting):
    #     bytes = usb_cdc.data.read(40)
    #     total_bytes += bytes

    cleaned = re.sub(r'\s', " ", total_bytes.decode()).strip()

    if len(cleaned) > 0:
        log(os, f"Cleaned input (stdin) = '{trunc(cleaned)}'")

    if len(cleaned) > 0:
        os.messages.append(Message('serial_input', cleaned))


def log(os, news):
    os.messages.append(Message('info', news))


def trunc(message):
    # Middle of the word truncating
    if len(message) <= MAX_OUTPUT_LEN:
        return message
    trunc_replacement = "…"
    left_pad = len(trunc_replacement) + 1
    right_pad = -len(trunc_replacement)
    return message[:left_pad] + trunc_replacement + message[right_pad:]
