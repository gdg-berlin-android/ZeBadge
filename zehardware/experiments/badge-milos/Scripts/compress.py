#!python

import sys
import zlib

if len(sys.argv) < 2:
    print("Usage:\n\tpython compress.py <input_file>")
    sys.exit(1)

file_name = sys.argv[1]
with open(file_name, "rb") as file:
    data_to_compress = file.read()

compressed_data = zlib.compress(data_to_compress)

compressed_file_name = file_name + ".gz"
with open(compressed_file_name, "wb") as compressed_file:
    compressed_file.write(compressed_data)

print("Done.")
