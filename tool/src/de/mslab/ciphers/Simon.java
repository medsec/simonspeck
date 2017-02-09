package de.mslab.ciphers;

import de.mslab.util.MathUtil;



public abstract class Simon extends Cipher {
	
	public final static long[] Z_0 = {
		0b1111_1010_0010_0101_0110_0001_1100_1101_1111_0100_0100_1010_1100_0011_1001_10L
	};
	public final static long[] Z_1 = {
		0b1000_1110_1111_1001_0011_0000_1011_0101_0001_1101_1111_0010_0110_0001_0110_10L
	};
	public final static long[] Z_2 = {
		0b1010_1111_0111_0000_0011_0100_1001_1000_1010_0001_0001_1111_1001_0110_1100_11L, 
		0b1010_1111_0111_0000_0011_0100_1001_1000_1010_0001_0001_1111_1001_0110_1100_11L
	};
	public final static long[] Z_3 = {
		0b1101_1011_1010_1100_0110_0101_1110_0000_0100_1000_1010_0111_0011_0100_0011_11L, 
		0b1101_1011_1010_1100_0110_0101_1110_0000_0100_1000_1010_0111_0011_0100_0011_11L 
	};
	public final static long[] Z_4 = {
		0b1101_0001_1110_0110_1011_0110_0010_0000_0101_1100_0011_0010_1001_0011_1011_11L, 
		0b1101_0001_1110_0110_1011_0110_0010_0000_0101_1100_0011_0010_1001_0011_1011_11L
	};
	
	protected long[] constSequence;
	protected long[] roundKeys;
	protected long wordMask;
	protected long c;
	
	protected Simon(int keySize, int numRounds, int wordSize, long wordMask, long[] constSequence) {
		this.keySize = keySize;
		this.numKeyWords = keySize / wordSize;
		this.numRounds = numRounds;
		this.wordSize = wordSize;
		this.wordMask = wordMask;
		this.c = wordMask ^ 0b11;
		this.stateSize = 2 * wordSize;
		this.constSequence = constSequence;
	}
	
	public long[] decrypt(int fromRound, int toRound, long[] state) {
		checkRounds(fromRound, toRound);
		
		final long[] leftAndRight = inputToLeftRight(state);
		assert leftAndRight.length == 2;
		
		long left = leftAndRight[0];
		long right = leftAndRight[1];
		
		long x,y,z;
		
		final int rotationAmountBy1 = wordSize - 1;
		final int rotationAmountBy2 = wordSize - 2;
		final int rotationAmountBy8 = wordSize - 8;
		
		for (int round = toRound; round >= fromRound; round--) {
			x = left;
			left = right;
			right = x;
			
			x = (((left << 1) & wordMask) | ((left >> rotationAmountBy1) & 0x0001)) & wordMask; 
			y = (((left << 8) & wordMask) | ((left >> rotationAmountBy8) & 0x00FF)) & wordMask;
			z = (((left << 2) & wordMask) | ((left >> rotationAmountBy2) & 0x0003)) & wordMask;
			
			right ^= x & y;
			right ^= z;
			right ^= roundKeys[round - 1];
		}
		
		return leftRightToOutput(left, right);
	}
	
	public long[] encrypt(int fromRound, int toRound, long[] state) {
		checkRounds(fromRound, toRound);
		
		final long[] leftAndRight = inputToLeftRight(state);
		assert leftAndRight.length == 2;
		
		long left = leftAndRight[0];
		long right = leftAndRight[1];
		
		// log("round: " + fromRound); 
		// log("L: " + StringUtil.to48BitBinary(left));
		// log("R: " + StringUtil.to48BitBinary(right));
		
		long x,y,z;
		
		final int rotationAmountBy1 = wordSize - 1;
		final int rotationAmountBy2 = wordSize - 2;
		final int rotationAmountBy8 = wordSize - 8;
		
		for (int round = fromRound; round <= toRound; round++) {
			x = (((left << 1) & wordMask) | ((left >> rotationAmountBy1) & 0x0001)) & wordMask; 
			y = (((left << 8) & wordMask) | ((left >> rotationAmountBy8) & 0x00FF)) & wordMask;
			z = (((left << 2) & wordMask) | ((left >> rotationAmountBy2) & 0x0003)) & wordMask;
			
			// log("x: " + StringUtil.to48BitBinary(x));
			// log("y: " + StringUtil.to48BitBinary(y));
			// log("z: " + StringUtil.to48BitBinary(z));
			// log("k: " + StringUtil.to48BitBinary(keys[round - 1]));
			
			right ^= x & y;
			right ^= z;
			right ^= roundKeys[round - 1];
			
			x = left;
			left = right;
			right = x;
			
			// log("round: " + round); 
			// log("L: " + StringUtil.to48BitBinary(left));
			// log("R: " + StringUtil.to48BitBinary(right));
		}
		
		return leftRightToOutput(left, right);
	}
	
	public long getRoundKey(int round) {
		return roundKeys[round - 1];
	}
	
	/**
	 * Sets the secret key and performs the key schedule. 
	 * Note that the key is expected to be in words, i.e., for Simon32-64, 
	 * we expect an array of four longs, where the least-significant 16 bits
	 * of each long represents a part of the key.
	 */
	public void setKey(long[] key) {
		assert key != null && key.length >= 2 && key.length <= 4;
		
		fillSecretKey(key);
		processKey(key.length);
	}
	
	private void fillSecretKey(long[] key) {
		roundKeys = new long[numRounds];
		
		for (int i = 0, j = key.length - 1; i < key.length; i++, j--) {
			roundKeys[i] = key[j] & wordMask;
		}
	}
	
	private void processKey(int fromRound) {
		int constBit = 0;
		
		for (int round = fromRound; round < numRounds; round++) {
			if (fromRound == 2) {
				setKeyWithTwoWords(round);
			} else if (fromRound == 3) {
				setKeyWithThreeWords(round);
			} else if (fromRound == 4) {
				setKeyWithFourWords(round);
			}
			
			if (round - fromRound < 62) {
				constBit = 61 - (round - fromRound);
				roundKeys[round] ^= (constSequence[0] >> constBit) & 1;
			} else {
				constBit = 61 - (round - 62 - fromRound);
				roundKeys[round] ^= (constSequence[1] >> constBit) & 1;
			}
		}
	}
	
	private void setKeyWithTwoWords(int round) {
		long x = MathUtil.rotateRight(roundKeys[round - 1], 3, wordSize);
		roundKeys[round] = x ^ roundKeys[round - 2];
		
		x ^= MathUtil.rotateRight(x, 1, wordSize);
		roundKeys[round] ^= x;
		roundKeys[round] ^= c;
	}
	
	private void setKeyWithThreeWords(int round) {
		long x = MathUtil.rotateRight(roundKeys[round - 1], 3, wordSize);
		roundKeys[round] = x ^ roundKeys[round - 3];
		
		x = MathUtil.rotateRight(x, 1, wordSize);
		roundKeys[round] ^= x;
		roundKeys[round] ^= c;
	}
	
	private void setKeyWithFourWords(int round) {
		long x = MathUtil.rotateRight(roundKeys[round - 1], 3, wordSize);
		x ^= roundKeys[round - 3];
		roundKeys[round] = roundKeys[round - 4] ^ x;
		
		x = MathUtil.rotateRight(x, 1, wordSize);
		roundKeys[round] ^= x;
		roundKeys[round] ^= c;
	}
	
	private long[] leftRightToOutput(long left, long right) {
		return new long[]{ left & wordMask, right & wordMask };
	}
	
	private long[] inputToLeftRight(long[] state) {
		assert state.length == 2;
		return state;
	}
	
}
