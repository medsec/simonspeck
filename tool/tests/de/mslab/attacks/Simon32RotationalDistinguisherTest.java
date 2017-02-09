package de.mslab.attacks;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.helpers.Batch;
import de.mslab.ciphers.helpers.RoundKeyCandidate;

public class Simon32RotationalDistinguisherTest {
	
	private static final Cipher cipher = new Simon32();
	private static final long[] key = { 0x65AC, 0x701C, 0x801A, 0xBB86 }; 
	private static final long numChosenTextPairs = 1L << 18;
	private static final int numChosenKeys = 1 << 7;
	/**
	 * Expected output differences of the state after round {@code i}.
	 */
	private static final long[][][] expectedOutputDifferences = {
		{ { 0x1101, 0x0404 } }, // ((0,8,12), (2,10))
		{ { 0x4000, 0x1101 } }, // ((14), (0,8,12))
		{ { 0x1100, 0x4000 } }, // ((8,12), (14))
		{ { 0x0400, 0x1100 }, { 0x0401, 0x1100 }, { 0x0600, 0x1100 }, { 0x0601, 0x1100 } }, 
		// ((10), (8,12)) or ((10,0), (8,12)) or ((10,9), (8,12)) or ((10,9,0), (8,12))
		{ { 0x0100, 0x0400 }, { 0x0100, 0x0401 }, { 0x0100, 0x0600 }, { 0x0100, 0x0601 } }, 
		// ((8), (10)) or ((8), (10,0)) or ((8), (10,9)) or ((8), (10,9,0))
		{ { 0x0000, 0x0100 } }, // ((-), (8)) 
		{ { 0x0100, 0x0000 } }, // ((8), (-))
		{ { 0x0400, 0x0100 }, { 0x0401, 0x0100 }, { 0x0600, 0x0100 }, { 0x0601, 0x0100 } }, 
		// ((10), (8)) or ((10,0), (8)) or ((10,9), (8)) or ((10,9,0), (8)) 
		{ { 0x1100, 0x0400 }, { 0x1100, 0x0401 }, { 0x1100, 0x0600 }, { 0x1100, 0x0601 } }, 
		// ((8,12), (10)) or ((8,12), (10,0)) or ((8,12), (10,9)) or ((8,12), (10,9,0))
		{ { 0x4000, 0x1100 } }, // ((14), (8,12))
		{ { 0x1101, 0x4000 } },  // ((0,8,12), (14))
		{ { 0x0404, 0x1101 } }, // ((2,10), (12,8,0))
		{ { 0x0010, 0x0404 } } // ((4), (10,2))
	};
	private static final long[] keyMasks = {
		-1,   
		0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, // K^0 .. K^4
		0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, // K^5 .. K^9
		0xFFFF, 0xFFFF, // K^{10}, K^{11}
		0b0000_1010_0000_1010, // K^{12}
		0b1011_1110_1001_1110, // K^{13}
		0xFFFF // K^{14}
	};
	
	private Batch batch = new Batch();
	private List<RoundKeyCandidate> keys = new ArrayList<RoundKeyCandidate>();
	
	/**
	 * Runs the differential attack against reduced SIMON32/64.
	 */
	@Test
	public void runAttack() {
		setKey();
		fillKeys();
		findCorrectPairs(batch, keys);
	}
	
	private void fillKeys() {
		RoundKeyCandidate key;
		
		for (int i = 0; i < numChosenKeys - 1; i++) {
			key = new RoundKeyCandidate();
			
			for (int round = 12; round <= 15; round++) {
				key.keys[round] = (long)(Math.random() * 0xFFFF) & keyMasks[round];
			}
			
			keys.add(key);
		}
		
		key = new RoundKeyCandidate();
		
		for (int round = 12; round <= 15; round++) {
			key.keys[round] = cipher.getRoundKey(round) & keyMasks[round];
		}
		
		keys.add(key);
	}
	
	private void findCorrectPairs(Batch batch, List<RoundKeyCandidate> keys) {
		log("Searching for correct pairs");
		
		final long[] inputDifference = expectedOutputDifferences[5][0]; 
		final long[] expectedDifferenceAfterRound11 = expectedOutputDifferences[11][0];
		
		long[] plaintext, plaintext_;
		long[] ciphertext, ciphertext_;
		long[] state = {0,0}, state_ = {0,0};
		long[] ciphertextDifference = {0,0};
		long[] differenceAfterRound11 = {0,0};
		
		long numCorrect = 0;
		long[] correctPairsForKey = new long[numChosenKeys];
		boolean isCorrect;
		
		RoundKeyCandidate key;
		long roundKey;
		
		for (long counter = 0; counter < numChosenTextPairs; counter++) {
			plaintext = generatePlaintext(counter);
			plaintext_ = new long[]{0,0};
			plaintext_[0] = plaintext[0] ^ inputDifference[0];
			plaintext_[1] = plaintext[1] ^ inputDifference[1];
			
			ciphertext = cipher.encrypt(6, 11, plaintext);
			ciphertext_ = cipher.encrypt(6, 11, plaintext_);
			differenceAfterRound11[0] = ciphertext[0] ^ ciphertext_[0];
			differenceAfterRound11[1] = ciphertext[1] ^ ciphertext_[1];
			
			isCorrect = (differenceAfterRound11[0] == expectedDifferenceAfterRound11[0])
				&& (differenceAfterRound11[1] == expectedDifferenceAfterRound11[1]);
			
			if (isCorrect) {
				numCorrect++;
			}
			
			ciphertext = cipher.encrypt(12, 15, ciphertext);
			ciphertext_ = cipher.encrypt(12, 15, ciphertext_);
			
			for (int i = 0; i < numChosenKeys; i++) {
				key = keys.get(i);
				state[0] = ciphertext[0];
				state[1] = ciphertext[1];
				state_[0] = ciphertext_[0];
				state_[1] = ciphertext_[1];
				
				for (int round = 15; round >= 12; round--) {
					roundKey = key.keys[round];
					state = decryptRound(state, roundKey);
					state_ = decryptRound(state_, roundKey);
				}
				
				ciphertextDifference[0] = state[0] ^ state_[0];
				ciphertextDifference[1] = state[1] ^ state_[1];
				
				/*log("diff: " + StringUtil.to16BitBinary(ciphertextDifference[0]) + " " + 
					StringUtil.to16BitBinary(ciphertextDifference[1]));*/
				
				if (ciphertextDifference[0] == expectedDifferenceAfterRound11[0]
					&& ciphertextDifference[1] == expectedDifferenceAfterRound11[1]) {
					correctPairsForKey[i]++;
				}
			}
		}
		
		for (int i = 0; i < numChosenKeys; i++) {
			if (correctPairsForKey[i] > 0) {
				log("key: " + i + " #correct: " + correctPairsForKey[i]);
			}
		}
		
		log("#correct: " + numCorrect);
	}
	
	/**
	 * Inverts a single round of SIMON.
	 * @param roundInput
	 * @param roundKey
	 * @return The state before the round.
	 */
	private long[] decryptRound(long[] roundInput, long roundKey) {
		long L = roundInput[1];
		long R = roundInput[0];
		
		long x = ((L & 0x7FFF) << 1) | ((L >> 15) & 0x0001);
		long y = ((L & 0x00FF) << 8) | ((L >>  8) & 0x00FF);
		long z = ((L & 0x3FFF) << 2) | ((L >> 14) & 0x0003);
		R ^= z;
		R ^= x & y;
		R ^= roundKey;
		
		return new long[]{ L, R };
	}
	
	/**
	 * Maps a plaintext index i (from P_i, P'_i) to the plaintext.
	 * This has to be done since we need to fix a few bits of the plaintext to '0'
	 * to pass the first round for free. 
	 * Currently, these bits are L_{1,3,5,7,9,15}.
	 * @param i Plaintext index
	 * @return Plaintext P. 
	 */
	private long[] generatePlaintext(long i) {
		long right = i & 0xFFFF;
		long left = (long)(Math.random() * 0xFFFF);
		return new long[]{ left, right };
	}
	
	private void log(Object message) {
		System.out.println(message.toString());
	}
	
	private void setKey() {
		cipher.setKey(key);
	}
	
}

