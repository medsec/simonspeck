// =========================================================================
// Copyright (c) 2013 Farzaneh Abed, Eik List, Jakob Wenzel
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
// =========================================================================

#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <string.h>

#define uint16_max 65535
#define uint24_max 16777215
#define uint32_max 4294967295

typedef unsigned short int uint16;
typedef unsigned int uint32;

/**
 * Computes the hamming weight of the given 32-bit value.
 */
int hamming(uint32 i) {
	i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
    return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
}

/**
 * Another way to Computes the hamming weight of the given 32-bit value.
 */

//int hamming(uint32 i) {
  //  int r=0;
   // while (i) {
     //   if (i & 0x1)
       //     r++;
        //i>>= 1;
    //}
   //return r;
//}

void print_binary(const uint32 value, const int word_size, char* b) {
    b[0] = '\0';

    int i;
	uint32 mask = 1;

    for (i = 0; i <= word_size; i++, mask <<= 1) {
		strcat(b, ((value & mask) != 0) ? "1" : "0");
    }
}

void print_difference(const uint32 value, const int word_size,
	char* b) {
	// Max: 32 active bits * (2 chars/index + comma as separator)
	b[0] = '\0';

	bool is_first = true;
    int i;
	uint32 mask = 1;
	int j;

    for (i = 0; i <= word_size; i++, mask <<= 1) {
		if ((value & mask) != 0) {
			char str[5];
			//itoa(i, str, 10);

			if (is_first) {
				is_first = false;
			} else {
				strcat(b, ",");
			}

			strcat(b, str);
		}
    }

	if (is_first) {
		strcat(b, "-");
	}
}

/**
 * Rotates the given n-bit value by r bits to the left,
 * where n is the word size.
 */
uint32 rotate_left(uint32 value, int r, int word_size) {
	r %= word_size;

	if (r < 0) {
		r += word_size;
	}

	uint32 lsb_mask = (1 << (word_size - r)) - 1;
	uint32 msb_mask = (1 << r) - 1;

	return ((value >> (word_size - r)) & msb_mask)
		| ((value & lsb_mask) << r);
}

/**
 * Rotates the given n-bit value by r bits to the right,
 * where n is the word size.
 */
uint32 rotate_right(uint32 value, int r, int word_size) {
	r %= word_size;

	if (r < 0) {
		r += word_size;
	}

	uint32 lsb_mask = (1 << r) - 1;
	uint32 msb_mask = (1 << (word_size - r)) - 1;

	return ((value & lsb_mask) << (word_size - r))
		| ((value >> r) & msb_mask);
}

/**
 * Helper construct for a one-round 16-bit difference.
 */
struct Difference {
	uint16 left;
	uint16 right;
};

#endif


