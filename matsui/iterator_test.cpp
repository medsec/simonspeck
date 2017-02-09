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
#include <math.h>
#include "iterator.cpp"
#include "constants.h"

// \Delta L = 0000 0001 0100 0000
// \Delta R = 0000 0000 0000 0001
const Difference input_difference = { 0x0140, 0x0001 }; 
const uint32 expected_outputs[] = { 
	0x0501, 0x0581, 0x4501, 0x4581, 
	0x0701, 0x0781, 0x4701, 0x4781, 
	0x0500, 0x0580, 0x4500, 0x4580, 
	0x0700, 0x0780, 0x4700, 0x4780 
};
bool all_tests_passed = true;
Iterator iterator;

/**
 * Tests (only) if the iterator creates the correct number of output differences
 * for the given input difference above.
 * Sets all_tests_passed to false if not.
 */
void test_num_output_differences() {
	const uint32 num_expected_output_differences = 
		1 << (2 * hamming(input_difference.left));
	
	iterator.set_difference(input_difference.left, input_difference.right);
	uint32 output;
	uint32 num_output_differences = 0;
	
	while (iterator.has_next()) {
		iterator.next(&output);
		num_output_differences++;
	}
	
	if (num_output_differences != num_expected_output_differences) {
		printf("#Output differences incorrect. Expected %i but was %i.\n", 
			num_expected_output_differences, num_output_differences);
		all_tests_passed = false;
	}
	
}

/**
 * Tests if the iterator creates the correct output differences
 * for the given input difference above. 
 * Sets all_tests_passed to false if not.
 */
void test_output_differences() {
	uint32 output;
	uint32 expected_output;
	uint32 i = 0;
	
	iterator.set_difference(input_difference.left, input_difference.right);
	iterator.set_word_size(16);
	
	while (iterator.has_next()) {
		expected_output = expected_outputs[i];
		output = input_difference.right;
		
		try {
			iterator.next(&output);
			
			if (output != expected_output) {
				printf(
					"Output difference #%i incorrect. Expected 0x%x but was 0x%x.\n", 
					i, expected_output, output
				);
				all_tests_passed = false;
			}
		} catch (const char* error) {
			fprintf(stderr, error);
			all_tests_passed = false;
			return;
		}
		
		i++;
	}
}

/**
 * Simply runs all tests.
 */
int main() {
	test_num_output_differences();
	test_output_differences();
	
	if (all_tests_passed) {
		printf("All tests passed.");
	}
	
	return 0;
}
