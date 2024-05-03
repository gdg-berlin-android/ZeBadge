class Configuration:
    def __init__(self):
        self.keyboard_attached: bool = False
        self.wifi_attached: bool = False
        self.developer_mode: bool = False

        self.last_app: str = ""

        self.wifi: Wifi = Wifi()

    def __str__(self):
        return _fields_to_str(self)


def save_config(config: Configuration, filename: str = '/ze.conf'):
    file = open(filename, 'w')
    if file:
        file.write(
            _fields_to_str(config, "")
        )


def update_config(config: Configuration, content: str):
    _execute_assignments_on_obj(config, content.splitlines())


def load_config(config: Configuration, filename: str = '/ze.conf') -> bool:
    try:
        file = open(filename, 'r')
        if file:
            _execute_assignments_on_obj(config, file.readlines())
        return True
    except FileNotFoundError:
        return False


def _fields_to_str(obj, prefix: str = '') -> str:
    if '__dict__' in dir(obj):
        # obj like structure?
        result = ""
        for field in obj.__dict__:
            if len(prefix) > 0:
                name = f"{prefix}.{field}"
            else:
                name = field

            # recurse into next field
            result += _fields_to_str(obj.__dict__[field], name)

        return result
    else:
        # primitive type?
        if isinstance(obj, str):
            # special string handling
            obj = f'"{obj}"'
        return f"{prefix} = {obj}\n"


def _execute_assignments_on_obj(obj, assignments):
    for assignment in assignments:
        if '=' in assignment:
            field, value = assignment.split('=')
            field = field.strip()
            value = value.strip()
            parts = field.split('.')

            if len(parts) == 1:
                # top level element?
                parts[0] = field

            target = obj
            previous = target
            if len(parts) > 1:
                # loop through all dotted fields
                for part in parts:
                    previous = target
                    target = target.__dict__[part]
            value = _ensure_typed_value(value)
            previous.__dict__[parts[-1]] = value


def _ensure_typed_value(value):
    if '"' in value:
        _, value, _ = value.split('"')
    elif value == "True":
        value = True
    elif value == "False":
        value = False
    else:
        value = int(value)
    return value


class Wifi:
    def __init__(self):
        self.ssid = None
        self.pwd = None
        self.ip = '35.208.138.148'
        self.url = '/'
        self.host = 'char.zebadge.app'
        self.port = 13370

    def __str__(self):
        return _fields_to_str(self).replace(self.pwd, _hide_str(self.pwd))


def _hide_str(message):
    return "".join(map(lambda x: "*", message))


def __test__():
    # testing of serialization from and to text
    expected = Configuration()
    expected.developer_mode = True
    expected.wifi.ssid = "Your cat has an ssid"
    expected.wifi.pwd = "ask pete! !@"
    cfg = _fields_to_str(expected)

    actual = Configuration()
    _execute_assignments_on_obj(actual, cfg.split('\n'))

    assert (expected.__str__() == actual.__str__())

    assert '*****' == _hide_str('hello')


if __name__ == "__main__":
    # run a test to see if kaput
    __test__()
