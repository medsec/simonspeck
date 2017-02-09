package de.mslab.ciphers;

public class Simon128 extends Simon {

	public static final String NAME = "simon128";
	
	public final static int KEY_SIZE = 256;
	public final static int NUM_ROUNDS = 72;
	public final static long WORD_MASK = 0xFFFFFFFF_FFFFFFFFL;
	public final static int WORD_SIZE = 64;
	
	public Simon128() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, Z_4);
	}
	
}
