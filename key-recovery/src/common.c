#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "common.h"

// ---------------------------------------------------------
// Methods
// ---------------------------------------------------------

void print_hex(const char* label, 
                      const uint8_t* array, 
                      const size_t num_bytes) {
    printf("%s: ", label);
    
    for (size_t i = 0; i < num_bytes; i++) {
        printf("%02x", array[i]);
    }

    puts("");
}

// ---------------------------------------------------------

void do_xor(uint8_t* target, const uint8_t* src, const size_t num_bytes) {
    for (size_t i = 0; i < num_bytes; ++i) {
        target[i] ^= src[i];
    }
}

// ---------------------------------------------------------
// Endian-correct conversion
// ---------------------------------------------------------

void to_uint8(uint8_t* target, 
              const uint32_t* src, 
              const size_t num_bytes) {
    for (size_t i = 0; i < num_bytes/4; i++) {
        target[i*4  ] = (src[i] >> 24) & 0xFF;
        target[i*4+1] = (src[i] >> 16) & 0xFF;
        target[i*4+2] = (src[i] >>  8) & 0xFF;
        target[i*4+3] =  src[i] & 0xFF;
    }
}

// ---------------------------------------------------------

void to_uint32(uint32_t* target, 
               const uint8_t* src, 
               const size_t num_bytes) {
    for (size_t i = 0; i < num_bytes/4; i++) {
        target[i] = 0;
        target[i] |= (uint32_t)(src[i*4  ]) << 24;
        target[i] |= (uint32_t)(src[i*4+1]) << 16;
        target[i] |= (uint32_t)(src[i*4+2]) <<  8;
        target[i] |= (uint32_t)(src[i*4+3])      ;
    }
}
