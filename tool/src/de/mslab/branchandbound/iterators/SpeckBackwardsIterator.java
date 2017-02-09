package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;
import de.mslab.util.ArrayUtil;
import de.mslab.util.MathUtil;

public class SpeckBackwardsIterator extends SpeckIterator {
	
	public SpeckBackwardsIterator(Cipher cipher) {
		super(cipher);
	}
	
	public BranchIterator clone() {
		SpeckBackwardsIterator copy = new SpeckBackwardsIterator(null);
		copyProperties(copy);
		return copy;
	}
	
	protected ActiveBits createActiveBitsFromCurrent() {
		/** 
		 * Note that left contains also those bits which normally would have 
		 * canceled in the addition. We have left them in {@link #precomputeFirstStepsOfTheRound()}
		 * since otherwise, we would mistakenly consider not all carry bits. 
		 */
		final int[] indices = mapCurrentToIndices(leftActiveBits, current);
		double probabilityLog = determineProbabilityLogFromIndices(leftActiveBits, indices)
			+ inputDifference.probabilityLog;
		
		long currentLeft = mapIndicesToActiveBits(left, leftActiveBits, indices);
		long currentRight = right + 0;
		
		/**
		 * Still, we need to remove those active bits which would have canceled
		 * from the addition.  
		 */
		currentLeft ^= cancelingBits;
		currentLeft = MathUtil.rotateLeft(currentLeft, leftRotation, wordSize);
		
		return new ActiveBits(currentLeft, currentRight, probabilityLog, inputDifference.numPaths, 
			inputDifference.left, inputDifference.right);
	}
	
	protected long determineNumElements() {
		int[] left = ArrayUtil.activeBitsToArray(inputDifference.left);
		int[] right = ArrayUtil.activeBitsToArray(inputDifference.right);
		
		// We must determine which active bits are set in the left part
		// after the addition
		right = ArrayUtil.mergeAndCancel(left, right);
		right = MathUtil.rotateRight(right, rightRotation, wordSize);
		left = ArrayUtil.mergeWithoutCancel(left, right);
		
		long product = 1L;
		
		for (int i = 0; i < left.length; i++) {
			product *= determineMaxNumCarryBits(left[i]);
		}
		
		if (product > maxNumElements) {
			return maxNumElements;
		} else {
			return product;			
		}
	}
	
	protected void precomputeFirstStepsOfRound() {
		left = inputDifference.left; // 12,11,10,9
		right = inputDifference.right; // 13,12,11,10,9,0
		
		right ^= left; // 13,0
		right = MathUtil.rotateRight(right, rightRotation, wordSize); // 14,11
		long originalLeft = left + 0; // 12,11,10,9
		
		/**
		 * We must not cancel the active bits from both left and right
		 * to consider all carry bits (as correct).
		 */
		left |= right; // 14,12,11,10,9
		
		leftActiveBits = ArrayUtil.activeBitsToArray(left); // 14,12,11,10,9
		
		/**
		 * However, we have to cancel them afterwards in {@link #createActiveBitsFromCurrent}.
		 * To do so, we need to store the bits which have to be canceled here. 
		 * We obtain these by calling mergeAndCancel twice (this is not an error).
		 */
		cancelingBits = originalLeft ^ right ^ left; // 11
	}
	
}
