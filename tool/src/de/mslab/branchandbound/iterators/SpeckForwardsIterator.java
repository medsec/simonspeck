package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;
import de.mslab.util.ArrayUtil;
import de.mslab.util.MathUtil;

public class SpeckForwardsIterator extends SpeckIterator {
	
	public SpeckForwardsIterator(Cipher cipher) {
		super(cipher);
	}
	
	public BranchIterator clone() {
		SpeckForwardsIterator copy = new SpeckForwardsIterator(null);
		copyProperties(copy);
		return copy;
	}
	
	protected ActiveBits createActiveBitsFromCurrent() {
		// Note that left contains also those bits which normally would have 
		// canceled in the addition. We have left them in {@link #precomputeFirstStepsOfTheRound()}
		// since otherwise, we would mistakenly consider not all carry bits. 
		final int[] indices = mapCurrentToIndices(leftActiveBits, current);
		
		double probabilityLog = determineProbabilityLogFromIndices(leftActiveBits, indices)
			+ inputDifference.probabilityLog;
		
		long currentLeft = mapIndicesToActiveBits(left, leftActiveBits, indices);
		
		// Here, we need to remove those active bits which would have canceled
		// from the addition.
		currentLeft ^= cancelingBits;
		long currentRight = currentLeft ^ right;
		
		return new ActiveBits(
			currentLeft, currentRight, probabilityLog, inputDifference.numPaths, 
			inputDifference.left, inputDifference.right
		);
	}
	
	protected long determineNumElements() {
		int[] left = ArrayUtil.activeBitsToArray(inputDifference.left);
		int[] right = ArrayUtil.activeBitsToArray(inputDifference.right);
		
		// We must determine which active bits are set in the left part
		// after the addition
		left = MathUtil.rotateRight(left, leftRotation, wordSize);
		left = ArrayUtil.mergeWithoutCancel(left, right);
		
		/**
		 * Old limitation. Since some active bits may be at locations where 
		 * only a few carry bits will occur, this is being replaced by a more
		 * general limitation at function end.
		*/
		
		/**
		 * Example:
		 * left = { 12, 14 }
		 * 
		 * Then, there are the solutions (depending on carry bits):
		 * - 14,12 (-1-1), 14,13,12 (-1-2), 13,12 (-1-3), 15,13,12 (-1-3),
		 * - 15,14,12 (-1-1), 15,14,13,12 (-1-2), 15,13,12 (-1-3), 13,12 (-1-3)
		 * with probabilities in parentheses. 
		 * One can see that we have 2 (solutions for 14) * 4 (solutions for 12) 
		 * solutions = (16 - 14) * (16 - 12) = 8.
		 */
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
		left = inputDifference.left; // 4,3,2,0
		right = inputDifference.right; // 14,11
		
		left = MathUtil.rotateRight(left, leftRotation, wordSize); // 13,12,11,9
		long originalLeft = left + 0; // 13,12,11,9
		
		/**
		 * We must not cancel the active bits from both left and right
		 * to consider all carry bits (as correct).
		 */
		left |= right; // 14,13,12,11,9
		
		/**
		 * However, we have to cancel them afterwards in {@link #createActiveBitsFromCurrent}.
		 * To do so, we need to store the bits which have to be canceled here. 
		 * We obtain these by calling mergeAndCancel twice (this is not an error).
		 */
		cancelingBits = originalLeft ^ right ^ left; // 11
		
		right = MathUtil.rotateLeft(right, rightRotation, wordSize); // 13,0
		leftActiveBits = ArrayUtil.activeBitsToArray(left); // 14,13,12,11,9
	}
	
}
