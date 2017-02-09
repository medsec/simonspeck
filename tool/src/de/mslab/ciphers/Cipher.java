package de.mslab.ciphers;

public abstract class Cipher {
	
	protected long[] constSequence;
	protected int keySize;
	protected int numKeyWords;
	protected int numRounds;
	protected int stateSize;
	protected int wordSize;
	
	public long[] decrypt(long[] state) {
		return decrypt(numRounds, state);
	}
	
	public long[] decrypt(int fromRound, long[] state) {
		return decrypt(1, fromRound, state);
	}
	
	public abstract long[] decrypt(int fromRound, int toRound, long[] state);
	
	public long[] encrypt(long[] state) {
		return encrypt(1, state);
	}
	
	public long[] encrypt(int fromRound, long[] state) {
		return encrypt(fromRound, numRounds, state);
	}
	
	public abstract long[] encrypt(int fromRound, int toRound, long[] state);

	public long[] getConstSequence() {
		return constSequence;
	}
	
	public int getKeySize() {
		return keySize;
	}
	
	public int getNumRounds() {
		return numRounds;
	}
	
	public int getStateSize() {
		return stateSize;
	}
	
	public int getWordSize() {
		return wordSize;
	}
	
	public abstract long getRoundKey(int round);
	
	public void setKey(long[] key) {
		
	}
	
	protected void checkKey(long[] keyInput) {
		if (keyInput == null) {
			throw new Error("Key was null");
		}
		
		if (keyInput.length != (keySize / wordSize)) {
			throw new Error("Invalid key size: expected " + (keySize / wordSize) 
				+ " words but found: " + keyInput + ".");
		}
	}
	
	protected void checkRounds(int fromRound, int toRound) {
		if (fromRound < 1 
			|| fromRound > numRounds
			|| toRound < fromRound 
			|| toRound > numRounds) {
			
			throw new Error("Invalid round range: [" + fromRound 
				+ "," + toRound + "]. Must be in range [1," + numRounds + "]");
		}
	}
	
	protected void checkState(long[] state) {
		if (state.length != 2) {
			throw new Error("State length should be 2.");
		}
	}
	
	protected void log(Object message) {
		System.out.println(message.toString());
	}
	
	protected String toBinary(long value) {
		return String.format("%16s", Long.toBinaryString(value)).replace(' ', '0');
	}

	protected String toBinary(int value) {
		return String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');
	}

}
