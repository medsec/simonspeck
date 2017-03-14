/**
 * Encrypts <#pairs> of random texts with the given XOR difference <delta_l,
 * delta_r> with 1-step SPARX-64 under <#keys> random keys each, and counts and
 * outputs how many pairs have a zero difference on the left side after the
 * first step.
 * 
 * @author eik list
 * @last-modified 2017-03-03
 */

#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <getopt.h> 

#include "common.h"
#include "speck64.h"

// ---------------------------------------------------------
// Constants
// ---------------------------------------------------------

// ---------------------------------------------------------
// Types
// ---------------------------------------------------------

typedef struct {
    uint32_t delta_in[2];
    uint32_t delta_out[2];
    size_t   current_key_bit;
    uint32_t key_candidate;
    size_t   key_candidates[2];
    size_t   num_keys;
    size_t   num_pairs_per_key;
    size_t   num_key_candidates;
    uint64_t num_collisions;
    size_t   num_rounds;
} experiment_ctx_t;

typedef int table_t;

// ---------------------------------------------------------

static void get_random(uint8_t* data, size_t num_bytes) {
    int gathered_bytes = open("/dev/urandom", O_RDONLY);
    size_t len = 0;

    while (len < num_bytes) {
        ssize_t num_gathered_bytes = read(
            gathered_bytes, 
            data + len, 
            num_bytes - len
        );

        if (num_gathered_bytes < 0) {
            puts("Error, unable to read stream");
            close(gathered_bytes);
            exit(EXIT_FAILURE);
        } else {
            len += num_gathered_bytes;
        }
    }

    close(gathered_bytes);
}

// ---------------------------------------------------------

static void print(const experiment_ctx_t* ctx, 
                  const speck64_context_t* speck_ctx) {
    printf("Correct key for step %2zu: %08x\n", 
        ctx->num_rounds, 
        speck_ctx->subkeys[4]);
    printf("Found key for step   %2zu: %08x\n", 
        ctx->num_rounds, 
        ctx->key_candidate);
}

// ---------------------------------------------------------

static void xor_difference(const experiment_ctx_t* ctx, 
                           uint8_t* p1, 
                           uint8_t* p2) {
    uint32_t p1_as_uint32[2];
    uint32_t p2_as_uint32[2];

    memcpy((void*)p1_as_uint32, (void*)p1, SPECK_64_STATE_LENGTH);
    memcpy((void*)p2_as_uint32, (void*)p2, SPECK_64_STATE_LENGTH);

    p2_as_uint32[0] = p1_as_uint32[0] ^ ctx->delta_in[0];
    p2_as_uint32[1] = p1_as_uint32[1] ^ ctx->delta_in[1];

    p1_as_uint32[0] -= p1_as_uint32[1];
    p2_as_uint32[0] -= p2_as_uint32[1];

    p1_as_uint32[0] = ROTL32(p1_as_uint32[0], 8);
    p2_as_uint32[0] = ROTL32(p2_as_uint32[0], 8);

    to_uint8(p1, p1_as_uint32, SPECK_64_STATE_LENGTH);
    to_uint8(p2, p2_as_uint32, SPECK_64_STATE_LENGTH);
}

// ---------------------------------------------------------

static void reset_counters(experiment_ctx_t* ctx) {
    for (size_t i = 0; i < ctx->num_key_candidates; ++i) {
        ctx->key_candidates[i] = 0;
    }

    ctx->num_collisions = 0;
}

// ---------------------------------------------------------

static bool have_target_difference(const uint32_t state1[2], 
                                   const uint32_t state2[2], 
                                   const uint32_t delta[2]) {
    return ((state1[0] ^ state2[0]) == delta[0]) 
        && ((state1[1] ^ state2[1]) == delta[1]);
}

// ---------------------------------------------------------

static void count_key_candidates(experiment_ctx_t* ctx, 
                                 const uint8_t* c1, 
                                 const uint8_t* c2) {
    uint32_t c1_as_uint32[2];
    uint32_t c2_as_uint32[2];
    to_uint32(c1_as_uint32, c1, SPECK_64_STATE_LENGTH);
    to_uint32(c2_as_uint32, c2, SPECK_64_STATE_LENGTH);

    uint32_t state1[2];
    uint32_t state2[2];
    const uint32_t k_mask = 1 << ctx->current_key_bit;

    for (size_t i = 0; i < ctx->num_key_candidates; ++i) {
        uint32_t k = ctx->key_candidate | (i * k_mask);
        memcpy(state1, c1_as_uint32, SPECK_64_STATE_LENGTH);
        memcpy(state2, c2_as_uint32, SPECK_64_STATE_LENGTH);

        speck64_inverse_round(&(state1[0]), &(state1[1]), &k);
        speck64_inverse_round(&(state2[0]), &(state2[1]), &k);

        if (have_target_difference(state1, state2, ctx->delta_out)) {
            ctx->key_candidates[i]++;
            ctx->num_collisions++;
        }
    }
}

// ---------------------------------------------------------

static void find_key_candidate_bit(experiment_ctx_t* ctx) {
    const uint32_t k_mask = 1 << ctx->current_key_bit;
    uint32_t i = 0;

    if (ctx->key_candidates[1] > ctx->key_candidates[0]) {
        i = 1;
    }

    printf("bit: %2zu, #0: %5zu, #1: %5zu\n", 
        ctx->current_key_bit, 
        ctx->key_candidates[0], 
        ctx->key_candidates[1]
    );

    ctx->key_candidate |= (k_mask * i);
}

// ---------------------------------------------------------

static void get_key(uint8_t key[SPECK_64_96_KEY_LENGTH]) {
    get_random(key, SPECK_64_96_KEY_LENGTH);
}

// ---------------------------------------------------------

static void rotate_differences(experiment_ctx_t* ctx) {
    ctx->delta_in[0]  = ROTL32(ctx->delta_in[0],  1);
    ctx->delta_in[1]  = ROTL32(ctx->delta_in[1],  1);
    ctx->delta_out[0] = ROTL32(ctx->delta_out[0], 1);
    ctx->delta_out[1] = ROTL32(ctx->delta_out[1], 1);
}

// ---------------------------------------------------------

static void run_experiment(experiment_ctx_t* ctx) {
    uint8_t key[SPECK_64_96_KEY_LENGTH];
    speck64_context_t speck_ctx;

    puts("Iterations #Expected output difference");

    for (size_t i = 0; i < ctx->num_keys; ++i) {
        get_key(key);

        speck64_key_schedule(&speck_ctx, key);

        // ---------------------------------------------------------
        // Fill pool of random bytes since opening/closing files
        // iteratively would slow down
        // ---------------------------------------------------------

        const size_t NUM_RANDOM_POOL_BYTES = 
            ctx->num_pairs_per_key * SPECK_64_STATE_LENGTH;
        uint8_t* plaintexts = (uint8_t*)malloc(NUM_RANDOM_POOL_BYTES);
        get_random(plaintexts, NUM_RANDOM_POOL_BYTES);

        uint8_t* p1; 
        uint8_t p2[SPECK_64_STATE_LENGTH];
        uint8_t c1[SPECK_64_STATE_LENGTH];
        uint8_t c2[SPECK_64_STATE_LENGTH];

        for(ctx->current_key_bit = 0; 
            ctx->current_key_bit < 32; 
            ctx->current_key_bit++) {
            reset_counters(ctx);

            for (size_t j = 0; j < ctx->num_pairs_per_key; ++j) {
                p1 = plaintexts + (j * SPECK_64_STATE_LENGTH);
                xor_difference(ctx, p1, p2);
                
                speck64_encrypt_steps(&speck_ctx, p1, c1, ctx->num_rounds);
                speck64_encrypt_steps(&speck_ctx, p2, c2, ctx->num_rounds);

                count_key_candidates(ctx, c1, c2);
            }

            find_key_candidate_bit(ctx);
            rotate_differences(ctx);
        }

        free(plaintexts);
        print(ctx, &speck_ctx);
    }
}

// ---------------------------------------------------------

static void hex_string_to_bytes(const char* src, uint32_t array[2]) {
    const size_t string_len = strlen(src);

    // Each hexadecimal character encodes 4 bit 
    // => the array is half the length of the string.

    if (string_len > 16) { 
        fprintf(stderr, "Error: Input string longer than expected\n");
        return;
    }

    char** end_ptr = NULL;
    const uint64_t value_as_uint64 = strtoul(src, end_ptr, 16);
    array[0] = (uint32_t)((value_as_uint64 >> 32) & 0xFFFFFFFF);
    array[1] = (uint32_t)(value_as_uint64 & 0xFFFFFFFF);
}

// ---------------------------------------------------------

static void initialize_context(experiment_ctx_t* ctx) {
    ctx->delta_in[0] = 0;
    ctx->delta_in[1] = 0;
    ctx->delta_out[0] = 0;
    ctx->delta_out[1] = 0;
    
    ctx->num_keys = 0;
    ctx->num_pairs_per_key = 0;
    ctx->num_key_candidates = 2;
    ctx->key_candidates[0] = 0;
    ctx->key_candidates[1] = 0;
    ctx->num_collisions = 0;
    ctx->num_rounds = 0;
    ctx->key_candidate = 0;
}

// ---------------------------------------------------------

static void print_usage(char** argv) {
    fprintf(stderr, "Usage: %s [-i:k:o:s:p:] [file...]\n", argv[0]);
    fprintf(stderr, 
        "Tries to perform a key-recovery attack on s-round Speck64.\n");
    fprintf(stderr, "-k #Random keys that will be tested.\n");
    fprintf(stderr, "-p #Random pairs/keys.\n");
    fprintf(stderr, "-i 64-bit XOR input difference.\n");
    fprintf(stderr, "-o 64-bit XOR output difference.\n");
    fprintf(stderr, "-s #Rounds\n");
    exit(EXIT_FAILURE);
}

// ---------------------------------------------------------

static void parse_args(experiment_ctx_t* ctx, 
                       const int argc, 
                       char** argv) {
    int opt;

    if (argc < 11) {
        print_usage(argv);
    }

    while ((opt = getopt(argc, argv, "i:k:o:p:s:")) != -1) {
        switch (opt) {
            case 'i': 
                hex_string_to_bytes(optarg, ctx->delta_in);
                break;
            case 'o': 
                hex_string_to_bytes(optarg, ctx->delta_out);
                break;
            case 'k': 
                ctx->num_keys = atoi(optarg);
                break;
            case 's': 
                ctx->num_rounds = (size_t)(atoi(optarg));
                break;
            case 'p': 
                ctx->num_pairs_per_key = (size_t)(atoi(optarg));
                break;
            default:
                print_usage(argv);
        }
    }
}

// ---------------------------------------------------------

int main(int argc, char** argv) {
    experiment_ctx_t ctx;
    initialize_context(&ctx);
    parse_args(&ctx, argc, argv);

    printf("#Keys       %8zu\n", ctx.num_keys);
    printf("#Rounds     %8zu\n", ctx.num_rounds);
    printf("#Pairs/Key  %8zu\n", ctx.num_pairs_per_key);
    printf("Delta L in  %08x\n", ctx.delta_in[0]);
    printf("Delta R in  %08x\n", ctx.delta_in[1]);
    printf("Delta L out %08x\n", ctx.delta_out[0]);
    printf("Delta R out %08x\n", ctx.delta_out[1]);

    run_experiment(&ctx);
    return 0;
}
