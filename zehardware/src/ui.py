import board
import displayio
import terminalio
import vectorio
from adafruit_display_text import label

display = board.DISPLAY

palette = displayio.Palette(color_count=2)
palette[0] = 0x000000
palette[1] = 0xffffff

main_group = displayio.Group()


def draw_intro():
    # splash = displayio.Group()
    #
    # splash_picture = displayio.OnDiskBitmap('/splash.bmp')
    # splash_tiles = displayio.TileGrid(splash_picture, pixel_shader=splash_picture.pixel_shader)
    # splash.append(splash_tiles)
    # display.root_group = splash

    try:
        display.refresh()
    except RuntimeError:
        pass


def init(os):
    os.tasks.append(update_ui)

    top_back = vectorio.Rectangle(
        pixel_shader=palette,
        x=0, y=0,
        width=display.width, height=round(display.height / 4),
        color_index=1)

    top_text = label.Label(
        font=terminalio.FONT,
        text="Hello, my name is",
        color=palette[0]
    )
    top_text.x = 30
    top_text.y = 10

    mid = label.Label(
        font=terminalio.FONT,
        text="Mario Bodemann",
        color=palette[1]
    )
    mid.x = 50
    mid.y = 100

    bottom_back = vectorio.Rectangle(
        pixel_shader=palette,
        x=0,
        y=round(3 * display.height / 4),
        width=display.width,
        height=round(display.height / 4),
        color_index=1
    )

    bottom_text = label.Label(
        font=terminalio.FONT,
        text="All hail to ZeBadge",
        color=palette[0]
    )

    main_group.append(top_back)
    main_group.append(top_text)
    main_group.append(mid)
    main_group.append(bottom_back)
    main_group.append(bottom_text)


def update_ui(os):
    if display.time_to_refresh <= 0:
        display.refresh()
