package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;
import de.mslab.util.MathUtil;

public abstract class SimonIterator extends BranchIterator {
	
	protected long[] activeBitMasks;
	protected double outputProbabilityLog;
	protected int numActiveBits;
	protected int numActiveMasks;
	
	protected SimonIterator(Cipher cipher) {
		super(cipher);
	}
	
	public void setActiveBits(ActiveBits inputDifference) {
		super.setActiveBits(inputDifference);
		precomputeFirstStepsOfRound();
	}
	
	protected void copyProperties(SimonIterator copy) {
		copy.constSequence = constSequence;
		copy.keySize = keySize;
		copy.numRounds = numRounds;
		copy.stateSize = stateSize;
		copy.wordSize = wordSize;
	}
	
	protected abstract long determineNumElements();
	
	protected long internalDetermineNumElements() {
		final long numElements = 1 << this.numActiveMasks;
		
		return (numElements <= this.maxNumElements) ?
			numElements :
			this.maxNumElements;
	}
	
	protected abstract void precomputeFirstStepsOfRound();
	
	protected long internalPrecomputeFirstStepsOfRound(long roundLeft, long roundRight) {
		this.activeBitMasks = new long[this.numActiveMasks];
		
		long mask = 1L;
		
		for (int i = 0, j = 0; i < wordSize; i++) {
			if ((roundLeft & mask) != 0) {
				this.activeBitMasks[j++] = MathUtil.rotateLeft(mask, 1, wordSize);
				this.activeBitMasks[j++] = MathUtil.rotateLeft(mask, 8, wordSize);
			}
			
			mask <<= 1;
		}
		
		return roundRight ^ MathUtil.rotateLeft(roundLeft, 2, wordSize);
	}
	
}
