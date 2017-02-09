package de.mslab.analyzers.linear;

import de.mslab.ciphers.Cipher;
import de.mslab.util.MathUtil;
import de.mslab.util.StringUtil;

public class SimonLinearAnalyzer extends LinearAnalyzer {
	
	public SimonLinearAnalyzer(Cipher cipher) {
		super(cipher);
	}
	
	public void propagateBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		/**
		 * Example:
		 * - r = 11
		 * - L^{11} = {8,4}
		 * - R^{11} = {6}
		 * 
		 * Expected output: 
		 * - L^{10} = {6,2}
		 * - R^{10} = {8,4}
		 */
		
		int numLinearApproximations = 0;
		long left = MathUtil.createDifference(leftActiveBits);
		long right = MathUtil.createDifference(rightActiveBits);
		long temp, rot2;
		
		for (int round = fromRound; round >= 1; round--) {
			temp = left;
			rot2 = MathUtil.rotateRight(left, 2, wordSize);
			left = right | rot2;
			right = temp;
			numLinearApproximations += Long.bitCount(right);
			
			log(String.format(
				"Round %2d, L: %s, R: %s, approx.: %2d", 
				round - 1, 
				StringUtil.toNBitBinary(left, wordSize), 
				StringUtil.toNBitBinary(right, wordSize), 
				numLinearApproximations
			));
		}
	}
	
	public void propagateForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits) {
		/**
		 * Example:
		 * - r = 16
		 * - L^{16} = {6}
		 * - R^{16} = {8,4}
		 * 
		 * Expected output: 
		 * - L^{17} = {8,4}
		 * - R^{17} = {6,2}
		 * - Pr = (1/4)^2
		 */
		
		int numLinearApproximations = 0;
		long left = MathUtil.createDifference(leftActiveBits);
		long right = MathUtil.createDifference(rightActiveBits);
		long tempRight, rot2;
		
		for (int round = fromRound + 1; round <= numRounds; round++) {
			tempRight = right;
			rot2 = MathUtil.rotateRight(right, 2, wordSize);
			right = left | rot2;
			left = tempRight;
			numLinearApproximations += Long.bitCount(left);
			
			log(String.format(
				"Round %2d, L: %s, R: %s, approx.: %2d", 
				round, 
				StringUtil.toNBitBinary(left, wordSize), 
				StringUtil.toNBitBinary(right, wordSize), 
				numLinearApproximations
			));
		}
	}
	
}
