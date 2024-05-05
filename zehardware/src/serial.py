import usb_cdc
import re
import supervisor

from message import Message


def init(os):
    if usb_cdc.data:
        usb_cdc.data.timeout = 0.1
        os.tasks.append(_read_input)

        os.subscribe('SERIAL_RESPOND', _output_requested)


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
    os.messages.append(Message("SERIAL_RECEIVED", (command, meta, payload)))


def _parse_input(serial_input):
    if serial_input is None:
        return None

    parts = serial_input.split(":")
    if len(parts) != 3:
        readable_parts = " ".join(map(lambda p: trunc(p), parts))
        print(f"Invalid command: '{readable_parts}'")
        return None

    return [
        parts[0].strip(),
        parts[1].strip(),
        parts[2],
    ]


def log(os, news):
    os.messages.append(Message('info', news))


def trunc(message, max_length=10):
    # Middle of the word truncating
    if len(message) <= max_length:
        return message

    trunc_replacement = "…"

    # L = 2*pad + T
    # (L - T)/2 = pad
    pad = int(round((max_length - len(trunc_replacement)) / 2))
    left = message[:pad]
    right = message[-pad:]

    return left + trunc_replacement + right
