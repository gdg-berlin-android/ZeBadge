#!/usr/bin/env python3
#
# FLASH ME IF YOU CAN!!
#
import os
import shutil
import subprocess
import time

import requests

OPEN_AI_TOKEN = os.getenv("OPEN_AI_TOKEN")
SERVER_TOKEN = os.getenv("ZESERVER_AUTH_TOKEN")
BASE_URL = "https://zebadge.app/"

BASE_BADGE_PATH = "/Volumes"


def hsv(h, s, v):
    """
    For h (0..360), s (0..1), v(0..1) create a r(0..1) g(0..1) b(0..1) tuple.
    """
    r = 0
    g = 0
    b = 0

    c = v * s
    x = c * (1.0 - abs((((h / 60.0) % 2.0) - 1.0)))
    m = v - c

    if 0 <= h < 60:
        r = c
        g = x
        b = 0
    elif 60 <= h < 120:
        r = x
        g = c
        b = 0
    elif 120 <= h < 180:
        r = 0
        g = c
        b = x
    elif 180 <= h < 240:
        r = 0
        g = x
        b = c
    elif 240 <= h < 300:
        r = x
        g = 0
        b = c
    elif 300 <= h < 360:
        r = c
        g = 0
        b = x

    return r + m, g + m, b + m


def rgb_to_termcolor(rgb):
    """
    Convert the given rgb (given from 0 to 1) to a terminal color from 16 to 216
    """
    r, g, b = rgb
    return 16 + int(r * 5) * 36 + int(g * 5) * 6 + int(b * 5)


def colorize(text):
    result = ""
    for (index, char) in enumerate(text):
        code = rgb_to_termcolor(hsv(index * 360.0 / len(text), 0.3, 1.0))
        result += f"\033[38;5;{code}m{char}"
    result += f"\033[m"
    return result


def request_new_user():
    assert SERVER_TOKEN, f"SERVER_AUTH not set: '{SERVER_TOKEN}'."
    print(f"{colorize('creating new user')}")

    users_response = requests.get(
        url=f"{BASE_URL}/api/user",
        headers={
            'Content-Type': 'application/json',
            'ZeAuth': SERVER_TOKEN
        },
    )

    if not users_response.ok:
        print(users_response)
        return
    users = users_response.json()

    print(f"Found {len(users)} users. Adding another one.")

    new_user = requests.post(
        url=f"{BASE_URL}/api/user",
        headers={
            'Content-Type': 'application/json',
            'ZeAuth': SERVER_TOKEN
        },
        json={}
    )

    if new_user.ok:
        user = new_user.json()
        print(f".. user '{colorize(user['name'])}' created.")
        return user
    else:
        print(f".. couldn't create user: '{colorize(new_user.json())}'.")

        return None


def find_mount_point(name):
    mount = subprocess.run(["mount"], capture_output=True, check=True)
    if mount.returncode != 0:
        print(
            f'"mount" returned error code {mount.returncode}.\n\nCheck outputs\nstd:{mount.stdout}\nerr:{mount.stderr}\n')
        return None

    output = mount.stdout.decode() + mount.stderr.decode()
    if name in output:
        return list(map(lambda y: y.split()[2], filter(lambda x: name in x, output.splitlines())))[0]
    else:
        return None


def find_base_badge_path():
    rpi = find_mount_point('RPI')
    if rpi:
        return rpi

    cirpy = find_mount_point('CIRCUIT')
    if cirpy:
        return cirpy

    return None


def nuke():
    nuke_ware = list(filter(lambda x: 'nuke' in x, os.listdir("./")))

    if not nuke_ware:
        print(colorize("No nuke firmware found!"))
        return False

    nuke_ware = nuke_ware[0]

    path = find_mount_point('RPI')
    if not path:
        print(
            f"Please put badge in flash mode.\n"
            f"{colorize('restart')} ZeBadge and "
            f"hold {colorize('boot / usr')} button."
        )

        while not find_mount_point('RPI'):
            print('.', flush=True, end='')
            time.sleep(0.1)
        print()
        path = find_mount_point('RPI')

    print(colorize('nuking'))

    time.sleep(0.2)
    shutil.copy(nuke_ware, path)
    time.sleep(0.3)

    while not find_mount_point('RPI'):
        print('.', flush=True, end='')
        time.sleep(0.1)
    print()


def flash():
    zepython = list(filter(lambda x: 'zepython' in x, os.listdir("./")))

    if not zepython:
        print(colorize("No zepython firmware found!"))
        return False

    zepython = zepython[0]

    path = find_mount_point('RPI')
    if not path:
        print(f"Please put badge in flash mode.\n{colorize('restart')} and hold {colorize('boot / usr')} button.")

        while not find_mount_point('RPI'):
            print('.', flush=True, end='')
            time.sleep(0.1)
        print()

        path = find_mount_point('RPI')

    print(colorize('flashing'))

    complete = False

    def wait_on_exception():
        print('x', flush=True, end='')
        time.sleep(1)

    while not complete:
        try:
            shutil.copy(zepython, path)
            complete = True
        except PermissionError:
            wait_on_exception()
        except OSError:
            wait_on_exception()

    while not find_mount_point('CIRC'):
        print('.', flush=True, end='')
        time.sleep(0.1)

    print()


def copy_code():
    circuit = find_mount_point('CIRC')
    while not circuit:
        print('.', flush=True, end='')
        time.sleep(0.1)
        circuit = find_mount_point('CIRC')

    print(colorize("copy libs"))
    shutil.copytree('../lib', circuit + "/lib/", dirs_exist_ok=True)

    print(colorize("copy code"))
    source_files = os.listdir('../src/')
    for src in source_files:
        try:
            shutil.copy('../src/' + src, circuit)
            print('.', flush=True, end='')
        except IsADirectoryError:
            print(':', flush=True, end='')
    print()

    print(colorize("copy bitmap resources"))
    resource_files = os.listdir('../resources/')
    for res in resource_files:
        try:
            if res.endswith('bmp'):
                shutil.copy('../resources/' + res, circuit)
                print('.', flush=True, end='')
            else:
                print(',', flush=True, end='')
        except IsADirectoryError:
            print(':', flush=True, end='')

    print()


def inject_user(user):
    circuit = find_mount_point('CIRC')
    while not circuit:
        print('.', flush=True, end='')
        time.sleep(0.1)
        circuit = find_mount_point('CIRC')

    user_config = " ".join(list(map(lambda x: f'user.{x}={user[x].replace(" ", "$SPACE#")}', user)))
    user_config += f" "\
        f"wifi.ssid=#berlin24 " \
        f"wifi.pwd=#berlin24 " \
        f"wifi.ip=35.208.138.148 " \
        f"wifi.port=1337 " \
        f"wifi.url=/api/zepass " \
        f"wifi.host=zebadge.app "

    print(f"{colorize('injecting user')}: {user_config}")

    open(circuit + "/ze.conf", "w+").write(
        user_config
    )

    open(circuit + "/zeAlternative.bmp", "wb").write(
        requests.get(
            url=f"{BASE_URL}/api/user/{user['uuid']}/badge",
            headers={
                'Content-Type': 'application/json',
                'ZeAuth': SERVER_TOKEN
            },
        ).content
    )


if __name__ == '__main__':
    badge_path = find_base_badge_path()
    if badge_path:
        print(f"Found a badge on '{colorize(badge_path)}', happy days.")
    else:
        print(colorize("No Badge found!"))
        exit(-1)

    nuke()

    flash()

    copy_code()

    inject_user(request_new_user())

    print(colorize("!! DONE !!"))
