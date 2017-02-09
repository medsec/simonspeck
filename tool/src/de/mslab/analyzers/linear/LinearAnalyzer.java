package de.mslab.analyzers.linear;

import de.mslab.ciphers.Cipher;

public abstract class LinearAnalyzer {

	protected long[] constSequence;
	protected int numRounds;
	protected int wordSize;
	protected int stateSize;
	protected int keySize;

	protected LinearAnalyzer(Cipher cipher) {
		this.constSequence = cipher.getConstSequence();
		this.keySize = cipher.getKeySize();
		this.numRounds = cipher.getNumRounds();
		this.stateSize = cipher.getStateSize();
		this.wordSize = cipher.getWordSize();
	}
	
	public abstract void propagateBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits);
	public abstract void propagateForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits);

	protected void log(Object message) {
		System.out.println(message.toString());
	}
	
}
