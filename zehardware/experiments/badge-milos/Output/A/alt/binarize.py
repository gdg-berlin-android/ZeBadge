#!python

import sys
from PIL import Image # pip install pillow

if len(sys.argv) < 5 or sys.argv[1] != "-i" or sys.argv[3] != "-o":
    print("Usage:\n\tpython binarize.py -i <input_file.bmp> -o <output_file.bin>")
    sys.exit(1)

input_file = sys.argv[2]
output_file = sys.argv[4]

image = Image.open(input_file)
image_bw = image.convert("1")
pixels = image_bw.tobytes()

with open(output_file, "wb") as file:
    file.write(pixels)

print("Done.")
