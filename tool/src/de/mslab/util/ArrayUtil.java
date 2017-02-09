package de.mslab.util;


public class ArrayUtil {
	
	public static int[] mergeAndCancel(int[] left, int[] right) {
		if (left == null || left.length == 0) {
			return right;
		} else if (right == null || right.length == 0) {
			return left;
		}
		
		long bitSet = 0L;
		long mask;
		int i, j;
		
		for (i = 0; i < left.length; i++) {
			mask = 1L << left[i];
			bitSet ^= mask;
		}
		
		for (i = 0; i < right.length; i++) {
			mask = 1L << right[i];
			bitSet ^= mask;
		}
		
		final int numActiveBits = Long.bitCount(bitSet);
		final int lowestOne = Long.numberOfTrailingZeros(bitSet);
		final int highestOne = 64 - Long.numberOfLeadingZeros(bitSet);
		int[] result = new int[numActiveBits];
		
		for (i = lowestOne, j = 0; i < highestOne; i++) {
			mask = 1L << i;
			
			if ((bitSet & mask) != 0) {
				result[j] = i;
				j++;
			}
		}
		
		return result;
	}

	public static int[] mergeWithoutCancel(int[] left, int[] right) {
		if (left == null || left.length == 0) {
			return right;
		} else if (right == null || right.length == 0) {
			return left;
		}
		
		long bitSet = 0L;
		long mask;
		int i, j;
		
		for (i = 0; i < left.length; i++) {
			mask = 1L << left[i];
			bitSet |= mask;
		}
		
		for (i = 0; i < right.length; i++) {
			mask = 1L << right[i];
			bitSet |= mask;
		}
		
		final int numActiveBits = Long.bitCount(bitSet);
		final int lowestOne = Long.numberOfTrailingZeros(bitSet);
		final int highestOne = 64 - Long.numberOfLeadingZeros(bitSet);
		int[] result = new int[numActiveBits];
		
		for (i = lowestOne, j = 0; i < highestOne; i++) {
			mask = 1L << i;
			
			if ((bitSet & mask) != 0) {
				result[j] = i;
				j++;
			}
		}
		
		return result;
	}
	
	public static int[] activeBitsToArray(long value) {
		final int highestActiveBit = Long.SIZE - Long.numberOfLeadingZeros(value);
		final int numActiveBits = Long.bitCount(value);
		
		long mask = 1L;
		int[] activeBits = new int[numActiveBits];
		
		for (int i = 0, j = 0; i < highestActiveBit; i++) {
			if ((value & mask) != 0) {
				activeBits[j] = i;
				j++;
			}
			
			mask <<= 1;
		}
		
		return activeBits;
	}
	
	public static long activeBitsToLong(int[] value) {
		long result = 0L;
		
		for (int i = 0; i < value.length; i++) {
			result |= 1L << value[i];
		}
		
		return result;
	}
	
}
