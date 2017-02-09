package de.mslab.branches;

import de.mslab.ciphers.Cipher;
import de.mslab.util.MathUtil;

public class SimonBranchesTest {
	
	protected Cipher cipher;
	protected long[] key;
	protected long[][] expectedDifferences;

	protected static long[][] createDifferences(long[][][] activeBits) {
		long[][] expectedDifferences = new long[activeBits.length][2];
		
		for (int i = 0; i < activeBits.length; i++) {
			for (int j = 0; j < 2; j++) {
				expectedDifferences[i][j] = MathUtil.createDifference(activeBits[i][j]);
			} 
		}
		
		return expectedDifferences;
	}
	
	protected SimonBranchesTest(Cipher cipher, long[] key, long[][] expectedDifferences) {
		this.key = key;
		this.cipher = cipher;
		this.cipher.setKey(key);
		this.expectedDifferences = expectedDifferences;
	}
	
	protected void investigateRounds(long numPairs, int fromRound, int toRound) {
		final long[] inputDifference = expectedDifferences[fromRound - 1];
		final long[] outputDifference = expectedDifferences[toRound];
		
		long[] plaintext = { 0, 0 }, plaintext_ = { 0, 0 };
		long[] ciphertext = { 0, 0 }, ciphertext_ = { 0, 0 };
		long numPairsFound = 0;
		
		for (long i = 0; i < numPairs; i++) {
			plaintext[0] = (long)(Math.random() * 0xFFFFFFFFL);
			plaintext[1] = (long)(Math.random() * 0xFFFFFFFFL);
			
			plaintext_[0] = plaintext[0] ^ inputDifference[0];
			plaintext_[1] = plaintext[1] ^ inputDifference[1];
			
			ciphertext = cipher.encrypt(fromRound, toRound, plaintext);
			ciphertext_ = cipher.encrypt(fromRound, toRound, plaintext_);
			
			if ((ciphertext[0] ^ ciphertext_[0]) == outputDifference[0]
				&& (ciphertext[1] ^ ciphertext_[1]) == outputDifference[1]) {
				numPairsFound++;
				searchAndLogBranches(plaintext, plaintext_, fromRound, toRound);
			}
		}
		
		log("[" + fromRound + ".." + toRound + "]: found " + numPairsFound + " pairs");
	}
	
	protected void searchAndLogBranches(long[] plaintext, long[] plaintext_, int fromRound, int toRound) {
		long[] difference = { 0, 0 };
		boolean hasBranches = false;
		
		for (int round = fromRound; round <= toRound; round++) {
			plaintext = cipher.encrypt(round, round, plaintext);
			plaintext_ = cipher.encrypt(round, round, plaintext_);
			difference[0] = plaintext[0] ^ plaintext_[0];
			difference[1] = plaintext[1] ^ plaintext_[1];
			
			if (difference[0] != expectedDifferences[round][0]
				|| difference[1] != expectedDifferences[round][1]) {
				logDifference(round, difference);
				hasBranches = true;
			}
		}
		
		if (hasBranches) {
			log("--");
		}
	}
	
	protected void log(Object message) {
		System.out.println(message.toString());
	}
	
	protected void logDifference(int round, long[] difference) {
		log(
			"Round: " + round + " \\Delta_{" 
			+ getActiveBits(difference[0], cipher.getWordSize()) + "}"
			+ ", \\Delta_{" 
			+ getActiveBits(difference[1], cipher.getWordSize()) + "}"
		);
	}

	private String getActiveBits(long difference, int wordSize) {
		String s = "";
		long mask = 1L;
		boolean hasActiveBits = false;
		
		for (int i = 0; i < wordSize; i++) {
			if ((difference & mask) != 0) {
				if (hasActiveBits) {
					s += ",";
				}
				
				s += i;
				hasActiveBits = true;
			}
			
			mask <<= 1;
		}
		
		return s;
	}
	
}

