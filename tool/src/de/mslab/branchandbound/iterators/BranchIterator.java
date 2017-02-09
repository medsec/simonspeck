package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;

public abstract class BranchIterator implements Cloneable {
	
	protected ActiveBits inputDifference;
	
	protected int current = 0;
	protected int maxNumSubsequentCarryBits = 6;
	protected long maxNumElements = 1L << 18;
	protected long numElements = 0;
	
	protected long[] constSequence;
	protected int numRounds;
	protected int wordSize;
	protected int stateSize;
	protected int keySize;
	
	protected BranchIterator(Cipher cipher) {
		if (cipher != null) {
			this.constSequence = cipher.getConstSequence();
			this.keySize = cipher.getKeySize();
			this.numRounds = cipher.getNumRounds();
			this.stateSize = cipher.getStateSize();
			this.wordSize = cipher.getWordSize();
		}
	}
	
	public abstract BranchIterator clone();
	
	public long getNumElements() {
		return this.numElements;
	}
	
	public long getMaxNumElements() {
		return this.maxNumElements;
	}
	
	public void setMaxNumElements(long maxNumElements) {
		this.maxNumElements = maxNumElements;
	}
	
	public boolean hasNext() {
		return current < numElements;
	}
	
	public ActiveBits next() {
		if (!hasNext()) {
			throw new Error("No elements left.");
		}
		
		final ActiveBits nextActiveBits = createActiveBitsFromCurrent();
		current++;
		return nextActiveBits;
	}
	
	public void setToFirst() {
		current = 0;
	}
	
	public void setActiveBits(ActiveBits inputDifference) {
		this.inputDifference = inputDifference;
		this.numElements = determineNumElements();
	}
	
	protected abstract ActiveBits createActiveBitsFromCurrent();
	protected abstract long determineNumElements();
	
	protected void log(Object message) {
		System.out.println(message);
	}
	
}
