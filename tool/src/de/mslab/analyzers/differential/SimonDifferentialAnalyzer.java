package de.mslab.analyzers.differential;

import java.util.Arrays;

import de.mslab.ciphers.Cipher;
import de.mslab.util.ArrayUtil;
import de.mslab.util.MathUtil;


public class SimonDifferentialAnalyzer extends DifferentialAnalyzer {
	
	public SimonDifferentialAnalyzer(Cipher cipher) {
		super(cipher);
	}
	
	public void propagateDifferentialBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		int logarithmProbability = 0;
		int[] z, temp;
		
		for (int round = fromRound; round >= 1; round--) {
			temp = rightActiveBits;
			rightActiveBits = leftActiveBits;
			leftActiveBits = temp;
			logarithmProbability = 2 * leftActiveBits.length;
			
			z = MathUtil.rotateLeft(leftActiveBits, 2, wordSize);
			rightActiveBits = ArrayUtil.mergeAndCancel(z, rightActiveBits);
			
			log(
				"After round: " + round + " L_i: " + Arrays.toString(leftActiveBits)
				+ " R_i: " + Arrays.toString(rightActiveBits)
				+ " prob: " + logarithmProbability
			);
		}
	}
	
	public void propagateDifferentialForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		int logarithmProbability = 0;
		int[] z, temp;
		
		for (int round = fromRound + 1; round <= numRounds; round++) {
			z = MathUtil.rotateLeft(leftActiveBits, 2, wordSize);
			temp = ArrayUtil.mergeAndCancel(z, rightActiveBits);
			
			rightActiveBits = leftActiveBits;
			logarithmProbability = 2 * leftActiveBits.length;
			leftActiveBits = temp;
			
			log(
				"After round: " + round + " L_i: " + Arrays.toString(leftActiveBits)
				+ " R_i: " + Arrays.toString(rightActiveBits)
				+ " prob: " + logarithmProbability
			);
		}
	}
	
	public void foobar(int fromRound, int[] leftKnownActiveBits, int[] rightKnownActiveBits) {
		int[] knownX, knownY, knownZ, temp;
		int[] potX, potY, potZ;
		int[] leftPotentiallyActiveBits = {}, rightPotentiallyActiveBits = {};
		
		for (int round = fromRound; round >= 1; round--) {
			temp = rightKnownActiveBits;
			rightKnownActiveBits = leftKnownActiveBits;
			leftKnownActiveBits = temp;
			
			temp = leftPotentiallyActiveBits;
			leftPotentiallyActiveBits = rightPotentiallyActiveBits;
			rightPotentiallyActiveBits = temp;
			
			knownZ = MathUtil.rotateLeft(leftKnownActiveBits, 2, wordSize);
			rightKnownActiveBits = ArrayUtil.mergeAndCancel(rightKnownActiveBits, knownZ);
			
			potZ = MathUtil.rotateLeft(leftPotentiallyActiveBits, 2, wordSize);
			rightPotentiallyActiveBits = ArrayUtil.mergeWithoutCancel(rightPotentiallyActiveBits, potZ);
			
			knownX = MathUtil.rotateLeft(leftKnownActiveBits, 1, wordSize);
			knownY = MathUtil.rotateLeft(leftKnownActiveBits, 8, wordSize);
			potX = MathUtil.rotateLeft(leftPotentiallyActiveBits, 1, wordSize);
			potY = MathUtil.rotateLeft(leftPotentiallyActiveBits, 8, wordSize);
			
			potX = ArrayUtil.mergeWithoutCancel(potX, knownX);
			potY = ArrayUtil.mergeWithoutCancel(potY, knownY);
			
			rightPotentiallyActiveBits = ArrayUtil.mergeWithoutCancel(rightPotentiallyActiveBits, potX);
			rightPotentiallyActiveBits = ArrayUtil.mergeWithoutCancel(rightPotentiallyActiveBits, potY);
			
			rightKnownActiveBits = cancel(rightKnownActiveBits, rightPotentiallyActiveBits);
			
			log("After round: " + (round - 1));
			log("  L: " + Arrays.toString(leftKnownActiveBits) + " " + Arrays.toString(leftPotentiallyActiveBits));
			log("  R: " + Arrays.toString(rightKnownActiveBits) + " " + Arrays.toString(rightPotentiallyActiveBits));
		}
	}
	
	/**
	 * 
	 * @param fromRound Starting round.
	 * @param leftActiveBits Left part of the state after Round {@code startRound}.
	 * @param rightActiveBits Right part of the state after Round {@code startRound}.
	 */
	public void propagateFurtherForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits, 
		int[] leftUnknownBits, int[] rightUnknownBits) {
		
		int[] x_active, y_active, z_active, temp;
		int[] x_unknown, y_unknown, z_unknown;
		int[] keyBits, leftActiveKeyBits, rightActiveKeyBits, leftUnknownKeyBits, rightUnknownKeyBits;
		
		for (int round = fromRound + 1; round <= numRounds; round++) {
			leftActiveKeyBits = MathUtil.rotateLeft(rightActiveBits, 7, wordSize);
			leftUnknownKeyBits = MathUtil.rotateLeft(rightUnknownBits, 7, wordSize);
			rightActiveKeyBits = MathUtil.rotateRight(rightActiveBits, 7, wordSize);
			rightUnknownKeyBits = MathUtil.rotateRight(rightUnknownBits, 7, wordSize);
			
			rightActiveKeyBits = ArrayUtil.mergeWithoutCancel(rightActiveKeyBits, rightUnknownKeyBits);
			keyBits = ArrayUtil.mergeWithoutCancel(leftActiveKeyBits, leftUnknownKeyBits);
			keyBits = ArrayUtil.mergeWithoutCancel(keyBits, rightActiveKeyBits);
			
			x_active = MathUtil.rotateLeft(leftActiveBits, 1, wordSize);
			y_active = MathUtil.rotateLeft(leftActiveBits, 8, wordSize);
			z_active = MathUtil.rotateLeft(leftActiveBits, 2, wordSize);
			
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, x_active);
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, y_active);
			rightActiveBits = ArrayUtil.mergeAndCancel(rightActiveBits, z_active);
			
			x_unknown = MathUtil.rotateLeft(leftUnknownBits, 1, wordSize);
			y_unknown = MathUtil.rotateLeft(leftUnknownBits, 8, wordSize);
			z_unknown = MathUtil.rotateLeft(leftUnknownBits, 2, wordSize);
			
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, x_unknown);
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, y_unknown);
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, z_unknown);
			rightActiveBits = cancel(rightActiveBits, rightUnknownBits);
			
			temp = rightActiveBits;
			rightActiveBits = leftActiveBits;
			leftActiveBits = temp;
			
			temp = rightUnknownBits;
			rightUnknownBits = leftUnknownBits;
			leftUnknownBits = temp;
			
			Arrays.sort(leftActiveBits);
			Arrays.sort(rightActiveBits);
			Arrays.sort(leftUnknownBits);
			Arrays.sort(rightUnknownBits);
			Arrays.sort(keyBits);
			
			log("After round: " + round + " |L|: " + leftActiveBits.length + " |R|: " + rightActiveBits.length
				+ " |L_{unknown}|: " + leftUnknownBits.length + " |R_{unknown}|: " + rightUnknownBits.length);
			log("  L_{active}:  " + Arrays.toString(leftActiveBits));
			log("  L_{unknown}: " + Arrays.toString(leftUnknownBits));
			log("  R_{active}:  " + Arrays.toString(rightActiveBits));
			log("  R_{unknown}: " + Arrays.toString(rightUnknownBits));
			log("  Key " + (round - 1) + ":      " + Arrays.toString(keyBits) + " |K|: " + keyBits.length);
		}
	}
	
	public void propagateFurtherBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits, 
		int[] leftUnknownBits, int[] rightUnknownBits) {
		
		int[] x_active, y_active, z_active, temp;
		int[] x_unknown, y_unknown, z_unknown;
		int[] keyBits, leftActiveKeyBits, rightActiveKeyBits, leftUnknownKeyBits, rightUnknownKeyBits;
		
		for (int round = fromRound; round >= 1; round--) {
			temp = rightActiveBits;
			rightActiveBits = leftActiveBits;
			leftActiveBits = temp;
			
			temp = rightUnknownBits;
			rightUnknownBits = leftUnknownBits;
			leftUnknownBits = temp;
			
			leftActiveKeyBits = MathUtil.rotateLeft(rightActiveBits, 7, wordSize);
			leftUnknownKeyBits = MathUtil.rotateLeft(rightUnknownBits, 7, wordSize);
			rightActiveKeyBits = MathUtil.rotateRight(rightActiveBits, 7, wordSize);
			rightUnknownKeyBits = MathUtil.rotateRight(rightUnknownBits, 7, wordSize);
			
			rightActiveKeyBits = ArrayUtil.mergeWithoutCancel(rightActiveKeyBits, rightUnknownKeyBits);
			keyBits = ArrayUtil.mergeWithoutCancel(leftActiveKeyBits, leftUnknownKeyBits);
			keyBits = ArrayUtil.mergeWithoutCancel(keyBits, rightActiveKeyBits);
			
			x_active = MathUtil.rotateLeft(leftActiveBits, 1, wordSize);
			y_active = MathUtil.rotateLeft(leftActiveBits, 8, wordSize);
			z_active = MathUtil.rotateLeft(leftActiveBits, 2, wordSize);
			
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, x_active);
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, y_active);
			rightActiveBits = ArrayUtil.mergeAndCancel(rightActiveBits, z_active);
			
			x_unknown = MathUtil.rotateLeft(leftUnknownBits, 1, wordSize);
			y_unknown = MathUtil.rotateLeft(leftUnknownBits, 8, wordSize);
			z_unknown = MathUtil.rotateLeft(leftUnknownBits, 2, wordSize);
			
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, x_unknown);
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, y_unknown);
			rightUnknownBits = ArrayUtil.mergeWithoutCancel(rightUnknownBits, z_unknown);
			rightActiveBits = cancel(rightActiveBits, rightUnknownBits);
			
			Arrays.sort(leftActiveBits);
			Arrays.sort(rightActiveBits);
			Arrays.sort(leftUnknownBits);
			Arrays.sort(rightUnknownBits);
			Arrays.sort(keyBits);
			
			log("After round: " + (round - 1)
				+ " |L_{unknown}|: " + leftUnknownBits.length 
				+ " |R_{unknown}|: " + rightUnknownBits.length);
			log("  L_{active}:  " + Arrays.toString(leftActiveBits));
			log("  L_{unknown}: " + Arrays.toString(leftUnknownBits));
			log("  R_{active}:  " + Arrays.toString(rightActiveBits));
			log("  R_{unknown}: " + Arrays.toString(rightUnknownBits));
			log("  Key " + (round - 1) + ":      " 
				+ Arrays.toString(keyBits) + " |K|: " + keyBits.length);
		}
	}

	private int[] cancel(int[] rightKnownActiveBits, int[] rightPotentiallyActiveBits) {
		int[] result = new int[rightKnownActiveBits.length];
		int k = 0;
		boolean isUnknown;
		
		for (int i = 0; i < rightKnownActiveBits.length; i++) {
			isUnknown = false;
			
			for (int j = 0; j < rightPotentiallyActiveBits.length; j++) {
				if (rightKnownActiveBits[i] == rightPotentiallyActiveBits[j]) {
					isUnknown = true;
					break;
				}
			}
			
			if (!isUnknown) {
				result[k++] = rightKnownActiveBits[i];
			}
		}
		
		return Arrays.copyOfRange(result, 0, k);
	}
	
}


