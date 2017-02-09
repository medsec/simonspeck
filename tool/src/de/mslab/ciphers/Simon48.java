package de.mslab.ciphers;

public class Simon48 extends Simon {

	public static final String NAME = "simon48";
	
	public final static int KEY_SIZE = 96;
	public final static int NUM_ROUNDS = 36;
	public final static long WORD_MASK = 0xFFFFFFL;
	public final static int WORD_SIZE = 24;
	
	public Simon48() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, Z_1);
	}
	
}
