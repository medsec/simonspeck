CC = g++
CFLAGS = -std=c++11
#-Wall -Wextra
FILE = main

all: build

build:
	$(CC) $(CFLAGS) $(FILE).cpp -o $(FILE)

run:
	./$(FILE) -w 16 -n 10 -l 0x0000 -r 0x0001