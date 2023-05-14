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
from digitalio import DigitalInOut, Direction


# Configuration

READ_TIMEOUT   =    2 # seconds
LOOP_CYCLE     =  0.3 # seconds
KEEP_ALIVE     =    5 # seconds
PREVIEW_TIME   =    3 # seconds
DEBUG          = True
MAX_OUTPUT_LEN = 10

def log(string):
    if DEBUG: print(string)


## Run board setup
led = DigitalInOut(board.USER_LED)
led.direction = Direction.OUTPUT
display = board.DISPLAY

log("-----")
log("Running in serial mode.")


# Middle of the word truncating (GPT says so)
def trunc(long):
    if len(long) <= MAX_OUTPUT_LEN:
        return long    
    trunc_replacement = "..."
    left_pad = len(trunc_replacement) + 1
    right_pad = -len(trunc_replacement)
    return long[:left_pad] + "..." + long[right_pad:]


# Keep alive pinger
iteration = 0
def log_keep_alive():
    global iteration
    keep_alive_cycle = int(KEEP_ALIVE / LOOP_CYCLE)
    if iteration % keep_alive_cycle == 0:
        time_string = "--:"
        time_string += "{:0>2}".format(time.localtime().tm_min)
        time_string += ":{:0>2}".format(time.localtime().tm_sec)
        log("Awaiting commands… (%s)" % time_string)


# Blink it when doing some work
should_blink_led = False
def update_blinking():
    global led
    if should_blink_led:
        led.value = not led.value
    else:
        led.value = False


# Read a single command from the serial interface
def read_command():
    buffer = ""
    if not supervisor.runtime.usb_connected:
        log("No USB connection, skipping read")
        return None
    if not supervisor.runtime.serial_connected:
        log("No serial connection, skipping read")
        return None
    while supervisor.runtime.serial_bytes_available:
        buffer += sys.stdin.readline()
    cleaned = re.sub(r'\s', " ", buffer).strip()
    return cleaned if len(cleaned) > 0 else None


# Base64<command:metadata:payload> -> [command, metadata, payload]
# For debug, you can skip Base64 and put "debug:" in front
COMMAND_NONE = [None, None, None]
def parse_command(base64_string):
    if base64_string == None:
        return COMMAND_NONE
    if DEBUG and base64_string.startswith("debug:"):
        debug_command = base64_string.replace("debug:", "")
        parts = debug_command.split(":")
        if len(parts) != 3:
            log("Invalid debug command: '%s'" % debug_command)
            log("  - Did you forget to add colons?")
            return COMMAND_NONE
        return [
            parts[0].strip(),
            parts[1].strip(),
            parts[2].strip(),
        ]
    try:
        base64_bytes = base64_string.encode("utf-8")
        bytes_plain = base64.decodebytes(base64_bytes)
        plain_string = str(bytes_plain, "utf-8")
        parts = plain_string.split(":")
        if len(parts) != 3:
            raise Exception("Invalid command format: '%s'" % plain_string)
        return plain_string.split(":")
    except Exception as e:
        log(f"Failed to decode '{base64_string}'.\n%s" % format_e(e))
        return COMMAND_NONE


# Handle commands in format Base64<command:metadata:payload>
allowed_commands = ["blink", "reload", "exit", "preview"]
def handle_commands():
    global allowed_commands
    command_raw = read_command()
    command_name, metadata, payload = parse_command(command_raw)
    if command_name == None:
        return
    elif command_name not in allowed_commands:
        log("Unknown command '%s'" % command_name)
        return
    elif command_name == "blink":
        handle_command_blink()
    elif command_name == "reload":
        handle_command_reload()
    elif command_name == "exit":
        handle_command_exit()
    elif command_name == "preview":
        handle_command_preview(metadata, payload)
    else:
        log("Command not implemented yet!")


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
    time.sleep(1)
    supervisor.reload()


# For the exiting command
# debug:exit::
def handle_command_exit():
    sys.exit()


# For the previewing command
# debug:preview::Qk1WAAAAAAAAAD4AAAAoAAAABgAAAPr///8BAAEAAAAAABgAAAAAAAAAAAAAAAIAAAAAAAAAAAAAAP///wAAAAAABAAAAAwAAAAcAAAAPAAAAHwAAAA=
def handle_command_preview(base64_metadata, base64_payload):
    log("Previewing image…")
    command_name = "preview"
    metadata = ""
    try:
        base64_metadata_bytes = base64_metadata.encode("utf-8")
        metadata_bytes_plain = base64.decodebytes(base64_metadata_bytes)
        metadata = str(metadata_bytes_plain, "utf-8")
        log("Decoded metadata: '%s'" % metadata)
        store_b64_as_bitmap(base64_payload, command_name)
        render_stored_bitmap(command_name)        
    except Exception as e:
        log(f"Preview failed for: '{base64_metadata}':'{trunc(base64_payload)}'.\n%s" % format_e(e))


# Rendering a stored bitmap onto the screen
def render_stored_bitmap(name):
    global display
    bitmap = displayio.OnDiskBitmap(file_path_for(name))
    tile_grid = displayio.TileGrid(bitmap, pixel_shader=bitmap.pixel_shader)
    group = displayio.Group()
    group.append(tile_grid)
    display.show(group)
    time.sleep(PREVIEW_TIME)
    

# Rendering an in-memory bitmap
def render_bitmap_bytes(raw_bytes):
    global display
    raw_bytes = fetch_bitmap_bytes(command_name)
    raw_stream = io.BytesIO(raw_bytes)
    bitmap, palette = adafruit_imageload.load(
        raw_stream,
        bitmap=displayio.Bitmap,
        palette=displayio.Palette,
    )
    tile_grid = displayio.TileGrid(bitmap, pixel_shader=palette)
    group = displayio.Group()
    group.append(tile_grid)
    display.show(group)
    time.sleep(PREVIEW_TIME)
    

# Storing a Base64 string as a bitmap image
def store_b64_as_bitmap(base64_str, name):
    file_path = file_path_for(name)
    with open(file_path, "wb") as file:
        raw_bytes = base64.b64decode(base64_str)
        file.write(raw_bytes)


# Reading a bitmap image as 