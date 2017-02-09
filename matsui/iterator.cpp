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

#include "iterator.h"

Iterator::Iterator() {
	
}

Iterator::~Iterator() {
	
}

void Iterator::set_difference(uint32 l, uint32 r) {
	current = 0;
	left = l;
	right = r;
	num_active_bits = hamming(left);
	num_elements = 1 << (2 * num_active_bits);
	
	store_active_bit_masks(left, num_active_bits);
}

void Iterator::set_word_size(int word_size) {
	this->word_size = word_size;
}

bool Iterator::has_next() {
	return current < num_elements;
}

void Iterator::next(uint32 *output) {
	if (!has_next()) {
		return;
	}
	
	// left  = 	0000 0001 0100 0000 (8,6)
	// right = 	0000 0000 0000 0001 (1)
	// current = 11/16 = 1011
	// correct output = 0x4080
	// 6 + 1 = 	0000 0000 1000 0000
	// 6 + 8 = 	0100 0000 0000 0000
	// 8 + 8 = 	0000 0000 0000 0001
	// right =	0000 0000 0000 0001
	// ----------------------------
	// 			0100 0000 1000 0000
	
	*output = right;
	
	// Masks the bit in current, will be 0001, 0010, 0100, 1000
	uint32 mask = 1;
	// How many masks to apply?
	uint32 num_masks = 2 * num_active_bits; // 2 * 2 = 4
	int i;
	
	for (i = 0; i < num_masks; ++i) {
		if ((current & mask) != 0) {
			*output ^= active_bits[i];
		}
		
		mask <<= 1;
	}
	
	// XOR the output with \Delta L_i <<< 2
	*output ^= rotate_left(left, 2, word_size);
	current++;
}

void Iterator::store_active_bit_masks(uint32 value, uint32 num_active_bits) {
	// value = 0000 0001 0100 0000 = ,6
	// array shall become: [
	// 	0000 0000 1000 0000, =  6 + 1
	// 	0100 0000 0000 0000, =  6 + 8
	// 	0000 0010 0000 0000, =  8 + 1
	// 	0000 0000 0000 0001, =  8 + 8
	// ]
	
	int i, j;
	// Will be 0001, 0010, 0100, 1000
	uint32 mask = 1;
	uint32 output_difference_mask;
	
	const uint32 num_masks = 2 * num_active_bits;
	
	for (i = 0, j = 0; i < word_size; ++i) {
		if ((value & mask) != 0) {
			output_difference_mask = rotate_left((value & mask), 1, word_size);
			active_bits[j++] = output_difference_mask;
			output_difference_mask = rotate_left((value & mask), 8, word_size);
			active_bits[j++] = output_difference_mask;
		}
		
		mask <<= 1;
	}
}


