#pragma once

#include <stdint.h>

// ---------------------------------------------------------
// Constants
// ---------------------------------------------------------

#define SPECK_64_96_KEY_LENGTH   12
#define SPECK_64_128_KEY_LENGTH  16
#define SPECK_64_STATE_LENGTH     8
#define SPECK_64_96_NUM_ROUNDS   26
#define SPECK_64_128_NUM_ROUNDS  27

// ---------------------------------------------------------
// Types
// ---------------------------------------------------------

typedef struct {
    uint32_t subkeys[SPECK_64_96_NUM_ROUNDS];
} speck64_context_t;

// ---------------------------------------------------------
// API
// ---------------------------------------------------------

void speck64_round(uint32_t* l, uint32_t* r, const uint32_t* k);

// ---------------------------------------------------------

void speck64_inverse_round(uint32_t* l, uint32_t* r, const uint32_t* k);

// ---------------------------------------------------------

void speck64_key_schedule(speck64_context_t* ctx, 
                          const uint8_t key[SPECK_64_96_KEY_LENGTH]);

// ---------------------------------------------------------

void speck64_encrypt_steps(const speck64_context_t* ctx, 
                           const uint8_t p[SPECK_64_STATE_LENGTH], 
                           uint8_t c[SPECK_64_STATE_LENGTH], 
                           const size_t num_rounds);

// ---------------------------------------------------------

void speck64_decrypt_steps(const speck64_context_t* ctx, 
                           const uint8_t c[SPECK_64_STATE_LENGTH], 
                           uint8_t p[SPECK_64_STATE_LENGTH], 
                           const size_t num_rounds);

// ---------------------------------------------------------

void speck64_encrypt(const speck64_context_t* ctx, 
                     const uint8_t p[SPECK_64_STATE_LENGTH], 
                     uint8_t c[SPECK_64_STATE_LENGTH]);

// ---------------------------------------------------------

void speck64_decrypt(const speck64_context_t* ctx, 
                     const uint8_t c[SPECK_64_STATE_LENGTH], 
                     uint8_t p[SPECK_64_STATE_LENGTH]);
