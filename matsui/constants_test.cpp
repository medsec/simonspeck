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

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "constants.h"

const uint32 values[] = { 
	0x05010581, 0x45014581, 
	0x07010781, 0x47014781, 
	0x05000580, 0x45004580, 
	0x07000780, 0x47004780 
};
const int expected_weights[] = { 7, 9, 9, 11, 5, 7, 7, 9 };
bool all_tests_passed = true;

void test_hamming_weight() {
	int i, weight;
	const int length = sizeof(values) / sizeof(uint32);
	
	for(i = 0; i < length; ++i) {
		weight = hamming(values[i]);
		
		if (weight == expected_weights[i]) {
			printf("%i weight: %i \n", i, weight);
		} else if (weight != expected_weights[i]) {
			printf("Wrong hamming weight for %i. Expected %i but was %i \n", 
				i, expected_weights[i], weight
			);
			return;
		}
	}
}

void test_print_difference() {
	int i;
	int word_size = 16;
	uint32 values[] = { 0x0000, 64 };
	const int length = sizeof(values) / sizeof(uint32);
	
	for(i = 0; i < length; ++i) {
		char s[96];
		print_difference(values[i], word_size, s);
		printf("%s \n", s);
	}
}

/**
 * Simply runs all tests.
 */
int main() {
	test_hamming_weight();
	test_print_difference();
	
	if (all_tests_passed) {
		printf("All tests passed.");
	}
	
	return EXIT_SUCCESS;
}
