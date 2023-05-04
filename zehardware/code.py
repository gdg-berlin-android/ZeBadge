import time
import board
import terminalio
import displayio
import digitalio
import vectorio
from adafruit_bitmap_font import bitmap_font
from adafruit_display_text import label

display = board.DISPLAY
enable = board.ENABLE_DIO

act = digitalio.DigitalInOut(board.USER_LED)
act.direction = digitalio.Direction.OUTPUT
act.value = True

derp = digitalio.DigitalInOut(board.SW_A)
derp.direction = digitalio.Direction.INPUT
derp.pull = digitalio.Pull.DOWN

contact_font = bitmap_font.load_font("jetbrains.bdf")
name_font = bitmap_font.load_font("righteous.bdf")
color = 0x000000

palette = displayio.Palette(1)
palette[0] = 0xFFFFFF

def derp_it(derping=False):
    rectangle = vectorio.Rectangle(pixel_shader=palette, width=display.width + 1, height=display.height, x=0, y=0)

    logo = displayio.OnDiskBitmap("voltron-head.bmp")
    logo_grid = displayio.TileGrid(logo, pixel_shader=logo.pixel_shader)
    logo_grid.x = display.width - 100

    derp_bmp = displayio.OnDiskBitmap("voltron-head-derp.bmp")
    derp_grid = displayio.TileGrid(derp_bmp, pixel_shader=derp_bmp.pixel_shader)
    derp_grid.x = display.width - 100

    first_name_label = label.Label(name_font, text="Mario", color=color, scale=2)
    last_name_label = label.Label(name_font, text="Bodemann", color=color, scale=2)
    contact_label = label.Label(contact_font, text="mario.bodemann@gmail.com", color=color, scale=1)

    first_name_label.x = 0
    first_name_label.y = 15
    last_name_label.x = 0
    last_name_label.y = 67
    contact_label.x = 0
    contact_label.y = 110
    advert_label.x = 280
    advert_label.y = 0

    group = displayio.Group()
    group.append(rectangle)
    group.append(first_name_label)
    group.append(last_name_label)
    group.append(contact_label)
    if derping:
        group.append(derp_grid)
    else:
        group.append(logo_grid)

    display.show(group)
    display.refresh()

derp_it()

enable.value = False
derped = False


while True:
    if derp.value:
        derped = not derped
        derp_it(derped)
        time.sleep(4)
