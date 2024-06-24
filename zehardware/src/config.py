_SPACE_REPLACEMENT_ = "$SPACE#"


def save_config(config, filename: str = '/ze.conf'):
    file = open(filename, 'w')
    if file:
        file.write(
            fields_to_str(config)
        )


def update_config(config, content: str):
    if content:
        _execute_assignments_on_obj(config, content)
    else:
        print('No content to update.')


def load_config(config, filename: str = '/ze.conf') -> bool:
    try:
        file = open(filename, 'r')
        if file:
            _execute_assignments_on_obj(config, file.readlines())
        return True
    except Exception:
        return False


def fields_to_str(obj) -> str:
    result = ""
    for field in obj:
        value = obj[field]

        if isinstance(value, str):
            value = f"{value.replace(' ', _SPACE_REPLACEMENT_)}"

        result += f'{field}={value} '

    return result


def _execute_assignments_on_obj(obj, assignments):
    assignments = assignments.split(' ')

    for assignment in assignments:
        assignment = assignment.replace(_SPACE_REPLACEMENT_, ' ')
        if '=' in assignment:
            key, *values = assignment.split('=')
            key = key.strip()
            value = '='.join(values)

            if value:
                value = _ensure_typed_value(value)

            obj[key] = value


def _ensure_typed_value(value):
    if '"' in value:
        # TODO: Replace only start and end quotes.
        value = str(value.replace('"', ""))
    elif "'" in value:
        # TODO: Replace only start and end quotes.
        value = str(value.replace("'", ""))
    elif value == "True":
        value = True
    elif value == "False":
        value = False
    elif "." in value:
        try:
            value = float(value)
        except ValueError:
            value = None
    else:
        try:
            value = int(value)
        except ValueError:
            value = None

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
        "wifi.ssid": "Your cat has an ssid",
        "wifi.pwd": "ask pete! !@",
        "wifi.port": 1233.890
    }

    cfg = fields_to_str(expected)
    print(cfg)

    actual = {}
    _execute_assignments_on_obj(actual, cfg)

    __test_compare(actual, expected)

    assert '*****' == _hide_str('hello')


if __name__ == "__main__":
    # run a test to see if kaput
    __test__()
