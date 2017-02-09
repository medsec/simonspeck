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

#ifndef ITERATOR_H_INCLUDED
#define ITERATOR_H_INCLUDED

#include <stdlib.h>
#include "constants.h"

/**
 * Given an input difference (\Delta L_i, \Delta R_i) for one round of SIMON2n/k, 
 * this class can produce all possible output differences \Delta L_{i + 1}. 
 * The possible output differences are not stored, but provided one after the 
 * other each time a client calls the next() method. 
 * 
 * In advance, one has to call the method set_difference(\Delta L_i, \Delta R_i) 
 * with the desired input difference, and to call the method
 * set_word_size(n) to specify the correct word size of SIMON.
 */
class Iterator {
	
	public:
		Iterator();
		~Iterator();
		/**
		 * Set the input difference \Delta L_i, \Delta R_i with this method. 
		 * Has to be called before one can obtain output differences 
		 * \Delta L_{i + 1} by calling next().
		 */
		void set_difference(uint32 left, uint32 right);
		/**
		 * Simon2n/k can be used for several word sizes n, n in {16,24,32,48,64}.
		 * This method must be called to specify the used word size n before 
		 * trying to obtain output differences \Delta L_{i + 1} by calling next().
		 */
		void set_word_size(int word_size);
		/**
		 * Returns true if for the current input difference, there is another new 
		 * possible output difference. Returns false otherwise.
		 * Has to be called before invoking next().
		 */
		bool has_next();
		/**
		 * Stores the next possible output difference \Delta L_{i + 1} in the
		 * address of right. Does nothing, if there is all possible output
		 * difference have been asked before by calling this method.
		 * You must call has_next() before invoking next().
		 */
		void next(uint32 *right);
		
	private:
		/**
		 * For the given input difference \Delta L_i, we need to apply the same
		 * masks each time to compute new output differences. 
		 * The values of these masks are the active bits of \Delta L_i, rotated 
		 * by 1 and 8 positions, respectively. This method precomputes these
		 * masks once in advance, once the set_difference() method is invoked, 
		 * and stores them in the active_bits member to simplify the 
		 * later computations in the next() method.
		 */
		void store_active_bit_masks(uint32 value, uint32 num_active_bits);
		
		/**
		 * Stores the masks of the active bits of \Delta L_i to simplify the 
		 * computations in the next() method.
		 */
		uint32 active_bits[32];
		/**
		 * This class "iterates" through all (say m) possible output differences.
		 * This member keeps track of the index i in [0,..,m-1] of the new
		 * output difference. 
		 */
		uint32 current;
		/** 
		 * Stores \Delta L_i.
		 */
		uint32 left;
		/**
		 * Stores the number of active bits in \Delta L_i.
		 */
		uint32 num_active_bits;
		/**
		 * This class "iterates" through all (say m) possible output differences, 
		 * so, this member just stores m, which is m = 2^{2 * hamming(\Delta L_i)}.
		 */
		uint32 num_elements;
		/** 
		 * Stores \Delta R_i.
		 */
		uint32 right;
		/**
		 * The word size of SIMON. Must be 16, 24, 32, 48, or 64.
		 */
		int word_size = 16;
	
};

#endif
