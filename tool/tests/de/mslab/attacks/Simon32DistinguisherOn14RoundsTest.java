package de.mslab.attacks;

import org.junit.Test;
import static org.junit.Assert.*;

import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.helpers.Batch;
import de.mslab.ciphers.helpers.RoundKeyCandidate;
import de.mslab.ciphers.helpers.TextPair;
import de.mslab.util.StringUtil;

public class Simon32DistinguisherOn14RoundsTest {
	
	private static final Cipher cipher = new Simon32();
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
		{ { 0x1101, 0x4000 } }, // ((0,8,12), (14)) => 10
		{ { 0x0404, 0x1101 } }, // ((2,10), (12,8,0))
		{ { 0x0111, 0x0404 } }, // ((0,4,8), (10,2))
		{ { 0x0040, 0x0111 } }, // ((6), (0,4,8))
		{ { } }, // ((0,4), (6))
	};
	private static final long[][][] differenceMasks = {
		null, null, null, null, null, // After round 0..4
		null, null, null, null, null, // 5..9
		null, 
		{ { 0xFDFD, 0xFFFF } }, // 11
		{ { 0xF1F1, 0xFDFD } }, // 12
		{ { 0xC0C0, 0xF1F1 } }, // 13
		{ { 0x0000, 0xC0C0 } }  // 14
	};
	
	private static final long[] key = { 0x65AC, 0x701C, 0x801A, 0xBB86 }; 
	private static final long[] keyMasks = {
		-1,   
		0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, // Round 1 .. 5
		0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, // 6 .. 10
		0xFFFF, // 11
		0xFFFF, // 12
		0b0000_1111_0000_1111, // 13
		0b1011_1111_1001_1111, // 14
	};
	private static final long numChosenTextPairs = 1L << 16;
	private static final long numKeys = 1L << 21;
	
	private Batch batch = new Batch();
	long numCorrect = 0;
	
	/**
	 * Runs the differential attack against reduced SIMON32/64.
	 */
	@Test
	public void runAttack() {
		setKey();
		findCorrectPairs(batch);
		findCorrectKey(batch);
	}
	
	private void findCorrectPairs(Batch batch) {
		log("Searching for correct pairs");
		
		final long[] inputDifference = expectedOutputDifferences[5][0]; 
		final long[] expectedDifferenceAfterRound11 = expectedOutputDifferences[11][0];
		final long[] expectedDifferenceAfterRound13 = expectedOutputDifferences[13][0];
		final long[] differenceMaskAfterRound11 = differenceMasks[11][0];
		final long[] differenceMaskAfterRound13 = differenceMasks[13][0];
		
		long[] plaintext, plaintext_;
		long[] ciphertext, ciphertext_;
		long[] state = {0,0}, state_ = {0,0};
		long[] differenceAfterRound11 = {0,0};
		long[] differenceAfterRound13 = {0,0};
		
		boolean isCorrect;
		
		for (long counter = 0; counter < numChosenTextPairs; counter++) {
			plaintext = generatePlaintext(counter);
			plaintext_ = new long[]{0,0};
			plaintext_[0] = plaintext[0] ^ inputDifference[0];
			plaintext_[1] = plaintext[1] ^ inputDifference[1];
			
			// Encrypt partially first to spy whether it is a correct pair =)
			ciphertext = cipher.encrypt(6, 11, plaintext);
			ciphertext_ = cipher.encrypt(6, 11, plaintext_);
			differenceAfterRound11[0] = (ciphertext[0] ^ ciphertext_[0]) & differenceMaskAfterRound11[0];
			differenceAfterRound11[1] = (ciphertext[1] ^ ciphertext_[1]) & differenceMaskAfterRound11[1];
			
			isCorrect = (differenceAfterRound11[0] == expectedDifferenceAfterRound11[0])
				&& (differenceAfterRound11[1] == expectedDifferenceAfterRound11[1]);
			
			if (isCorrect) {
				numCorrect++;
			}
			
			// Encrypt full
			ciphertext = cipher.encrypt(12, 14, ciphertext);
			ciphertext_ = cipher.encrypt(12, 14, ciphertext_);
			
			// 1st filter
			state = decryptRound(ciphertext, 0L);
			state_ = decryptRound(ciphertext_, 0L);
			differenceAfterRound13[0] = (state[0] ^ state_[0]) & differenceMaskAfterRound13[0]; 
			differenceAfterRound13[1] = (state[1] ^ state_[1]) & differenceMaskAfterRound13[1];
			
			if (differenceAfterRound13[0] == expectedDifferenceAfterRound13[0]
				&& differenceAfterRound13[1] == expectedDifferenceAfterRound13[1]) {
				batch.add(new TextPair(isCorrect, ciphertext, ciphertext_));
			}
		}
		
		log("#correct: " + numCorrect);
		log("#found:   " + batch.size());
	}
	
	private void findCorrectKey(Batch batch) {
		final long[] differenceMaskAfterRound11 = differenceMasks[11][0];
		final long[] expectedDifferenceAfterRound11 = expectedOutputDifferences[11][0];
		final long correctRoundKey13 = cipher.getRoundKey(13) & keyMasks[13];
		final long correctRoundKey14 = cipher.getRoundKey(14) & keyMasks[14];
		
		long[] state = {0,0}, state_ = {0,0};
		long[] ciphertextDifference = {0,0};
		
		RoundKeyCandidate key;
		RoundKeyCandidate bestRoundKeyCandidate = null;
		long roundKey;
		long maxNumCorrectPairs = -1;
		long numMatchesForCurrentKey = 0;
		long numKeysWithMaxNumCorrectPairs = 0;
		
		for (int i = 0; i < numKeys; i++) {
			key = generateKey(i);
			numMatchesForCurrentKey = 0;
			
			for (TextPair textPair : batch) {
				state[0] = textPair.p[0] + 0;
				state[1] = textPair.p[1] + 0;
				state_[0] = textPair.p_[0] + 0;
				state_[1] = textPair.p_[1] + 0;
				
				for (int round = 14; round >= 12; round--) {
					roundKey = key.keys[round];
					state = decryptRound(state, roundKey);
					state_ = decryptRound(state_, roundKey);
				}
				
				ciphertextDifference[0] = (state[0] ^ state_[0]) & differenceMaskAfterRound11[0]; 
				ciphertextDifference[1] = (state[1] ^ state_[1]) & differenceMaskAfterRound11[1];
				
				if (ciphertextDifference[0] == expectedDifferenceAfterRound11[0]
					&& ciphertextDifference[1] == expectedDifferenceAfterRound11[1]) {
					numMatchesForCurrentKey++;
				}
			}
			
			if (numMatchesForCurrentKey == maxNumCorrectPairs) {
				numKeysWithMaxNumCorrectPairs++;
				log("#Keys with max: " + numKeysWithMaxNumCorrectPairs);
			} else if (numMatchesForCurrentKey > maxNumCorrectPairs) {
				maxNumCorrectPairs = numMatchesForCurrentKey;
				numKeysWithMaxNumCorrectPairs = 1;
				bestRoundKeyCandidate = key;
				log(numMatchesForCurrentKey);
			}
			
			if (key.keys[13] == correctRoundKey13
				&& key.keys[14] == correctRoundKey14) {
				log("#pairs for correct key: " + numMatchesForCurrentKey + " #max: " + maxNumCorrectPairs
					+ " #keys with max: " + numKeysWithMaxNumCorrectPairs);
			}
			
			if (((i & 0xFFFF) == 0) && (i != 0)) {
				log("i: " + i + "/" + numKeys);
			}
		}
		
		final long foundRoundKey13 = bestRoundKeyCandidate.keys[13] & keyMasks[13];
		final long foundRoundKey14 = bestRoundKeyCandidate.keys[14] & keyMasks[14];
		
		log("#Keys with max: " + numKeysWithMaxNumCorrectPairs);
		log("Found   13: " + StringUtil.to16BitBinary(foundRoundKey13));
		log("Correct 13: " + StringUtil.to16BitBinary(correctRoundKey13));
		log("Found   14: " + StringUtil.to16BitBinary(foundRoundKey14));
		log("Correct 14: " + StringUtil.to16BitBinary(correctRoundKey14));
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
	
	@Test
	public void testDecrypt() {
		final int fromRound = 12;
		final int toRound = 14;
		
		setKey();
		
		long[] plaintext = { 0xa9a5, 0xbc17 };
		long[] ciphertext = cipher.encrypt(fromRound, toRound, plaintext);
		long[] result = ciphertext;
		
		for (int round = toRound; round >= fromRound; round--) {
			result = decryptRound(result, cipher.getRoundKey(round));
		}
		
		assertEquals(plaintext[0], result[0]);
		assertEquals(plaintext[1], result[1]);
	}
	
	private RoundKeyCandidate generateKey(int i) {
		// 21 bits
		RoundKeyCandidate key = new RoundKeyCandidate(); 
		
		// 8 bits => 0-3, 8-11
		key.keys[13] = ((i & 0b0_0001_1110_0000_0000_0000) >> 13) 
			| ((i & 0b1_1110_0000_0000_0000_0000) >> 9);
		
		// 13 bits => 0-4, 7-13, 15
		key.keys[14] = (i & 0b1_1111) 
			| ((i & 0b0000_1111_1110_0000) << 2)
			| ((i & 0b0001_0000_0000_0000) << 3);
		
		return key;
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


