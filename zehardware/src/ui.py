import board
import displayio
import terminalio
import time
import vectorio
import util
from adafruit_display_text import label


def init(os):
    os.subscribe("UI_SHOW_BITMAP", _show_image_handler)
    os.subscribe("UI_SHOW_TERMINAL", _show_terminal_handler)
    os.subscribe("UI_REFRESH", _refresh_handler)


def _refresh_display_save():
    time.sleep(board.DISPLAY.time_to_refresh + 0.3)

    try:
        board.DISPLAY.refresh()
    except:
        print("x")


def _refresh_handler(os, message):
    _refresh_display_save()


def _show_image_handler(os, message):
    bitmap, palette = message.value
    del message

    tile_grid = displayio.TileGrid(bitmap, pixel_shader=palette)

    group = displayio.Group()
    group.append(tile_grid)
    board.DISPLAY.root_group = group
    board.DISPLAY.refresh()


def _show_terminal_handler(os, message):
    board.DISPLAY.root_group = displayio.CIRCUITPYTHON_TERMINAL
    _refresh_display_save()


def draw_intro():
    # splash = displayio.Group()
    #
    # splash_picture = displayio.OnDiskBitmap('/splash.bmp')
    # splash_tiles = displayio.TileGrid(splash_picture, pixel_shader=splash_picture.pixel_shader)
    # splash.append(splash_tiles)
    # display.root_group = splash

    _refresh_display_save()
