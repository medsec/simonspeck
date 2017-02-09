package de.mslab.ciphers;


public class Speck96 extends Speck {
	
	public static final String NAME = "speck96";
	
	public final static int ALPHA = 8;
	public final static int BETA = 3;
	public final static int KEY_SIZE = 96;
	public final static int NUM_ROUNDS = 29;
	public final static long WORD_MASK = 0xFFFF_FFFF_FFFFL;
	public final static int WORD_SIZE = 48;
	
	public Speck96() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, ALPHA, BETA);
	}
	
}
