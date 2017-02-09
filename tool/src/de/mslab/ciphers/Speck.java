package de.mslab.ciphers;

import java.util.Arrays;

import de.mslab.util.MathUtil;

public class Speck extends Cipher {
	
	protected int alpha;
	protected int beta;
	
	protected long[] roundKeys;
	protected long wordMask;
	
	protected Speck(int keySize, int numRounds, int wordSize, long wordMask, int alpha, int beta) {
		this.keySize = keySize;
		this.numKeyWords = keySize / wordSize;
		this.numRounds = numRounds;
		this.wordSize = wordSize;
		this.wordMask = wordMask;
		this.stateSize = 2 * wordSize;
		this.alpha = alpha;
		this.beta = beta;
	}
	
	public long[] decrypt(int fromRound, int toRound, long[] state) {
		checkRounds(fromRound, toRound);
		checkState(state);
		state = Arrays.copyOf(state, 2);
		
		for (int round = toRound; round >= fromRound; round--) {
			decryptRound(state, getRoundKey(round));
		}
		
		return state;
	}
	
	public long[] encrypt(int fromRound, int toRound, long[] state) {
		checkRounds(fromRound, toRound);
		checkState(state);
		state = Arrays.copyOf(state, 2);
		
		for (int round = fromRound; round <= toRound; round++) {
			encryptRound(state, getRoundKey(round));
		}
		
		return state;
	}
	
	protected void decryptRound(long[] state, long roundKey) {
		state[1] ^= state[0];
		state[0] ^= roundKey;
		state[1] = MathUtil.rotateRight(state[1], beta, wordSize);
		state[0] -= state[1];
		
		if (state[0] < 0) {
			state[0] += wordMask + 1;
		}
		
		state[0] = MathUtil.rotateLeft(state[0], alpha, wordSize);
	}
	
	protected void encryptRound(long[] state, long roundKey) {
		state[0] = MathUtil.rotateRight(state[0], alpha, wordSize);
		state[0] = (state[0] + state[1]) & wordMask;
		state[1] = MathUtil.rotateLeft(state[1], beta, wordSize);
		state[0] ^= roundKey;
		state[1] ^= state[0];
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
	public void setKey(long[] keyInput) {
		checkKey(keyInput);
		fillSecretKey(keyInput);
		processKey();
	}
	
	private void fillSecretKey(long[] keyInput) {
		roundKeys = new long[numRounds + numKeyWords];
		
		for (int i = 0, j = keyInput.length - 1; i < keyInput.length; i++, j--) {
			roundKeys[i] = keyInput[j] & wordMask;
		}
	}
	
	private void processKey() {
		long[] state = { roundKeys[1], roundKeys[0] };
		
		for (int round = 1; round <= numRounds; round++) {
			state[0] = roundKeys[round];
			encryptRound(state, (round - 1));
			roundKeys[round + numKeyWords - 1] = state[0]; // The ell word
			roundKeys[round] = state[1]; // The round key
		}
	}
	
}
