# export ASAN_SYMBOLIZER_PATH=/usr/bin/llvm-symbolizer-3.5
# export ASAN_OPTIONS=symbolize=1

CC = clang
CCFLAGS = -W -Wall -Wextra -std=c11 -march=native -O3
OBJECTS = *.o
INCLUDE = include
SRC = src
BIN = bin

ifdef DEBUG
CCFLAGS += -ggdb3 -DDEBUG -fsanitize=undefined -fsanitize=alignment -ftrapv -fno-omit-frame-pointer -fno-optimize-sibling-calls
endif

.PHONY: all clean format speck64-key-recovery test-speck64

all: speck64-key-recovery test-speck64

$(BIN)/%.o: $(SRC)/%.c
	$(CC) $(CCFLAGS) -I$(INCLUDE) -c $< -o $@

test-speck64: $(BIN)/speck64.o $(BIN)/common.o
	$(CC) $(CCFLAGS) -I$(SRC) -I$(INCLUDE) $(SRC)/$@.c $^ -o $(BIN)/$@

speck64-key-recovery: $(BIN)/speck64.o $(BIN)/common.o
	$(CC) $(CCFLAGS) -I$(SRC) -I$(INCLUDE) $(SRC)/$@.c $^ -o $(BIN)/$@

format:
	astyle *.c *.h

clean:
	rm -rf $(BIN)/*
