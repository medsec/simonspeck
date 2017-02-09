package de.mslab.analyzers.differential;

import java.util.Arrays;

import de.mslab.ciphers.Cipher;
import de.mslab.util.ArrayUtil;
import de.mslab.util.MathUtil;

public class SimonRotationalAnalyzer extends DifferentialAnalyzer {
	
	private int[][] roundKeyErrors;
	
	public SimonRotationalAnalyzer(Cipher cipher) {
		super(cipher);
	}
	
	public void propagateDifferentialBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		checkIfKeysAreInitialized();
		
	}
	
	public void propagateDifferentialForwards(int fromRound, int[] leftErrors, int[] rightErrors) {
		checkIfKeysAreInitialized();
		
		
		int probabilityLog = 0;
		int[] z, temp;
		
		for (int round = fromRound + 1; round <= numRounds; round++) {
			z = MathUtil.rotateLeft(leftErrors, 2, wordSize);
			rightErrors = ArrayUtil.mergeAndCancel(z, rightErrors);
			rightErrors = ArrayUtil.mergeAndCancel(rightErrors, roundKeyErrors[round]);
			
			temp = rightErrors;
			rightErrors = leftErrors;
			probabilityLog = 2 * leftErrors.length;
			leftErrors = temp;
			
			if (leftErrors.length > 0) {
				Arrays.sort(leftErrors);
			}
			if (rightErrors.length > 0) {
				Arrays.sort(rightErrors);
			}
			
			log(
				"After round: " + round + " L_i: " + Arrays.toString(leftErrors)
				+ " R_i: " + Arrays.toString(rightErrors)
				+ " prob: " + probabilityLog
			);
		}
	}
	
	public void propagateFurtherBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits,
		int[] leftUnknownBits, int[] rightUnknownBits) {
		checkIfKeysAreInitialized();
		// TODO Auto-generated method stub
	}
	
	public void propagateFurtherForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits,
		int[] leftUnknownBits, int[] rightUnknownBits) {
		checkIfKeysAreInitialized();
		// TODO Auto-generated method stub
	}
	
	public void initializeSecretKeyErrors(int[][] secretKeyErrors) {
		final int numKeyWords = keySize / wordSize;
		
		assert (secretKeyErrors != null)
			&& (secretKeyErrors.length > 0)
			&& (secretKeyErrors.length == (numKeyWords + 1));
		
		this.roundKeyErrors = new int[this.numRounds + 1][];
		
		for (int round = 1; round <= numKeyWords; round++) {
			roundKeyErrors[round] = secretKeyErrors[round];
		}
		
		/*for (int round = numKeyWords + 1; round <= numRounds; round++) {
			// TODO
		}*/
	}
	
	private void checkIfKeysAreInitialized() {
		if(roundKeyErrors == null) {
			throw new Error("You must call initializeSecretKeyErrors() to initialize " +
				"the secret key before you .");
		}
	}
	
}
