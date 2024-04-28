import usb_cdc
import re
import supervisor

from message import Message
from util import exception_to_readable

MAX_OUTPUT_LEN = 10


def init(os):
    if usb_cdc.data:
        usb_cdc.data.timeout = 0.1
        os.tasks.append(_read_input)

        os.subscribe('SERIAL_OUTPUT_REQUESTED', _output_requested)


def _output_requested(os, message):
    usb_cdc.data.write(message.value)


def _read_input(os):
    if not supervisor.runtime.usb_connected:
        return

    if not supervisor.runtime.serial_connected:
        return

    read_bytes = usb_cdc.data.read()
    cleaned = re.sub(r'\s', " ", read_bytes.decode()).strip()
    del read_bytes

    if len(cleaned) <= 0:
        return

    parsed = _parse_input(cleaned)
    del cleaned

    if not parsed:
        return

    command, meta, payload = parsed
    del parsed
    os.messages.append(Message("info", f"Payload with {len(payload)} bytes received."))

    if command in COMMANDS:
        usb_cdc.data.write(command)
        COMMANDS[command](os, meta, payload)
    else:
        os.messages.append(Message("SERIAL_INPUT_RECEIVED", (command, meta, payload)))


def _parse_input(input):
    if input is None:
        return None

    parts = input.split(":")
    if len(parts) != 3:
        print(f"Invalid command: '{input}'")
        print(" - Did you forget to add colons?")
        return None

    return [
        parts[0].strip(),
        parts[1].strip(),
        parts[2],
    ]


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


def _update_blinking():
    return


def _reload_command(os, meta, payload):
    os.messages.append(Message("reload", None))


def _exit_command(os, meta, payload):
    os.messages.append(Message("exit", None))


def _terminal_command(os, meta, payload):
    os.messages.append(Message("UI_SHOW_TERMINAL", None))


def _refresh_command(os, meta, payload):
    os.messages.append(Message("UI_REFRESH", None))


COMMANDS = {
    "reload": _reload_command,
    "exit": _exit_command,
    "terminal": _terminal_command,
    "refresh": _refresh_command,
}
