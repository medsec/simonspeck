package de.mslab.ciphers;


public class Simon32 extends Simon {

	public static final String NAME = "simon32";
	
	public final static int KEY_SIZE = 64;
	public final static int NUM_ROUNDS = 32;
	public final static long WORD_MASK = 0xFFFFL;
	public final static int WORD_SIZE = 16;
	
	public Simon32() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, Z_0);
	}
	
}
