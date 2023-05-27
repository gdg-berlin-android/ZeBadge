#!/bin/python
#
# To properly format commands, you must end them with CR+LF.
# The easiest way is apparently using Pico COM, like below.
# (that will map CR to CR+LF on each echo)
#
# Linux: picocom /dev/ttyACM1 --omap crcrlf --echo
# Mac: picocom /dev/cu.usbmodem2103 --omap crcrlf --echo
# Windows: Change OS (:
#
# Thonny's interactive mode (REPL) should be the best option.

import adafruit_imageload
import bitmaptools
import board
import circuitpython_base64 as base64
import displayio
import io
import os
import re
import storage
import supervisor
import sys
import time
import traceback
import usb_cdc
import zlib
from digitalio import DigitalInOut, Direction, Pull

# Configuration

DEBUG = True
MAX_OUTPUT_LEN = 10
READ_TIMEOUT = 0.1  # seconds
LOOP_CYCLE = 0.1  # seconds
KEEP_ALIVE = 5  # seconds
REFRESH_RATE = 3  # seconds
DEBOUNCE_TIME = 2  # seconds

COMMANDS = [
    "reload",
    "exit",

    "blink",
    "terminal",
    "preview",
    "refresh",

    "store-a",
    "store-b",
    "store-c",
    "store-down",
    "store-up",

    "show-a",
    "show-b",
    "show-c",
    "show-down",
    "show-up",
]
COMMAND_NONE = [None, None, None]


# Debug logging
def log(string):
    if DEBUG:
        with open("log.txt", "a") as file:
            file.write(string)
            file.write("\n")
        print(string)


# Dumping object properties and functions
def dump(obj):
    for attr in dir(obj):
        print(f"obj.{attr} = {getattr(obj, attr)}")


# Middle of the word truncating
def trunc(message):
    if len(message) <= MAX_OUTPUT_LEN:
        return message
    trunc_replacement = "…"
    left_pad = len(trunc_replacement) + 1
    right_pad = -len(trunc_replacement)
    return message[:left_pad] + trunc_replacement + message[right_pad:]


# Formatting an exception
def format_e(exception):
    message = str(exception)
    trace = traceback.format_exception(exception)
    result = "Reason: "
    result += message if len(message) > 0 else "🤷"
    result += "\n"
    result += "\n  ".join(trace)
    return result


# Setting up hardware buttons
def prep_button(identifier):
    button = DigitalInOut(identifier)
    button.direction = Direction.INPUT
    button.pull = Pull.DOWN
    return button


### Set up hardware ###

led = DigitalInOut(board.USER_LED)
led.direction = Direction.OUTPUT
display = board.DISPLAY
buttons = {
    "a": prep_button(board.SW_A),
    "b": prep_button(board.SW_B),
    "c": prep_button(board.SW_C),
    "down": prep_button(board.SW_DOWN),
    "up": prep_button(board.SW_UP),
}
if usb_cdc.data is None:
    usb_cdc.data.timeout = READ_TIMEOUT

log("-----")
log("Running in serial mode.")

# Keep alive pinger
iteration = 0


def log_keep_alive():
    global iteration
    keep_alive_cycle = int(KEEP_ALIVE / LOOP_CYCLE)
    if iteration % keep_alive_cycle == 0:
        now = time.localtime()
        time_string = f"{now.tm_hour:02d}:{now.tm_min:02d}:{now.tm_sec:02d}"
        log(f"Awaiting commands… ({time_string})")


def refresh_if_needed():
    while board.DISPLAY.busy:
        print("X", end="")
        time.sleep(0.10)
    print("")

    try:
        display.refresh()
    except Exception as e:
        log(f"Failed to refresh.\n{format_e(e)}.")


# Blink it when doing some work
led_on = False


def update_blinking():
    global led, led_on
    led.value = led_on
    led_on = not led_on


# Handle button clicks
last_click_time = time.time()  # seconds
last_button = None


def handle_buttons():
    global buttons, last_click_time, last_button
    if time.time() - last_click_time <= DEBOUNCE_TIME:
        return
    if buttons["a"].value and last_button != "a":
        handle_show_command("a")
        last_click_time = time.time()
    elif buttons["b"].value and last_button != "b":
        handle_show_command("b")
        last_click_time = time.time()
    elif buttons["c"].value and last_button != "c":
        handle_show_command("c")
        last_click_time = time.time()
    elif buttons["down"].value and last_button != "down":
        handle_show_command("down")
        last_click_time = time.time()
    elif buttons["up"].value and last_button != "up":
        handle_show_command("up")
        last_click_time = time.time()


# Read a single command from the stdin interface
def read_command_stdin():
    buffer = ""
    if not supervisor.runtime.usb_connected:
        log("No USB connection, skipping read")
        return None
    if not supervisor.runtime.serial_connected:
        log("No serial connection, skipping read")
        return None
    buffer = sys.stdin.readline()
    cleaned = re.sub(r'\s', " ", buffer).strip()
    if len(cleaned) > 0 and DEBUG:
        log(f"Cleaned input (stdin) = '{trunc(cleaned)}'")
    return cleaned if len(cleaned) > 0 else None


# Read a single command from the CDC interface
def read_command_cdc():
    if usb_cdc.data is None:
        log("No data connection, skipping read")
        return None

    bytes_read = usb_cdc.data.readline()
    if bytes_read:
        line = bytes_read.decode()
        log(f"input (CDC) = '{trunc(line)}'")
        cleaned = re.sub(r'\s', " ", line).strip()
        if len(cleaned) > 0 and DEBUG:
            log(f"Cleaned input (CDC) = '{trunc(cleaned)}'")
        return cleaned if len(cleaned) > 0 else None
    else:
        return None


# Base64<command:metadata:payload> -> [command, metadata, payload]
# For debug, you can skip Base64 and put "debug:" in front
def parse_command(base64_string):
    if base64_string is None:
        return COMMAND_NONE

    debug_command = base64_string.replace("debug:", "")
    parts = debug_command.split(":")
    if len(parts) != 3:
        log(f"Invalid debug command: '{debug_command}'")
        log(" - Did you forget to add colons?")
        return COMMAND_NONE
    return [
        parts[0].strip(),
        parts[1].strip(),
        parts[2].strip(),
    ]


# Handle commands in format Base64<command:metadata:payload>
def handle_commands():
    command_raw = read_command_cdc()
    if command_raw is None:
        print("Command is empty, ignore")
    command_name, metadata, payload = parse_command(command_raw)
    if command_name is None:
        return
    elif command_name not in COMMANDS:
        log(f"Unknown command '{command_name}'.")
        return
    elif command_name == "blink":
        handle_command_blink()
    elif command_name == "reload":
        handle_command_reload()
    elif command_name == "exit":
        handle_command_exit()
    elif command_name == "preview":
        handle_command_preview(payload)
    elif command_name == "terminal":
        handle_terminal_command()
    elif command_name == "refresh":
        handle_refresh_command()
    elif command_name.startswith("show"):
        page = command_name.split("-")[1]
        handle_show_command(page)
    elif command_name.startswith("store"):
        page = command_name.split("-")[1]
        handle_store_command(page, metadata, payload)
    else:
        log("Command not implemented yet!")


# For the refresh command
# debug:refresh::
def handle_refresh_command():
    log("Scheduling screen refresh…")
    refresh_if_needed()


# For the terminal command
# debug:terminal::
def handle_terminal_command():
    log("Showing terminal…")
    display.root_group = displayio.CIRCUITPYTHON_TERMINAL


# For the blinking command
# debug:blink::
def handle_command_blink():
    global should_blink_led
    log("Changing blink status…")
    should_blink_led = not should_blink_led


# For the reloading command
# debug:reload::
def handle_command_reload():
    log("Reloading…")
    time.sleep(0.5)
    supervisor.reload()


# For the exiting command
# debug:exit::
def handle_command_exit():
    log("Exiting…")
    time.sleep(0.5)
    sys.exit()


# For the previewing command
# debug:preview::eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==
def handle_command_preview(base64_payload):
    log("Previewing image…")
    try:
        bitmap, palette = decode_payload(base64_payload)
        show_bitmap(bitmap, palette)
    except Exception as e:
        log(f"Preview failed for:'{trunc(base64_payload)}'.\n{format_e(e)}.")


# For the storing command
# debug:store-a:bmFtZT1NaWtl:eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==
def handle_store_command(name, metadata, payload):
    log("Storing image…")
    try:
        write_b64_as_file(metadata, f"{name}.metadata.base64")
        write_b64_as_file(payload, f"{name}.bin.gz.base64")
    except Exception as e:
        log(f"Storing failed for: '{trunc(metadata)}':'{trunc(payload)}'.\n{format_e(e)}.")


# For the showing command
# debug:show-a::
def handle_show_command(name):
    log(f"Showing page '{name}'…")
    file_name_meta = f"{name}.metadata.base64"
    file_name_payload = f"{name}.bin.gz.base64"
    meta_b64 = ""
    payload_b64 = ""
    try:
        meta_b64 = read_file_as_b64(file_name_meta)
        payload_b64 = read_file_as_b64(file_name_payload)
        bitmap, palette = decode_payload(payload_b64)
        show_bitmap(bitmap, palette)
    except Exception as e:
        log(f"Showing failed for: '{trunc(meta_b64)}':'{trunc(payload_b64)}'.\n{format_e(e)}.")


# Showing a bitmap with a palette
def show_bitmap(bitmap, palette):
    global display
    tile_grid = displayio.TileGrid(bitmap, pixel_shader=palette)
    group = displayio.Group()
    group.append(tile_grid)
    display.root_group = group
    refresh_if_needed()


# Storing a Base64 string as a file
def write_b64_as_file(base64_str, file_name):
    with open(file_name, "wb") as file:
        raw_bytes = base64.b64decode(base64_str)
        file.write(raw_bytes)


# Reading a bytes file as a Base64 string
def read_file_as_b64(file_name):
    raw_bytes = []
    with open(file_name, "rb") as file:
        raw_bytes = file.read()
    b64_bytes = base64.b64encode(raw_bytes)
    b64_string = b64_bytes.decode("utf-8")
    return b64_string


# Decoding a Base64ed, compressed a binarized bitmap
def decode_payload(payload):
    global display
    log(f"pload : '{trunc(payload)}'.")
    compressed_bytes = base64.b64decode(payload)
    binarized_bytes = zlib.decompress(compressed_bytes)
    bitmap = displayio.Bitmap(display.width, display.height, 2)
    palette = displayio.Palette(2)
    palette[0] = 0x000000
    palette[1] = 0xFFFFFF
    for y in range(display.height):
        print(".", end="")
        update_blinking()
        for x in range(display.width):
            # Pretend you understand this part
            byte_index = (y * (display.width // 8)) + (x // 8)
            bit_index = 7 - (x % 8)
            pixel_value = (binarized_bytes[byte_index] >> bit_index) & 1
            bitmap[x, y] = pixel_value
    print("done")
    return bitmap, palette


### The Main Loop ###

while True:
    log_keep_alive()
    update_blinking()
    handle_commands()
    handle_buttons()
    iteration += 1
    time.sleep(LOOP_CYCLE)
