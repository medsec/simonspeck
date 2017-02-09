package de.mslab.util;

public class MathUtil {

	public static long createDifference(int[] activeBits) {
		long difference = 0;
		long mask = 1;
		
		for (int i = 0; i < activeBits.length; i++) {
			mask = 1L << activeBits[i];
			difference |= mask;
		}
		
		return difference;
	}
	
	public static long createDifference(long[] activeBits) {
		long difference = 0;
		long mask = 1;
		
		for (int i = 0; i < activeBits.length; i++) {
			mask = 1L << activeBits[i];
			difference |= mask;
		}
		
		return difference;
	}
	
	public static int[] rotateLeft(int[] activeBits, int rotateBy, int wordSize) {
		int[] result = new int[activeBits.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = (activeBits[i] + rotateBy) % wordSize;
		}
		
		return result;
	}
	
	public static int[] rotateRight(int[] activeBits, int rotateBy, int wordSize) {
		int[] result = new int[activeBits.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = (activeBits[i] - rotateBy) % wordSize;
			
			if (result[i] < 0) {
				result[i] += wordSize;
			}
		}
		
		return result;
	}
	
	public static long rotateLeft(long value, int rotation, int wordSize) {
		rotation %= wordSize;
		
		if (rotation < 0) {
			rotation += wordSize;
		}
		
		return internalRotateRight(value, wordSize - rotation, wordSize);
	}
	
	public static long rotateRight(long value, int rotation, int wordSize) {
		rotation %= wordSize;
		
		if (rotation < 0) {
			rotation += wordSize;
		}
		
		return internalRotateRight(value, rotation % wordSize, wordSize);
	}

	private static long internalRotateRight(long value, int rotation, int wordSize) {
		return ((value & ((1L << rotation) - 1)) << (wordSize - rotation))
			| ((value >> rotation) & ((1L << (wordSize - rotation)) - 1));
	}
	
}
