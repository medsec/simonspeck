package de.mslab.analyzers.differential;

import de.mslab.ciphers.Cipher;

public abstract class DifferentialAnalyzer {
	
	protected long[] constSequence;
	protected int numRounds;
	protected int wordSize;
	protected int stateSize;
	protected int keySize;

	protected DifferentialAnalyzer(Cipher cipher) {
		this.constSequence = cipher.getConstSequence();
		this.keySize = cipher.getKeySize();
		this.numRounds = cipher.getNumRounds();
		this.stateSize = cipher.getStateSize();
		this.wordSize = cipher.getWordSize();
	}
	
	public void findAllSolutionsBackwards(int round, int[] leftActiveBits, int[] rightActiveBits) {
		
	}
	
	public void findAllSolutionsForwards(int round, int[] leftActiveBits, int[] rightActiveBits) {
		
	}
	
	public void propagateDifferentialBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		
	}
	
	public void propagateDifferentialForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		
	}
	
	public void propagateFurtherBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		propagateFurtherBackwards(fromRound, leftActiveBits, rightActiveBits, new int[]{}, new int[]{});
	}
	
	public abstract void propagateFurtherBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits, 
		int[] leftUnknownBits, int[] rightUnknownBits);
	
	public void propagateFurtherForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		propagateFurtherForwards(fromRound, leftActiveBits, rightActiveBits, new int[]{}, new int[]{});
	}
	
	public abstract void propagateFurtherForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits, 
		int[] leftUnknownBits, int[] rightUnknownBits);
	
	protected void log(Object message) {
		System.out.println(message.toString());
	}
	
}
