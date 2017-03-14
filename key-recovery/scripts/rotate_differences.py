#!/usr/bin/env python

'''
Example call:
./rotate_differences.py -a 00002400 -b 80000480 -c 20200000 -d 20200001 -r 0
'''

import sys
import argparse

# ----------------------------------------------------------

def rotl32(value, r):
    return ((value << r) | (value >> (32 - r))) & 0xFFFFFFFFL

# ----------------------------------------------------------

def rotr32(value, r):
    return ((value >> r) | (value << (32 - r))) & 0xFFFFFFFFL

# ----------------------------------------------------------

def rotl64(value, r):
    return ((value << r) | (value >> (64 - r))) & 0xFFFFFFFFFFFFFFFFL

# ----------------------------------------------------------

def rotr64(value, r):
    return ((value >> r) | (value << (64 - r))) & 0xFFFFFFFFFFFFFFFFL

# ----------------------------------------------------------

def main(initial_delta_in, initial_delta_out, initial_r):
    for i in xrange(32):
        r = i - initial_r
        delta_in = [0, 0]
        delta_out = [0, 0]

        if r < 0:
            delta_in[0]  = rotr32(initial_delta_in[0],  32+r)
            delta_in[1]  = rotr32(initial_delta_in[1],  32+r)
            delta_out[0] = rotr32(initial_delta_out[0], 32+r)
            delta_out[1] = rotr32(initial_delta_out[1], 32+r)
        else:
            delta_in[0]  = rotl32(initial_delta_in[0],  r)
            delta_in[1]  = rotl32(initial_delta_in[1],  r)
            delta_out[0] = rotl32(initial_delta_out[0], r)
            delta_out[1] = rotl32(initial_delta_out[1], r)

        print("r: {:2}, delta in: {:08x} {:08x}, delta out: {:08x} {:08x}"
            .format(r, delta_in[0], delta_in[1], delta_out[0], delta_out[1]))

# ----------------------------------------------------------

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description="Rotates Speck64 differences given as hexadecimal values.",
        add_help=True)
    parser.add_argument("-a", help="left  input difference", required=True)
    parser.add_argument("-b", help="right input difference", required=True)
    parser.add_argument("-c", help="left output difference", required=True)
    parser.add_argument("-d", help="right output difference", required=True)
    parser.add_argument("-r", help="initial rotation", default=0)
    args = parser.parse_args()

    initial_delta_in  = [int(args.a, 16), int(args.b, 16)]
    initial_delta_out = [int(args.c, 16), int(args.d, 16)]
    initial_r = int(args.r)

    main(initial_delta_in, initial_delta_out, initial_r)
