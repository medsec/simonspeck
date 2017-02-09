Code for searching trails in SIMON and SPECK

# Tool 
Provides a Java application that performs a branch-and-bound search for
differential trails under the Markov-cipher assumption of independent random
round keys.

tool/src/de/mslab/branchandbound/BranchAndBoundApplication

## Dependencies 

- Apache Commons CLI

# Matsui 

Performs Matsui's algorithm for searching trails under the Markov-
cipher assumption of independent random round keys.

## Usage: 
```
make
./main -w <16|24|32> -n <num_rounds> -l [Delta L_2] -r [Delta R_2]");
```
where 
- w is the word size (16 for SIMON-32)
- n is the number of rounds
- l is the left part of the start difference
- r is the right part of the start difference

