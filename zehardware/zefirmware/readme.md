You'll want to find two buttons on the RP2040 boards: reset and BOOTSEL/BOOT. The two buttons are the same size - small black buttons. Reset is typically labeled RESET or RST on the board. The boot button is labeled BOOTSEL or BOOT on the board.

To enter the bootloader on an RP2040 board, you must hold down the boot select button, and while continuing to hold it, press and release the reset button. Continue to hold the boot select button until the bootloader drive appears.

Drag'n'Drop adafruit-circuitpython-pimoroni_badger2040-en_US-8.0.5.uf2 to the newly appeared drive.

Copy code.py and all the libs and start muEditor.

(taken form https://learn.adafruit.com/welcome-to-circuitpython/installing-circuitpython)
