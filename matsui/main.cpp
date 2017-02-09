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
//
// =========================================================================

#include <stdio.h>
#include <math.h>
#include <getopt.h>
#include <stdlib.h>
#include "iterator.cpp"
#include "constants.h"

/**
 * Fixes stdlib bug when compiling with gcc. Alternatively, compile with:
 * g++ main.cpp -o <outname> or
 * gcc main.cpp -o <outname> -lstdc++
 *
 * See for details:
 *
http://stackoverflow.com/questions/6045809/problem-with-g-and-undefined-reference-to-gxx-personality-v0
 * http://stackoverflow.com/questions/329059/what-is-gxx-personality-v0-for
 */
void *__gxx_personality_v0;

// Initial differential trail for Simon32/64.
int initial_trail_for_simon32[30] = {
	0, 2, 4, 8, 10, 16, 20, 26, 28, 32,
	34, 36, 36, 38, 40, 44, 46, 52, 56, 62,
	64, 68, 70, 72, 72, 74, 76, 80, 82, 88
	// 0, 2, 2, 4, 2, 6, 4, 6, 2, 4, 2, 2, 0
};

// Initial differential trail for Simon48/k.
int initial_trail_for_simon48[30] = {
	0, 2, 4, 8, 12, 22, 26, 30, 34, 38,
	40, 46, 50
	// 0, 2, 2, 4, 2, 6, 4, 6, 2, 8,
	// 6, 10, 4, 6, 2, 4, 2, 6, 4, 10,
	// 6, 8, 2, 6, 4, 6, 2, 4, 2, 2, 0
};

// Initial differential trail for Simon64/k.
int initial_trail_for_simon64[20] = {
	0, 2, 4, 8, 10, 16, 20, 26, 32, 40,
	44, 50, 52, 56, 58, 60, 60, 62, 64, 68
	// 0, 2, 2, 4, 2, 6, 4, 6, 2, 8,
	// 6, 10, 4, 10, 6, 8, 2, 6, 4, 6,
	// 2, 4, 2, 2, 0
};

uint32 current_trail[40][2];
int current_probabilities[40];

int *p;
int r = 10;
int start_round = 1;
int word_size = 16;
int threshold = 32;
uint32 start_delta_left = 0x0000;
uint32 start_delta_right = 0x0040;

// External variables for reading command line options
extern char *optarg;
extern int optind, opterr, optopt;

/**
 * Matsui's algorithm. Takes the current difference (Delta L, Delta R) of
 * the given round, and the current probability of the trail (weight).
 * @param delta_l Delta L.
 * @param delta_r Delta R.
 * @param probability The negative logarithm of the probability of the current trail.
 * E.g., if the probability is 2^{-20}, probability = 20.
 * @param round The current round.
 * @param p Pointer to the probabilities of the reference trail.
 */
void next_round(uint32 delta_l, uint32 delta_r, int probability, int round, int *p) {
	// Store the current trail in order to trace all round-wise differences and
	// their individual probabilities to trace them later if the current trail
	// is a new one with highest probability.
	current_trail[round - 1][0] = delta_l;
	current_trail[round - 1][1] = delta_r;
	current_probabilities[round - 1] = probability;

	// If we have reached the last round,
	// trace the trail if it has higher probability than our reference in p,
	// and update our reference trail in p.
    if (round == r + 1) {
        if (probability < threshold) {
            threshold = probability;

			int i;

			for (i = 0; i <= r; ++i) {
				char b[96];
				char c[96];

				print_difference(current_trail[i][0], word_size, b);
				print_difference(current_trail[i][1], word_size, c);

				printf("%2i Pr: %2i Delta L: %016x Delta R: %016x \n",
					i, current_probabilities[i], current_trail[i][0], 
					current_trail[i][1]
				);
			}

			printf("Pr: 2^{-%i} \n", probability);

        }

        return;
    }

	probability += 2 * hamming(delta_l);

	// If the trail is worse than our reference trail in p, then,
	// stop depth search.
    if (probability + p[r - round] > threshold) {
        return;
    }

	// Otherwise, for all possible output differences, trace the trail through
	// the next round.
	Iterator iterator;
	iterator.set_word_size(word_size);
    iterator.set_difference(delta_l, delta_r);

	uint32 delta_r_out;

    while(iterator.has_next()) {
        iterator.next(&delta_r_out);
        next_round(
			delta_r_out, delta_l, probability, round + 1, p
		);
    }
}

/**
 * Prints the usage of the program and exits the program
 * after wrong user input, e.g., if the word size/number of rounds
 * have not been specified by the user.
 */
void print_usage_and_exit() {
	fprintf(stderr, "%s\n", "Usage: -w <16|24|32> -n <num_rounds> -l [Delta L_2] -r [Delta R_2]");
	exit(EXIT_FAILURE);
}

/**
 * Checks if the word size is in {16,24,32}, which stand for
 * Simon32/64, Simon48/k, and Simon64/k, respectively.
 */
void check_word_size(int word_size) {
	if (word_size == 16) {
		p = initial_trail_for_simon32;
	} else if (word_size == 24) {
		p = initial_trail_for_simon48;
	} else if (word_size == 32) {
		p = initial_trail_for_simon64;
	} else {
		print_usage_and_exit();
	}
}

/**
 * Uses getopt to read the command line parameters.
 * Exits the program if the word size (n) of the cipher
 * or the number of rounds (r) are not specified.
 */
void read_and_check_args(int argc, char *argv[]) {
	bool is_word_size_given = false;
	bool is_num_rounds_given = false;
	int opt;

	while ((opt = getopt(argc, argv, "w:n:l:r:")) != -1) {
		switch (opt) {
			case 'w':
				word_size = atoi(optarg);
				check_word_size(word_size);
				// Should be little higher than double word size
				threshold = (int)(2.5f * (float)word_size);
				is_word_size_given = true;
				break;
			case 'n':
				r = atoi(optarg);
				is_num_rounds_given = true;
				break;
			case 'l':
				start_delta_left = strtol(optarg, NULL, 16);
				break;
			case 'r':
				start_delta_right = strtol(optarg, NULL, 16);
				break;
			default:
				print_usage_and_exit();
		}
	}

	if (!is_word_size_given || !is_num_rounds_given) {
		print_usage_and_exit();
	}
}

int main(int argc, char *argv[]) {
	read_and_check_args(argc, argv);

	next_round(
		start_delta_left,
		start_delta_right,
		2 * hamming(start_delta_left),
		start_round,
		p
	);

	return EXIT_SUCCESS;
}

