#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "speck64.h"

// ---------------------------------------------------------

static const uint8_t KEY[] = { 
    0x13,0x12,0x11,0x10,0x0b,0x0a,0x09,0x08,0x03,0x02,0x01,0x00 
};
static const uint8_t PLAINTEXT[] = {
    0x74,0x61,0x46,0x20,0x73,0x6e,0x61,0x65
};
static const uint8_t CIPHERTEXT[] = {
    0x9f,0x79,0x52,0xec,0x41,0x75,0x94,0x6c
};

// ---------------------------------------------------------

static bool test(const char* label, 
                 const uint8_t* expected, 
                 const uint8_t* actual, 
                 const size_t num_bytes) {
    const int was_wrong = memcmp(expected, actual, num_bytes);
    
    if (was_wrong) {
        printf("%s incorrect\n", label);
    } else {
        puts("Pass");
    }

    return !was_wrong;
}

// ---------------------------------------------------------

int main() {
    speck64_context_t ctx;
    speck64_key_schedule(&ctx, KEY);

    uint8_t c[SPECK_64_STATE_LENGTH];
    speck64_encrypt(&ctx, PLAINTEXT, c);
    print_hex("c", c, SPECK_64_STATE_LENGTH);

    uint8_t p[SPECK_64_STATE_LENGTH];
    speck64_decrypt(&ctx, CIPHERTEXT, p);
    print_hex("p", p, SPECK_64_STATE_LENGTH);

    bool result = 0;
    result |= test("Encryption", CIPHERTEXT, c, SPECK_64_STATE_LENGTH);
    result |= test("Decryption", PLAINTEXT,  p, SPECK_64_STATE_LENGTH);

    return result;
}
