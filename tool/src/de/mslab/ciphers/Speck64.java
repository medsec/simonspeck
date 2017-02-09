package de.mslab.ciphers;


public class Speck64 extends Speck {
	
	public static final String NAME = "speck64";
	
	public final static int ALPHA = 8;
	public final static int BETA = 3;
	public final static int KEY_SIZE = 128;
	public final static int NUM_ROUNDS = 27;
	public final static long WORD_MASK = 0xFFFF_FFFFL;
	public final static int WORD_SIZE = 32;
	
	public Speck64() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, ALPHA, BETA);
	}
	
}
