package de.mslab.analyzers.differential;

import java.util.Arrays;

import de.mslab.ciphers.Cipher;
import de.mslab.util.ArrayUtil;
import de.mslab.util.MathUtil;

@SuppressWarnings("unused")
public class SpeckDifferentialAnalyzer extends DifferentialAnalyzer {
	
	public SpeckDifferentialAnalyzer(Cipher cipher) {
		super(cipher);
	}
	
	public void propagateDifferentialBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		int logarithmProbability = 0;
		final int RIGHT_ROTATION = (wordSize == 16) ? 2 : 3;
		final int LEFT_ROTATION = (wordSize == 16) ? 7 : 8;
		
		for (int round = fromRound; round >= 1; round--) {
			rightActiveBits = ArrayUtil.mergeAndCancel(leftActiveBits, rightActiveBits);
			rightActiveBits = MathUtil.rotateRight(rightActiveBits, RIGHT_ROTATION, wordSize);
			logarithmProbability = calculateBackwardProbability(ArrayUtil.mergeWithoutCancel(leftActiveBits, rightActiveBits));
			leftActiveBits = ArrayUtil.mergeAndCancel(leftActiveBits, rightActiveBits);
			leftActiveBits = MathUtil.rotateLeft(leftActiveBits, LEFT_ROTATION, wordSize);
			
			log(
				"After round: " + round 
				+ " L_i: " + Arrays.toString(leftActiveBits)
				+ " R_i: " + Arrays.toString(rightActiveBits)
				+ " Pr: " + logarithmProbability
			);
		}
	}
	
	public void propagateDifferentialForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		int logarithmProbability = 0;
		final int RIGHT_ROTATION = (wordSize == 16) ? 2 : 3;
		final int LEFT_ROTATION = (wordSize == 16) ? 7 : 8;
		
		for (int round = fromRound + 1; round <= numRounds; round++) {
			leftActiveBits = MathUtil.rotateRight(leftActiveBits, LEFT_ROTATION, wordSize);
			logarithmProbability = calculateForwardProbability(leftActiveBits, rightActiveBits);
			leftActiveBits = ArrayUtil.mergeAndCancel(leftActiveBits, rightActiveBits);
			
			rightActiveBits = MathUtil.rotateLeft(rightActiveBits, RIGHT_ROTATION, wordSize);
			rightActiveBits = ArrayUtil.mergeAndCancel(leftActiveBits, rightActiveBits);
			
			log(
				"After round: " + round 
				+ " L_i: " + Arrays.toString(leftActiveBits)
				+ " R_i: " + Arrays.toString(rightActiveBits)
				+ " Pr: " + logarithmProbability
			);
		}
	}

	public void propagateFurtherBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits, 
		int[] leftUnknownBits, int[] rightUnknownBits) {
		// TODO
		throw new Error("Not yet implemented");
	}
	
	
	public void propagateFurtherForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits, 
		int[] leftUnknownBits, int[] rightUnknownBits) {
		
		// TODO: 
		// Example: (15,8), (15,8,1), -, -
		// Should produce: Pr = -3: (15,9), (15,10,4,3,1)
		// Pr = -3: (15,2), (15,10,3,2,1)
		// Pr = -4: (15,10,9), (...)
		// Pr = -4: (15,3,2), (...)
		// Pr = -4: (15,9,2), (...)
		// ...
		
		final int RIGHT_ROTATION = (wordSize == 16) ? 2 : 3;
		final int LEFT_ROTATION = (wordSize == 16) ? 7 : 8;
		
		for (int round = fromRound + 1; round <= numRounds; round++) {
			leftActiveBits = MathUtil.rotateRight(leftActiveBits, LEFT_ROTATION, wordSize);
			
			// TODO
		}
	}
	
	public void findAllSolutionsForwards(int round, int[] leftActiveBits, int[] rightActiveBits) {
		// TODO: 
		// Example: (15,8), (15,8,1), -, -
		// Should produce: Pr = -3: (15,9), (15,10,4,3,1)
		// Pr = -3: (15,2), (15,10,3,2,1)
		// Pr = -4: (15,10,9), (...)
		// Pr = -4: (15,3,2), (...)
		// Pr = -4: (15,9,2), (...)
		// ...
		
		final int RIGHT_ROTATION = (wordSize == 16) ? 2 : 3;
		final int LEFT_ROTATION = (wordSize == 16) ? 7 : 8;
		
		leftActiveBits = MathUtil.rotateRight(leftActiveBits, LEFT_ROTATION, wordSize);
		final int[] allActiveBits = ArrayUtil.mergeWithoutCancel(leftActiveBits, rightActiveBits);
		Arrays.sort(allActiveBits);
		
		final int numActiveBits = allActiveBits.length;
		final long numSolutions = 1L << numActiveBits;
		int[] activeBitsForCase;
		
		for (long i = 0; i < numSolutions; i++) {
			activeBitsForCase = findActiveBitsForCase(allActiveBits, i);
			foobar(activeBitsForCase);
		}	
	}
	
	private void foobar(int[] activeBits) {
		
	}
	
	private int calculateBackwardProbability(int[] left) {
		int sum = 0;
		
		for (int i = 0; i < left.length; i++) {
			if (left[i] != wordSize - 1) {
				sum++;
			}
		}
		
		return sum;
	}
	
	private int calculateForwardProbability(int[] left, int[] right) {
		int[] both = ArrayUtil.mergeWithoutCancel(left, right);
		int sum = 0;
		
		for (int i = 0; i < both.length; i++) {
			if (both[i] != wordSize - 1) {
				sum++;
			}
		}
		
		return sum;
	}
	
	private int[] findActiveBitsForCase(int[] allActiveBits, long caseIndex) {
		long mask = 1L;
		int numBitsInCase = 0;
		
		// Count first to find out how many active bits we have to store for this case
		for (int j = 0; j < allActiveBits.length; j++) {
			if ((caseIndex & mask) != 0) {
				numBitsInCase++;
			}
			mask <<= 1;
		}
		
		// Know store the active bits for the current case
		final int[] activeBitsForCase = new int[numBitsInCase];
		mask = 1;
		
		for (int i = 0, j = 0; j < activeBitsForCase.length; j++) {
			if ((caseIndex & mask) != 0) {
				activeBitsForCase[i] = allActiveBits[j];
				i++;
			}
			
			mask <<= 1;
		}
		
		return activeBitsForCase;
	}
	
}
