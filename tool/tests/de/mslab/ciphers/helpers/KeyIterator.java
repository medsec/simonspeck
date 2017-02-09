package de.mslab.ciphers.helpers;

import java.util.NoSuchElementException;

public class KeyIterator {
	
	private long[] activeBitMasks;
	private long[] indexMasks;
	private int current = 0;
	private int numActiveBits = 0;
	private int numElements = 0;
	
	public KeyIterator(long keyMask) {
		this.numActiveBits = determineNumElements(keyMask);
		this.numElements = 1 << numActiveBits;
		this.activeBitMasks = prepareActiveBitMasks(keyMask, numActiveBits);
		this.indexMasks = prepareIndexMasks(numActiveBits);
	}
	
	public int getNumElements() {
		return numElements;
	}

	public int getNumElementsRemaining() {
		return numElements - current;
	}
	
	public boolean hasNext() {
		return current < numElements;
	}
	
	public long next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		return createKey(current++);
	}
	
	public void reset() {
		current = 0;
	}
	
	private long createKey(int current) {
		long key = 0;
		
		for (int i = 0; i < numActiveBits; i++) {
			if ((current & indexMasks[i]) != 0) {
				key |= activeBitMasks[i];
			}
		}
		
		return key;
	}
	
	private int determineNumElements(long keyMask) {
		return Long.bitCount(keyMask);
	}
	
	private long[] prepareActiveBitMasks(long keyMask, int numActiveBits) {
		long currentMask;
		long activeBitMasks[] = new long[numActiveBits];
		
		for (int i = 0, j = 0; i < 32; i++) {
			currentMask = 1 << i;
			
			if ((keyMask & currentMask) != 0) {
				activeBitMasks[j++] = currentMask;
			}
		}
		
		return activeBitMasks;
	}
	
	private long[] prepareIndexMasks(int numActiveBits) {
		long[] indexMasks = new long[numActiveBits];
		
		for (int i = 0; i < numActiveBits; i++) {
			indexMasks[i] = 1 << i;
		}
		
		return indexMasks;
	}
	
}
