package de.mslab.ciphers;

public class Simon96 extends Simon {
	
	public static final String NAME = "simon96";
	
	public final static int KEY_SIZE = 144;
	public final static int NUM_ROUNDS = 54;
	public final static long WORD_MASK = 0xFFFF_FFFFFFFFL;
	public final static int WORD_SIZE = 48;
	
	public Simon96() {
		super(KEY_SIZE, NUM_ROUNDS, WORD_SIZE, WORD_MASK, Z_3);
	}
	
}
