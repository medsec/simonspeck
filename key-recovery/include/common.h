#pragma once

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

// ---------------------------------------------------------
// Types
// ---------------------------------------------------------

typedef int bool;

#define ROTL32(x, n) (x<<n | x>>(32-n))
#define ROTR32(x, n) (x>>n | x<<(32-n))

// ---------------------------------------------------------
// Methods
// ---------------------------------------------------------

void print_hex(const char* label, 
               const uint8_t* array, 
               const size_t num_bytes);

// ---------------------------------------------------------

void do_xor(uint8_t* target, const uint8_t* src, const size_t num_bytes);

// ---------------------------------------------------------
// Endian-correct conversion
// ---------------------------------------------------------

void to_uint8(uint8_t* target, 
              const uint32_t* src, 
              const size_t num_bytes);

// ---------------------------------------------------------

void to_uint32(uint32_t* target, 
               const uint8_t* src, 
               const size_t num_bytes);
