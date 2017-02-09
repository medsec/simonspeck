package de.mslab.ciphers;


public class Speck48 extends Speck {
	
	public static final String NAME = "speck48";
	
	public final static int ALPHA = 8;
	public final static int BETA = 3;
	public final static int KEY_SIZE = 96;
	public final static int NUM_ROUNDS = 23;
	public final static long WORD_MASK = 0xFF_FFFFL;
	public final static int WORD_SIZE = 24;
	
	public Speck48() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, ALPHA, BETA);
	}
	
}
