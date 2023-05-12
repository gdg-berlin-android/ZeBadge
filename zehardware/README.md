# ZeBadge Hardware

## How do I set up the badge?

If you got your device from us, it's already set up with Circuit Python, some Adafruit libs, and operates as described here.

#### Flashing the device

In normal conditions, you only have to do this once (we did it already).

1. Press and hold the `BOOT` hardware button on the back on the device
1. While holding the button, plug the device into your PC
1. You should see a new drive being attached to your PC
1. Simply copy a `.uf2` binary of your choice to the root folder `/` on the device. Once complete, the device will automatically restart and install the firmware

The images we used are in `/zehardware/zefirmware`:
1. First we copy the `flash_nuke` file and wait for the reboot
1. Then we copy the Adafruit-flavored Circuit Python image and wait for the reboot

Device should now appear as a new drive on your PC, containing some Python and configuration files.

**Alternatively**, you can use [Thonny](https://thonny.org) to set up everything using their user-friendly app interface. If you get stuck or need more help, see the [Pimoroni website](https://pimoroni.com/badger2040).

#### Writing code

You might want to reconfigure or rewrite everything to your liking. The device normally runs a micro-version of Python, with most default Python APIs built-in. 
Files are in the root directory on your device:

- `boot.py` is executed only once
- `boot_out.txt` contains any message that arise from booting the devices
- `code.py` is the main dish – your code lives there
- Additional libraries required to run your code need to be in the `/lib` directory on the device. Our repository contains **all** Adafruit libs, but you should not copy the stuff you don't need to the device
- There's a `settings.toml` file as well, empty for now but could be edited to change device configuration during boot. See the official Circuit Python docs to learn more about it

The main libraries our project needs in the on-device `/lib` directory are:
  - adafruit_imageload/
  - circuitpython_base64.py
  - adafruit_binascii.mpy

## What can I do with the device?

_This document assumes you didn't edit the device code after receiving it from us._

The device is set up to receive and respond to a simple set of commands through its main serial port (better known as the USB), as well as react to commands given by the on-device hardware buttons.

Some of the commands run on the device and don't produce visible results, while others are set up to do graphics rendering and communication with other on-device hardware (like the LED light).

Keep reading to find out more about the commands.

#### Command structure and format

Commands are sent through the serial port to the device and must be known to the device. Adding new commands requires a code change on the device as well. Each command **must** have 3 parts, in this order:

1. **Name** — A simple name of the command, already known to the hardware device, like `blink`
1. **Metadata** — Additional data for the command. Not necessarily required data for the command to run, but helpful information for debugging and user-experience purposes. An example when this could be useful is some kind of image metadata, for when the main payload is an image
1. **Payload** — The main data, potentially required for the command to run

Some more rules around the 3 parts of a command:
- Only the **name** is mandatory, the rest are optional. Optional parts can be sent as empty strings
- The delimiter used to separate the parts is a simple **colon** (`:`)
- The final command must be Base64-encoded
- Commands can also be sent in debug mode, if device is in debug mode as well. To do this, you don't have to Base64-encode the command, but instead add a prefix `debug:` in front

Here are some examples:

```console
Ymxpbms6Og==
```
👆  Decodes to `blink::`, there's no metadata or payload.

```console
debug:exit::
```
👆  Plain debug command, also no metadata or payload.

```console
cHJldmlldzpkYXRhOmltYWdlL3BuZztiYXNlNjQ6aVZCT1J3MEtHZ29BQUFBTlNVaEVVZ0FBQUFnQUFBQUlBUU1BQUFEK3dTeklBQUFBQmxCTVZFWC8vLysvdjcralEzWTVBQUFBRGtsRVFWUUkxMlA0QUlYOEVBZ0FMZ0FEL2FOcGJ0RUFBQUFBU1VWT1JLNUNZSUk=
```
👆  Decodes to `preview:ZGF0YTppbWFnZS9wbmc7YmFzZTY0:iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII`. If we split the command by colon, we get the following:
- `preview` is the command name, 
- `ZGF0YTppbWFnZS9wbmc7YmFzZTY0` is the metadata, and
- `iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII` is the payload

In this case, metadata and payload were additionaly encoded to Base64 because their original raw format contained colons. Here's what they decode to:
- decoded metadata: `data:image/png;base64`
- decoded payload as a PNG: &nbsp; ![Payload Preview](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII)

Each command defines what it needs to run, which is reflected by the on-device code.

## Available commands

#### Blink

As simple as it sounds — starts or stops the LED light blinking.

| Command section | Content |
| --------------- | ------- |
| Name            | `blink` |
| Metadata        | N/A     |
| Payload         | N/A     |

Debug example:
```console
debug:blink::
```

#### Reload

Reloads the latest hardware code changes.

| Command section | Content  |
| --------------- | -------- |
| Name            | `reload` |
| Metadata        | N/A      |
| Payload         | N/A      |

Debug example:
```console
debug:reload::
```

#### Exit

Stops the main loop and exits to an interactive Python interpreter (REPL).

| Command section | Content |
| --------------- | ------- |
| Name            | `exit`  |
| Metadata        | N/A     |
| Payload         | N/A     |

Debug example:
```console
debug:exit::
```
