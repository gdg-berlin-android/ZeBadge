# ZeBadge Hardware

## What is it?

It's a simple circuitboard hardware badge with an integrated E-Ink display. See the root [README](../README.md) for more info.

⚠️  Looking for commands? Jump to [the Commands List](#the-command-list) directly.

## How do I set up the badge?

If you got your device from us, it's already set up with Circuit Python, some Adafruit libs, and operates as described here.

### Flashing the device yourself - File System Approach

Under normal conditions, you only have to flash the OS once (and we did it already).

  1. Press and hold the `BOOT` hardware button on the back on the device
  1. While holding the button, plug the device into your PC
  1. You should see a new drive attached to your PC
  1. Simply copy a `.uf2` binary of your choice to the root folder `/` of the device. Once complete, the device will automatically restart and install the firmware

The images we used are in [ZeFirmware](zefirmware) directory.
  1. First we copy the `flash_nuke` file and wait for the reboot
  1. Then we copy the Adafruit-flavored Circuit Python image and wait for the reboot

Your device should now appear as a new drive on your PC, containing some Python and configuration files.

### Flashing the device yourself - Using Thonny

Alternatively to the approach above, you can use [Thonny](https://thonny.org) to set up everything using their user-friendly app interface.

If you get stuck or need more help, see the [Pimoroni website](https://pimoroni.com/badger2040).

#### Flashing other/custom images

If you don't want to flash a base OS image we use and describe in the section above, you can take a look at other examples of custom images: [rpi/pico-examples](https://github.com/pimoroni/pimoroni-pico/tree/main/examples). You can also take a look at the source [here](https://github.com/pimoroni/pimoroni-pico/blob/main/examples/badger2040/badger2040_image.cpp).

To find some examples of Pico images you could flash, see the compiled binaries [here](/zehardware/experiments/c++/flash) and the experimental images [here](/zehardware/experiments/c++/images). For some of our testing purposes, we used the `badger2040_image` binary.

##### How do I build custom images?

  1. Install the [C++ SDK](https://github.com/pimoroni/pimoroni-pico/blob/main/setting-up-the-pico-sdk.md) from Pimoroni
  1. Build using C, following that guide
  1. After the project is built, under the `build` directory we will have all the examples (their flash files)
  1. Reminder: for testing purposes, flash your device with "badger2040_image"

One important side-note, the build (`cmake` & `make`) took around 30 minutes total.

### Writing and running code

You might want to reconfigure or rewrite the badge's code to your liking. The device normally runs a micro-version of Python, with most (_but not all!_) default Python APIs built-in. 
With our base OS, all files are in the root directory on your device:

  - `boot.py` is executed only once, on device boot
  - `boot_out.txt` contains any message that arise from booting the devices
  - `code.py` is the main dish – your code lives there
  - Additional libraries required to run your code need to be in the `/lib` directory on the device. Our repository contains **all** Adafruit libs, but you should not copy the stuff you don't need to the device
  - There's a `settings.toml` file as well, empty for now but could be edited to change device configuration during boot. See the official Circuit Python docs to learn more about it

The devices we give out are already configured with the required libraries and do not need additional libraries to run our code.

## What can I do with the device?

_This section assumes you didn't edit the device code after receiving it from us._

The device is set up to receive and respond to a simple set of commands through its main serial port (better known as the USB), as well as react to commands given by the on-device hardware buttons.

Some of the commands run on the device and don't produce visible results, while others are set up to do graphics rendering and communication with other on-device hardware (like the LED light).

Keep reading to find out more about the commands.

### Command structure and format

Commands are sent through the serial port to the device. The send commands must be known to the device, obviously, so adding new commands requires a code change on the device as well.

Each command **must** have all 3 components, in this order:

|                  **Name**                  	|                                                           **Metadata**                                                           	|                       **Payload**                      	|
|:------------------------------------------:	|:--------------------------------------------------------------------------------------------------------------------------------:	|:------------------------------------------------------:	|
| A simple name of the command, e.g. `blink` 	| Additional data for the command, not required for the command to run; e.g. image metadata (if the command is related to imaging) 	| The main command data, required for the command to run 	|

Rules:
  - Only the **command name** is mandatory, the rest of the components are optional
  - Optional components still must be sent, at least as empty strings
  - The delimiter used to separate the components is a simple **colon** (`:`)
  - A final, production-ready command must be Base64-encoded
  - A dev mode command for when the device is in debug mode doesn't have to be Base64-encoded, but must add a `debug:` prefix

### Examples

All commands available today are listed **at the bottom of this page**. This section is only trying to explain the general idea:

(1) Sending an encoded blink command (decoded: `blink::`). There's no metadata or payload:
```console
Ymxpbms6Og==
```

(2) Sending an exit command in dev-mode, also without any metadata or payload:
```console
debug:exit::
```

(3) Sending an image preview command, with metadata and payload:
```console
cHJldmlldzpkYXRhOmltYWdlL3BuZztiYXNlNjQ6aVZCT1J3MEtHZ29BQUFBTlNVaEVVZ0FBQUFnQUFBQUlBUU1BQUFEK3dTeklBQUFBQmxCTVZFWC8vLysvdjcralEzWTVBQUFBRGtsRVFWUUkxMlA0QUlYOEVBZ0FMZ0FEL2FOcGJ0RUFBQUFBU1VWT1JLNUNZSUk=
```
This command decodes to `preview:ZGF0YTppbWFnZS9wbmc7YmFzZTY0:iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII`.
You may have noticed that metadata and payload here are again in Base64 format (even after the initial decoding). This is true, and the reason is that image metadata and payload in raw format could contain colons. It's up to each command to define the format of its payload and metadata.

If we split that last command by a colon (`:`), we get the following:
  - name: `preview`,
  - metadata: `ZGF0YTppbWFnZS9wbmc7YmFzZTY0`, decodes further to `data:image/png;base64` (but can be any string),
  - payload: `iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII`, decodes to bytes of a bitmap image: ![.](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAIAQMAAAD+wSzIAAAABlBMVEX///+/v7+jQ3Y5AAAADklEQVQI12P4AIX8EAgALgAD/aNpbtEAAAAASUVORK5CYII)

In general, each command must define what data it needs to run. This is reflected by the on-device code.

## The Command List

⚠️ &nbsp; Important consideration: each command, button press or screen refresh has some debouncing to preserve a steady command stream.

This means that you may experience delays of up to 5 seconds on your commands – this is by design and due to hardware limitations of our badge.

### Debugging

#### 🖥️ &nbsp; Reload

Reloads the latest badge code changes.

| Command section | Content  |
| --------------- | -------- |
| Name            | `reload` |
| Metadata        | N/A      |
| Payload         | N/A      |

Debug example:
```console
debug:reload::
```

#### 🖥️ &nbsp; Exit

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

### Interactions

#### 🖥️ &nbsp; Blink

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

#### 🖥️ &nbsp; Terminal

Shows the Terminal on the E-Ink display.

| Command section | Content    |
| --------------- | ---------- |
| Name            | `terminal` |
| Metadata        | N/A        |
| Payload         | N/A        |

Debug example:
```console
debug:terminal::
```

#### 🖥️ &nbsp; Preview image

Previews an image on the E-Ink display. The image is not stored anywhere.

| Command section | Content                                         |
| --------------- | ----------------------------------------------- |
| Name            | `preview`                                       |
| Metadata        | N/A                                             |
| Payload         | Base64-encoded, binary image format (see below) |

The binary format here is critically important, as it applies to all other image-related commands as well.  
Due to hardware limitations, we have to minimize the amount of data we send to the badge, but also minimize the amount of memory we allocate for each bitmap render.

The general idea of how we transmit images is this:
  1. Make the image black and white first (you can do that using online tools)
  1. Binarize the image: turn the bitmap pixel matrix into a simple binary array:
      - Create a new byte array; this will contain the result
      - Looping through rows and columns of the bitmap, inspect all image pixels one by one
      - If the pixel is white (`b255`), push a binary `1` into the result array
      - If the pixel is black (`b0`), push a binary `0` into the result array
  1. The binary array is grouped into bytes by the default behavior of arrays in most languages
  1. Compress that byte array using the `zlib` compression lib
  1. Encode the compressed byte array into a string
  1. Base64-encode the string for use as `payload` of the command

To assist you in preparing the images, there are some sample scripts in [this Scripts directory](experiments/badge-milos/Scripts) and some sample images in [this Outputs directory](experiments/badge-milos/Output). Our [companion Android app](../zeapp) already has the necessary code for producing images in the correct format.

Debug example:
```console
debug:preview::eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==
```

#### 🖥️ &nbsp; Refresh

Requests a refresh to happen on the next refresh cycle.

| Command section | Content   |
| --------------- | --------- |
| Name            | `refresh` |
| Metadata        | N/A       |
| Payload         | N/A       |

Debug example:
```console
debug:refresh::
```

### Persistent images

⚠️ &nbsp; The device has 5 hardware interaction buttons (excluding reset and boot buttons):
  - `A`, `B` and `C` – placed horizontally on the bottom of the frame
  - `UP` and `DOWN` – placed vertically on the right side of the frame

For each of those buttons, the device has an associated image (with metadata) stored on the device, in the root directory. The images are therefore named:
  - a.bin.gz
  - b.bin.gz
  - c.bin.gz
  - down.bin.gz
  - up.bin.gz

The image rendered on the E-Ink screen can easily changed by simple presses on those buttons. It is possible to simulate button presses using the "show" command (shown below).

#### 🖥️ &nbsp; Show a persisted image

Renders one of the already persisted images onto the E-Ink screen and requests a refresh on the next refresh cycle.

| Command section | Content                       |
| --------------- | ----------------------------- |
| Name            | `show-*` (see examples below) |
| Metadata        | N/A                           |
| Payload         | N/A                           |

Debug examples:
```console
debug:show-a::

debug:show-b::

debug:show-c::

debug:show-down::

debug:show-up::
```

#### 🖥️ &nbsp; Store/persist a new image

Stores a new image onto the device, tied to one of the buttons. This does not request a change to display the just stored image.

| Command section | Content                                                |
| --------------- | ------------------------------------------------------ |
| Name            | `store-*` (see examples below)                         |
| Metadata        | Any Base64-encoded string (unused for now)             |
| Payload         | Binarized image (see the "Preview" command for format) |


Debug examples:
```console
debug:store-a:bmFtZT1NaWtl:eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==

debug:store-b:bmFtZT1NaWtl:eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==

debug:store-c:bmFtZT1NaWtl:eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==

debug:store-down:bmFtZT1NaWtl:eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==

debug:store-up:bmFtZT1NaWtl:eJy12F9MG3UcAPDvtef16q5wdTquKbPXQpgPaFq6MCbRnWwhI2Ga+GCM+OcGhGCCpIsJLBmO60IaSbpQ5GUVl+3RF5NFzeBl5gBlI06HPjHZYiNkmsgiaCAlAvXau/vdUXr87sVfSO9+5ZPf99v7+/v+AOy0wRyu7QCBNbmc5JKwsYhMmY2MYpU2UANnA3Hh0t+7TGkDY4EaTBkBI5ZGsrHLAFUaOXftWyDKtE9IFojeFdoC7TowvAXizZ2wHcRaIMHcoS2QbO4wFkgyd8jSiNjVc/zfyKHSHjU1QkGcnN9R+yCa0BM1nUVIG4M3Ic9XdQj5UhJV5qGgMVjLQC3Tx+nI//BTWUfPvCl5/H4/HAjW10Hf0UPVOqrvX0kh1CL5vKOjwASjPIRCXJeO+m9NdOro0Byw7OhhoKuClRDqZS/qaO6LxdM6euoMsN5RL9CRqjJoF1lBR8nFnl90xH4HnNebAjoackCIZ0VWQ+3Nj79EiIJC4lxFVR2E6sM1nIoIR3isVtJRMyiHoAye9eVzqhzg9JHYS2/c4TUEZOnT4pDh1Ot6OOXuK4mczwPxNEJ7m4omuqFawCBqvkbt7YdcGfIOHoGPxyJqcNwGyv0+NY9FCWqqFYcIyZ/AhiOFkabC5bzvSNyt2cL9sR+Cmi7PWgyHiIPDI2ENBYrfN6sqCkMLtaKHI4NkkG5ihwofvPKtdj3VCO7mzgwmXJQFZxR3CI7OQTyNQ0d6eDjFYlBj/cf57PdH9X9/i0fHfn1F7e2HDn5Eqg9YA5F7UMXXO5mikcqIYkRu/Tt5mt+FGFemCDnbBAcr6SiQK4QWi9FblVI8hUFwM7e0JOqIg9Lo8p/vUzJCc1L55ACIgXi2d2mgXNaR+zrjQjn5stLhFQbE41PJ/nM/5t/vKgrFwgEGobRE31VQbIbsiCY4XkfwKjuIjpPfrSAKRHFGCkUTDMqJdMbXEfJxEpcfSTy72hFxlktoJNb1GbpUfFfVcKI40BGhnBmEgPneGCmnIFJBMhOKUiAaKFSFEOeAQriwzLRHnQYSAKoNpLyw75ZB5vhMsiOSqERI0t5d6mnZUtDg7S3lYHZEqXKUk6wFVVG2MDchSfXxgA6B7DChvU397yol4NGmJ4xHDz1JPNp4N4NHf7XF8OjxtXk8uvDBAh59+N4fePTS4g088oxnEbJ6ZgL12jZC7upPWkx/XlrQkOPBOj4c4f8Gj6BlQsIjZt0Gcrwj4BG02EFPhm2gUMoGUhsOVdtBppH0ebxQhAgTIgrpKcWDoN+QJRAaCYXWw/2MUEU4lcm8IC7ffLu54dLt+LA7i9AcQoFsevNG4+bWo53Wi7P35DQzidDKdR1xyXEfGxlOzk6fnz21IKzRYYQ2jNnh5TF3JuImE03np1uvwFlaROgCmmd6h8boWPAAk5junu5eEM4xsoEeyfpIQ+M+LuhOJqZbZxsW5J+uSAi9vJ1C4dJr/0SGtxInmwca7l1Nbxq/7sR9Ho2UEk8G6U6pqYkDp5v+4XNjpLZJ5dO599wRnDHPD+ysQMlCgsgaFUOgv6s0yp8cIqOhE+PKJ2VRJqn3N5TnNiXLMgkeaGhjW7IuuPrVjevajmhdujWqmVNnFiXrIrBOLmycy70L1uVkJV/YOI6NPKf8XAvk0bbx9WXrEld7YShlSp/rvlWx7FQTB0cyTRwByqLK/02bBXXliC3LpYAXjd2YJdq9qGBrecLWQoetJRNbiy+2lnFsLQjZav8BwW4yIQ==
```

