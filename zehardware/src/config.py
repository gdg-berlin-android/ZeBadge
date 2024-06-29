import util

_SPACE_REPLACEMENT_ = "$SPACE#"

def _sanatize(config):
    return config.replace('\r\n', '').replace('\n', ' ')

def save_config(config, filename: str = 'ze.conf'):
    file = open(filename, 'w')
    if file:
        file.write(
            fields_to_str(_sanatize(config))
        )


def update_config(config, content: str):
    if content:
        str_to_fields(config, _sanatize(content))
    else:
        print('No content to update.')


def load_config(config, filename: str = 'ze.conf') -> bool:
    try:
        file = open(filename, 'r')
        if file:
            str_to_fields(config, file.read())
        return True
    except Exception as e:
        print(util.exception_to_readable(e))
        return False


def fields_to_str(obj) -> str:
    result = ""
    for field in obj:
        value = obj[field]
        try:
            value = value.replace(' ', _SPACE_REPLACEMENT_)
        except Exception:
            ''

        result += f'{field}={value} '

    return result


def str_to_fields(obj, assignments):
    l = list(filter(lambda y: len(y) == 2, map(lambda x: x.replace(_SPACE_REPLACEMENT_, ' ').replace('\n', '').split('='), assignments.split(' '))))
    read = dict(l)
    for key in read:
        value = read[key]
        obj[key] = _ensure_typed_value(value)


def _ensure_typed_value(value):
    if value == "True":
        value = True
    elif value == "False":
        value = False
    elif "." in value:
        try:
            value = float(value)
        except ValueError:
            value = str(value)
    else:
        try:
            value = int(value)
        except ValueError:
            value = str(value)

    return value


def _hide_str(message):
    return "".join(map(lambda x: "*", message))


def __test_compare(actual, expected):
    for x in actual:
        inner_actual = actual[x]
        inner_expected = expected[x]

        message = f'"{x}" differs: "{inner_actual}" != "{inner_expected}".'

        if inner_actual is not None and inner_expected is not None:
            assert inner_actual == inner_expected, message


def __test__():
    # testing of serialization from and to text
    expected = {
        "last_app": None,
        "developer_mode": True,
        "wifi.ssid": "Your cat has an ssid.",
        "wifi.pwd": "ask pete! !@",
        "wifi.port": 1233.890,
    }

    cfg = fields_to_str(expected)
    print(cfg)
    actual = {}
    str_to_fields(actual, cfg)

    __test_compare(actual, expected)

    assert '*****' == _hide_str('hello')


if __name__ == "__main__":
    # run a test to see if kaput
    __test__()
