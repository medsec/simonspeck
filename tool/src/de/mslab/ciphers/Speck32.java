package de.mslab.ciphers;


public class Speck32 extends Speck {
	
	public static final String NAME = "speck32";
	
	public final static int ALPHA = 7;
	public final static int BETA = 2;
	public final static int KEY_SIZE = 64;
	public final static int NUM_ROUNDS = 22;
	public final static long WORD_MASK = 0xFFFFL;
	public final static int WORD_SIZE = 16;
	
	public Speck32() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, ALPHA, BETA);
	}
	
}
