# Example code for a subkey-recovery on round-reduced SPECK64-96.

SPECK is a family of lightweight block ciphers designed by Beaulieu et al. in
2013. SPECK64-96 is one member of the family with 64-bit state and 96-bit key
size.

[ePrint Link](https://eprint.iacr.org/2013/404).

Readers who are not familiar with differential cryptanalysis can read for a 
good introduction, e.g.,
[A Tutorial on Linear and Differential Cryptanalysis](https://www.engr.mun.ca/~howard/PAPERS/ldc_tutorial.pdf)

This program tries do recover the last-round subkey using a differential
analysis. We always assume XOR differences.

## Differential Trail
The first step is to find a good differential trail with some input difference
Delta_in to some output difference Delta_out with high probability. Note:

- You need an output difference with active bit at the least significant bit for
  the right side for key recovery.

- Note that the addition of the first round is located before the XOR with the
  subkey. Hence, this program takes a state difference *after* the addition and
  inverts it to derive the plaintext. This way, the first addition is linear.

- This implies that the input difference is assumed to hold *after* the first-
  round addition for the left side. The right side of Delta_in is assumed to
  hold simply for the plaintexts.

## Example Differential Trail
| Round | L          | R          | Notes                                 |
|-------|:----------:|:----------:|---------------------------------------|
|0      |  00002400  | *80000480* | (right part of Delta_in)              |
|1      | *00002400* |  00000004  | (left  part of Delta_in)              |
|2      |  00000020  |  00000000  |                                       | 
|3      |  20000000  |  20000000  |                                       |
|4      | *20200000* | *20200001* | (left and right parts of Delta_out)   |
|5      |  20002001  |  21002008  |                                       |

A python script rotate_differences.py to rotate difference is provided in the
scripts folder.

## Steps
The program performs the following steps:

- The program chooses random pairs with input difference Delta_in ,
  inverts the first round addition to derive the left side of the plaintexts.

- Next, it encrypts the plaintext pairs over s rounds of SPECK64 and collects
  the corresponding ciphertext pairs.

- For the r-th bit of the round key, it decrypts the ciphertext pairs over one
  round, that is, it inverts the final round. For all pairs that satisfy the
  given output difference Delta_out after Round s-1, it increments a counter for
  the current value of the r-th key bit. After all pairs are processed, it takes
  the value for the r-th bit of the round key with the highest counter.

- The program repeats this procedure with rotated versions of Delta_in and
  Delta_out for every r = 0 .. 31 to successively find the bits of the last-
  round subkey.

- Finally, it outputs a candidate for the last-round subkey.

## Usage
```
./speck64-key-recovery [-i:k:o:s:p:] [file...]
Tries to perform a key-recovery attack on s-round SPECK64.
-k #Random keys that will be tested.
-p #Random pairs/keys.
-i 64-bit XOR input difference.
-o 64-bit XOR output difference.
-s #Rounds
``` 

## Example Call
``` 
./speck64-key-recovery -k 1 -p 10000 -i 0000240080000480 -o 2020000020200001 -s 5
#Keys              1
#Rounds            5
#Pairs/Key     10000
Delta L in  00002400
Delta R in  80000480
Delta L out 20200000
Delta R out 20200001
Iterations #Expected output difference
bit:  0, #0:     0, #1:     0
bit:  1, #0:     0, #1:    35
bit:  2, #0:     0, #1:   172
bit:  3, #0:     0, #1:     0
bit:  4, #0:     0, #1:     0
bit:  5, #0:     0, #1:     9
bit:  6, #0:     0, #1:     0
bit:  7, #0:     0, #1:    25
bit:  8, #0:     5, #1:     0
bit:  9, #0:     0, #1:     0
bit: 10, #0:     0, #1:   267
bit: 11, #0:     0, #1:     0
bit: 12, #0:     0, #1:     0
bit: 13, #0:     0, #1:    17
bit: 14, #0:     7, #1:     0
bit: 15, #0:     0, #1:     0
bit: 16, #0:     2, #1:    29
bit: 17, #0:     2, #1:    16
bit: 18, #0:     6, #1:    55
bit: 19, #0:     4, #1:    55
bit: 20, #0:    12, #1:     3
bit: 21, #0:    38, #1:     5
bit: 22, #0:    47, #1:     4
bit: 23, #0:    13, #1:     2
bit: 24, #0:    31, #1:     4
bit: 25, #0:     8, #1:    84
bit: 26, #0:   123, #1:    22
bit: 27, #0:     7, #1:    87
bit: 28, #0:    66, #1:     9
bit: 29, #0:    27, #1:   237
bit: 30, #0:   115, #1:    10
bit: 31, #0:   128, #1:   128
Correct key for step  5: aa0f36ef
Found key for step    5: 2a0f24a6
```

Note that the script does NOT always identify all subkey bits correctly.
Instead, the success probability depends largely on the used differential the
used key and the number of pairs. For instance, the run above recognized

10101010 00001111 00110110 11101111 as key, where the correct key was
00101010 00001111 00100100 10100110.

That is, 6 bits were incorrectly recognized. In the output, you can recognize
these positions where there are no or an equal wrong number of correct pairs for
those bits (0, 0) or (128, 128). One can repeat the run with another
differential then.

## Dependencies
- make
- clang (can be changed to the C compiler of your choice)

Assumes Linux/Unix platforms (uses /dev/urandom). On Windows etc., 
replace the calls to /dev/urandom by CryptGenRandom or similar.

## License
Author: Eik List 2017.

This code is free software: you can redistribute it and/or modify
it under the terms of the GNU LESSER GENERAL PUBLIC LICENSE.

This code is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU LESSER GENERAL PUBLIC LICENSE for more details.

You should have received a copy of the GNU LESSER GENERAL PUBLIC LICENSE.
