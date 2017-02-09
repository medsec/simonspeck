package de.mslab.ciphers;

public class Simon64 extends Simon {

	public static final String NAME = "simon64";
	
	public final static int KEY_SIZE = 128;
	public final static int NUM_ROUNDS = 44;
	public final static long WORD_MASK = 0xFFFFFFFFL;
	public final static int WORD_SIZE = 32;
	
	public Simon64() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, Z_3);
	}
	
}
