# The MIT License (MIT)
#
# Copyright (c) 2020 Jim Bennett
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
"""
`circuitpython_base64`
================================================================================

RFC 3548: Base16, Base32, Base64 Data Encodings


* Author(s): Jim Bennett

Implementation Notes
--------------------

**Software and Dependencies:**

* Adafruit CircuitPython firmware for the supported boards:
  https://github.com/adafruit/circuitpython/releases

"""

# imports

__version__ = "0.0.0-auto.0"
__repo__ = "https://github.com/jimbobbennett/CircuitPython_base64.git"


import re
import struct
import adafruit_binascii as binascii


__all__ = [
    # Legacy interface exports traditional RFC 1521 Base64 encodings
    "encode",
    "decode",
    "encodebytes",
    "decodebytes",
    # Generalized interface for other encodings
    "b64encode",
    "b64decode",
    "b32encode",
    "b32decode",
    "b16encode",
    "b16decode",
    # Standard Base64 encoding
    "standard_b64encode",
    "standard_b64decode",
]


BYTES_TYPES = (bytes, bytearray)  # Types acceptable as binary data


def _bytes_from_decode_data(data):
    if isinstance(data, str):
        try:
            return data.encode("ascii")
        #        except UnicodeEncodeError:
        except:
            raise ValueError("string argument should contain only ASCII characters")
    elif isinstance(data, BYTES_TYPES):
        return data
    else:
        raise TypeError(
            "argument should be bytes or ASCII string, not %s" % data.__class__.__name__
        )


# Base64 encoding/decoding uses binascii


def b64encode(toencode, altchars=None):
    """Encode a byte string using Base64.

    toencode is the byte string to encode.  Optional altchars must be a byte
    string of length 2 which specifies an alternative alphabet for the
    '+' and '/' characters.  This allows an application to
    e.g. generate url or filesystem safe Base64 strings.

    The encoded byte string is returned.
    """
    if not isinstance(toencode, BYTES_TYPES):
        raise TypeError("expected bytes, not %s" % toencode.__class__.__name__)
    # Strip off the trailing newline
    encoded = binascii.b2a_base64(toencode)[:-1]
    if altchars is not None:
        if not isinstance(altchars, BYTES_TYPES):
            raise TypeError("expected bytes, not %s" % altchars.__class__.__name__)
        assert len(altchars) == 2, repr(altchars)
        return encoded.translate(bytes.maketrans(b"+/", altchars))
    return encoded


def b64decode(todecode, altchars=None, validate=False):
    """Decode a Base64 encoded byte string.

    todecode is the byte string to decode.  Optional altchars must be a
    string of length 2 which specifies the alternative alphabet used
    instead of the '+' and '/' characters.

    The decoded string is returned.  A binascii.Error is raised if todecode is
    incorrectly padded.

    If validate is False (the default), non-base64-alphabet characters are
    discarded prior to the padding check.  If validate is True,
    non-base64-alphabet characters in the input result in a binascii.Error.
    """
    todecode = _bytes_from_decode_data(todecode)
    if altchars is not None:
        altchars = _bytes_from_decode_data(altchars)
        assert len(altchars) == 2, repr(altchars)
        todecode = todecode.translate(bytes.maketrans(altchars, b"+/"))
    if validate and not re.match(b"^[A-Za-z0-9+/]*={0,2}$", todecode):
        raise binascii.Error("Non-base64 digit found")
    return binascii.a2b_base64(todecode)


def standard_b64encode(toencode):
    """Encode a byte string using the standard Base64 alphabet.

    toencode is the byte string to encode.  The encoded byte string is returned.
    """
    return b64encode(toencode)


def standard_b64decode(todecode):
    """Decode a byte string encoded with the standard Base64 alphabet.

    todecode is the byte string to decode.  The decoded byte string is
    returned.  binascii.Error is raised if the input is incorrectly
    padded or if there are non-alphabet characters present in the
    input.
    """
    return b64decode(todecode)


# Base32 encoding/decoding must be done in Python
BASE32_ALPHABET = {
    0: b"A",
    9: b"J",
    18: b"S",
    27: b"3",
    1: b"B",
    10: b"K",
    19: b"T",
    28: b"4",
    2: b"C",
    11: b"L",
    20: b"U",
    29: b"5",
    3: b"D",
    12: b"M",
    21: b"V",
    30: b"6",
    4: b"E",
    13: b"N",
    22: b"W",
    31: b"7",
    5: b"F",
    14: b"O",
    23: b"X",
    6: b"G",
    15: b"P",
    24: b"Y",
    7: b"H",
    16: b"Q",
    25: b"Z",
    8: b"I",
    17: b"R",
    26: b"2",
}

BASE32_TAB = [v[0] for k, v in sorted(BASE32_ALPHABET.items())]
BASE32_REV = dict([(v[0], k) for k, v in BASE32_ALPHABET.items()])


def b32encode(toencode):
    """Encode a byte string using Base32.

    toencode is the byte string to encode.  The encoded byte string is returned.
    """
    if not isinstance(toencode, BYTES_TYPES):
        raise TypeError("expected bytes, not %s" % toencode.__class__.__name__)
    quanta, leftover = divmod(len(toencode), 5)
    # Pad the last quantum with zero bits if necessary
    if leftover:
        toencode = toencode + bytes(5 - leftover)  # Don't use += !
        quanta += 1
    encoded = bytearray()
    for i in range(quanta):
        # part1 and part2 are 16 bits wide, part3 is 8 bits wide.  The intent of this
        # code is to process the 40 bits in units of 5 bits.  So we take the 1
        # leftover bit of part1 and tack it onto part2.  Then we take the 2 leftover
        # bits of part2 and tack them onto part3.  The shifts and masks are intended
        # to give us values of exactly 5 bits in width.
        part1, part2, part3 = struct.unpack("!HHB", toencode[i * 5 : (i + 1) * 5])
        part2 += (part1 & 1) << 16  # 17 bits wide
        part3 += (part2 & 3) << 8  # 10 bits wide
        encoded += bytes(
            [
                BASE32_TAB[part1 >> 11],  # bits 1 - 5
                BASE32_TAB[(part1 >> 6) & 0x1F],  # bits 6 - 10
                BASE32_TAB[(part1 >> 1) & 0x1F],  # bits 11 - 15
                BASE32_TAB[part2 >> 12],  # bits 16 - 20 (1 - 5)
                BASE32_TAB[(part2 >> 7) & 0x1F],  # bits 21 - 25 (6 - 10)
                BASE32_TAB[(part2 >> 2) & 0x1F],  # bits 26 - 30 (11 - 15)
                BASE32_TAB[part3 >> 5],  # bits 31 - 35 (1 - 5)
                BASE32_TAB[part3 & 0x1F],  # bits 36 - 40 (1 - 5)
            ]
        )
    # Adjust for any leftover partial quanta
    if leftover == 1:
        encoded = encoded[:-6] + b"======"
    elif leftover == 2:
        encoded = encoded[:-4] + b"===="
    elif leftover == 3:
        encoded = encoded[:-3] + b"==="
    elif leftover == 4:
        encoded = encoded[:-1] + b"="
    return bytes(encoded)


def b32decode(todecode, casefold=False, map01=None):
    """Decode a Base32 encoded byte string.

    todecode is the byte string to decode.  Optional casefold is a flag
    specifying whether a lowercase alphabet is acceptable as input.
    For security purposes, the default is False.

    RFC 3548 allows for optional mapping of the digit 0 (zero) to the
    letter O (oh), and for optional mapping of the digit 1 (one) to
    either the letter I (eye) or letter L (el).  The optional argument
    map01 when not None, specifies which letter the digit 1 should be
    mapped to (when map01 is not None, the digit 0 is always mapped to
    the letter O).  For security purposes the default is None, so that
    0 and 1 are not allowed in the input.

    The decoded byte string is returned.  binascii.Error is raised if
    the input is incorrectly padded or if there are non-alphabet
    characters present in the input.
    """
    todecode = _bytes_from_decode_data(todecode)
    _, leftover = divmod(len(todecode), 8)
    if leftover:
        raise binascii.Error("Incorrect padding")
    # Handle section 2.4 zero and one mapping.  The flag map01 will be either
    # False, or the character to map the digit 1 (one) to.  It should be
    # either L (el) or I (eye).
    if map01 is not None:
        map01 = _bytes_from_decode_data(map01)
        assert len(map01) == 1, repr(map01)
        todecode = todecode.translate(bytes.maketrans(b"01", b"O" + map01))
    if casefold:
        todecode = todecode.upper()
    # Strip off pad characters from the right.  We need to count the pad
    # characters because this will tell us how many null bytes to remove from
    # the end of the decoded string.
    padchars = todecode.find(b"=")
    if padchars > 0:
        padchars = len(todecode) - padchars
        todecode = todecode[:-padchars]
    else:
        padchars = 0

    # Now decode the full quanta
    parts = []
    acc = 0
    shift = 35
    for char in todecode:
        val = BASE32_REV.get(char)
        if val is None:
            raise binascii.Error("Non-base32 digit found")
        acc += BASE32_REV[char] << shift
        shift -= 5
        if shift < 0:
            parts.append(binascii.unhexlify(bytes("%010x" % acc, "ascii")))
            acc = 0
            shift = 35
    # Process the last, partial quanta
    last = binascii.unhexlify(bytes("%010x" % acc, "ascii"))
    if padchars == 0:
        last = b""  # No characters
    elif padchars == 1:
        last = last[:-1]
    elif padchars == 3:
        last = last[:-2]
    elif padchars == 4:
        last = last[:-3]
    elif padchars == 6:
        last = last[:-4]
    else:
        raise binascii.Error("Incorrect padding")
    parts.append(last)
    return b"".join(parts)


# RFC 3548, Base 16 Alphabet specifies uppercase, but hexlify() returns
# lowercase.  The RFC also recommends against accepting input case
# insensitively.
def b16encode(toencode):
    """Encode a byte string using Base16.

    toencode is the byte string to encode.  The encoded byte string is returned.
    """
    if not isinstance(toencode, BYTES_TYPES):
        raise TypeError("expected bytes, not %s" % toencode.__class__.__name__)
    return binascii.hexlify(toencode).upper()


def b16decode(todecode, casefold=False):
    """Decode a Base16 encoded byte string.

    todecode is the byte string to decode.  Optional casefold is a flag
    specifying whether a lowercase alphabet is acceptable as input.
    For security purposes, the default is False.

    The decoded byte string is returned.  binascii.Error is raised if
    todecode were incorrectly padded or if there are non-alphabet characters
    present in the string.
    """
    todecode = _bytes_from_decode_data(todecode)
    if casefold:
        todecode = todecode.upper()
    if re.search(b"[^0-9A-F]", todecode):
        raise binascii.Error("Non-base16 digit found")
    return binascii.unhexlify(todecode)


# Legacy interface.  This code could be cleaned up since I don't believe
# binascii has any line length limitations.  It just doesn't seem worth it
# though.  The files should be opened in binary mode.

MAXLINESIZE = 76  # Excluding the CRLF
MAXBINSIZE = (MAXLINESIZE // 4) * 3


def encode(inval, outval):
    """Encode a file; input and output are binary files."""
    while True:
        read = inval.read(MAXBINSIZE)
        if not read:
            break
        while len(read) < MAXBINSIZE:
            next_read = inval.read(MAXBINSIZE - len(read))
            if not next_read:
                break
            read += next_read
        line = binascii.b2a_base64(read)
        outval.write(line)


def decode(inval, outval):
    """Decode a file; input and output are binary files."""
    while True:
        line = inval.readline()
        if not line:
            break
        outval.write(binascii.a2b_base64(line))


def encodebytes(toencode):
    """Encode a bytestring into a bytestring containing multiple lines
    of base-64 data."""
    if not isinstance(toencode, BYTES_TYPES):
        raise TypeError("expected bytes, not %s" % toencode.__class__.__name__)
    pieces = []
    for i in range(0, len(toencode), MAXBINSIZE):
        chunk = toencode[i : i + MAXBINSIZE]
        pieces.append(binascii.b2a_base64(chunk))
    return b"".join(pieces)


def encodestring(toencode):
    """Legacy alias of encodebytes()."""
    import warnings

    warnings.warn(
        "encodestring() is a deprecated alias, use encodebytes()", DeprecationWarning, 2
    )
    return encodebytes(toencode)


def decodebytes(todecode):
    """Decode a bytestring of base-64 data into a bytestring."""
    if not isinstance(todecode, BYTES_TYPES):
        raise TypeError("expected bytes, not %s" % todecode.__class__.__name__)
    return binascii.a2b_base64(todecode)


def decodestring(todecode):
    """Legacy alias of decodebytes()."""
    import warnings

    warnings.warn(
        "decodestring() is a deprecated alias, use decodebytes()", DeprecationWarning, 2
    )
    return decodebytes(todecode)
