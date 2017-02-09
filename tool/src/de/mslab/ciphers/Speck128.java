package de.mslab.ciphers;


public class Speck128 extends Speck {
	
	public static final String NAME = "speck128";
	
	public final static int ALPHA = 8;
	public final static int BETA = 3;
	public final static int KEY_SIZE = 256;
	public final static int NUM_ROUNDS = 34;
	public final static long WORD_MASK = 0xFFFF_FFFF_FFFF_FFFFL;
	public final static int WORD_SIZE = 64;
	
	public Speck128() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, ALPHA, BETA);
	}
	
}
