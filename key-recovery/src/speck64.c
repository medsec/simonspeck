#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "speck64.h"

// ---------------------------------------------------------
// Constants
// ---------------------------------------------------------

// SPECK versions
#define SPECK_64_96   0
#define SPECK_64_128  1

#ifndef SPECK_VERSION
#define SPECK_VERSION SPECK_64_96
#endif

#if (SPECK_VERSION == SPECK_64_96)

static const size_t KEY_LENGTH = SPECK_64_96_KEY_LENGTH;
static const size_t NUM_ROUNDS = SPECK_64_96_NUM_ROUNDS;

#elif (SPECK_VERSION == SPECK_128_128)

static const size_t KEY_LENGTH = SPECK_64_128_KEY_LENGTH;
static const size_t NUM_ROUNDS = SPECK_64_128_NUM_ROUNDS;

#endif

// ---------------------------------------------------------
// Basic functions and their inverses
// ---------------------------------------------------------

void speck64_round(uint32_t* l, uint32_t* r, const uint32_t* k) {
    (*l) = ROTR32((*l), 8);
    (*l) += (*r);
    (*l) ^= (*k);
    (*r) = ROTL32((*r), 3);
    (*r) ^= (*l);
}

// ---------------------------------------------------------

void speck64_inverse_round(uint32_t* l, uint32_t* r, const uint32_t* k) {
    (*r) ^= (*l);
    (*r) = ROTR32((*r), 3);
    (*l) ^= (*k);
    (*l) -= (*r);
    (*l) = ROTL32((*l), 8);
}

// ---------------------------------------------------------
// Key Schedule
// ---------------------------------------------------------

void speck64_key_schedule(speck64_context_t* ctx, 
                          const uint8_t master_key[KEY_LENGTH]) {
    uint32_t key[SPECK_64_96_KEY_LENGTH];
    to_uint32(key, master_key, SPECK_64_96_KEY_LENGTH);
    uint32_t lp2;
    uint32_t lp1;
    uint32_t lp0;

    ctx->subkeys[0] = key[2];

    for (uint32_t i = 0; i < NUM_ROUNDS-1; ++i) {
        if (i == 0) {
            lp0 = key[1]; // L[1] = left
            lp1 = key[0]; // L[0] = right
        } else {
            lp0 = lp1;    // L[0] = new left
            lp1 = lp2;    // L[2] = next in pipeline
        }

        lp2 = (ROTR32(lp0, 8) + ctx->subkeys[i]) ^ i;           // left side
        ctx->subkeys[i + 1] = ROTL32(ctx->subkeys[i], 3) ^ lp2; // new right
    }
}

// ---------------------------------------------------------
// API
// ---------------------------------------------------------

void speck64_encrypt_steps(const speck64_context_t* ctx, 
                           const uint8_t p[SPECK_64_STATE_LENGTH], 
                           uint8_t c[SPECK_64_STATE_LENGTH], 
                           const size_t num_rounds) {
    uint32_t state[2];
    to_uint32(state, p, SPECK_64_STATE_LENGTH);

#ifdef DEBUG
    printf("Round %2d\n", 0);
    print_hex("State (bytes)", (uint8_t*)state, 8);
    printf("Left  (uint)   %08x\n", state[0]);
    printf("Right (uint)   %08x\n", state[1]);
#endif    
    
    for (size_t i = 0; i < num_rounds; ++i) {
        speck64_round(&(state[0]), &(state[1]), &(ctx->subkeys[i]));

#ifdef DEBUG
        printf("Round %2zu\n", i+1);
        print_hex("State (bytes)", (uint8_t*)state, 8);
        printf("Left  (uint)   %08x\n", state[0]);
        printf("Right (uint)   %08x\n", state[1]);
        printf("Key   (uint)   %08x\n", ctx->subkeys[i]);
#endif
    }

    to_uint8(c, state, SPECK_64_STATE_LENGTH);
}

// ---------------------------------------------------------

void speck64_decrypt_steps(const speck64_context_t* ctx, 
                           const uint8_t c[SPECK_64_STATE_LENGTH], 
                           uint8_t p[SPECK_64_STATE_LENGTH], 
                           const size_t num_rounds) {
    uint32_t state[2];
    to_uint32(state, c, SPECK_64_STATE_LENGTH);
    
    for (int i = num_rounds-1; i >= 0; --i) {
        speck64_inverse_round(&(state[0]), &(state[1]), &(ctx->subkeys[i]));
    }

    to_uint8(p, state, SPECK_64_STATE_LENGTH);
}

// ---------------------------------------------------------

void speck64_encrypt(const speck64_context_t* ctx, 
                     const uint8_t p[SPECK_64_STATE_LENGTH], 
                     uint8_t c[SPECK_64_STATE_LENGTH]) {
    speck64_encrypt_steps(ctx, p, c, NUM_ROUNDS);
}

// ---------------------------------------------------------

void speck64_decrypt(const speck64_context_t* ctx, 
                     const uint8_t c[SPECK_64_STATE_LENGTH], 
                     uint8_t p[SPECK_64_STATE_LENGTH]) {
    speck64_decrypt_steps(ctx, c, p, NUM_ROUNDS);
}
