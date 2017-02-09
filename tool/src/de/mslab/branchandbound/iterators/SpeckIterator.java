package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;

public abstract class SpeckIterator extends BranchIterator {
	
	protected int leftRotation;
	protected int rightRotation;
	
	protected long left;
	protected int[] leftActiveBits;
	protected long right;
	protected long cancelingBits;
	
	protected SpeckIterator(Cipher cipher) {
		super(cipher);
		leftRotation = (wordSize == 16) ? 7 : 8;
		rightRotation = (wordSize == 16) ? 2 : 3;
	}
	
	public int getMaxNumSubsequentCarryBits() {
		return maxNumSubsequentCarryBits;
	}
	
	public void setActiveBits(ActiveBits inputDifference) {
		super.setActiveBits(inputDifference);
		precomputeFirstStepsOfRound();
	}
	
	public void setMaxNumSubsequentCarryBits(int maxNumSubsequentCarryBits) {
		this.maxNumSubsequentCarryBits = maxNumSubsequentCarryBits;
	}
	
	protected void copyProperties(SpeckIterator copy) {
		copy.constSequence = constSequence;
		copy.keySize = keySize;
		copy.numRounds = numRounds;
		copy.stateSize = stateSize;
		copy.wordSize = wordSize;
		copy.leftRotation = leftRotation;
		copy.rightRotation = rightRotation;
	}
	
	/**
	 * Given an index of an active bits, this helper method determines how
	 * many carry bits should maximally occur by computing wordSize - activeBit.
	 * However, if we would apply this without restriction, we had to consider 
	 * extremely many low-probability output differences that would extremely 
	 * increase the computational effort. Thus, we limit the number of max.
	 * carry bits to {@link #maxNumSubsequentCarryBits}.
	 * @param activeBit
	 * @return
	 */
	protected int determineMaxNumCarryBits(int activeBit) {
		final int numSubsequentCarryBits = wordSize - activeBit;
		
		if (numSubsequentCarryBits <= maxNumSubsequentCarryBits) {
			return numSubsequentCarryBits;
		} else {
			return maxNumSubsequentCarryBits;
		}
	}
	
	protected int determineProbabilityLogFromIndices(int[] left, int[] indices) {
		int sum = 0;
		
		for (int i = 0; i < indices.length; i++) {
			sum += indices[i];
			
			// We only pay for probability of a carry bit if it is not the MSB
			// (the MSB can not produce no further carry bits).
			if (left[i] + indices[i] + 1 < wordSize) {
				sum++;
			}
		}
		
		return sum;
	}
	
	protected int[] mapCurrentToIndices(int[] left, int current) {
		/**
		 * Example:
		 * left = { 9,12,13,14 } => 7 * 4 * 3 * 2 = 168 possibilities
		 * current = 37 or 108 
		 * 
		 * Expected output:
		 * 37 	=> { 2,1,1,0 } ((37 % 7) / 1, (37 % 28) / 7, (37 % 84) / 28, (37 % 168) / 84)
		 * 108	=> { 3,3,0,1 } ((108 % 7) / 1, (108 % 28) / 7, (108 % 84) / 28, (108 % 168) / 84)
		 */
		int product = 1;
		int productBefore;
		final int[] indices = new int[left.length];
		
		for (int i = 0; i < left.length; i++) {
			productBefore = product;
			product *= determineMaxNumCarryBits(left[i]);
			indices[i] = (current % product) / productBefore;
		}
		
		return indices;
	}
	
	protected long mapIndicesToActiveBits(long left, int[] leftActiveBits, int[] indices) {
		/**
		 * Example: 
		 * left = { 9,12,13,14 }
		 * indices = { 3,3,0,1 }
		 * 
		 * This means the bits are set:
		 * 9[3] 	=> 9,10,11,12
		 * 12[3]	=> 12,13,14,15
		 * 13[0]	=> 13
		 * 14[1]	=> 14,15
		 * 
		 * Expected output:
		 * { 9,10,11 } (everything else cancels)
		 */
		long mask;
		long result = left + 0;
		int currentBit;
		int numSubsequentBitsToSet;
		
		for (int i = 0; i < leftActiveBits.length; i++) {
			currentBit = leftActiveBits[i];
			numSubsequentBitsToSet = indices[i];
			
			for (int j = 1; j <= numSubsequentBitsToSet; j++) {
				mask = 1L << (currentBit + j);
				result ^= mask;
			}
		}
		
		return result;
	}
	
	protected abstract void precomputeFirstStepsOfRound();
	
}
