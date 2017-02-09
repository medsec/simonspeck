package de.mslab.ciphers;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.ciphers.Simon;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.helpers.KeyIterator;

public class Simon32Test {
	
	private static Simon cipher;
	private static final long[] key = { 0x1918, 0x1110, 0x0908, 0x0100 };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Simon32();
		cipher.setKey(key);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cipher = null;
	}
	
	@Test
	public void testDecrypt() {
		final long[] ciphertext = { 0xc69b, 0xe9bb };
		final long[] expectedPlaintext = { 0x6565, 0x6877 };
		final long[] plaintext = cipher.decrypt(ciphertext);
		assertArrayEquals(expectedPlaintext, plaintext);
	}
	
	@Test
	public void testEncrypt() {
		final long[] plaintext = { 0x6565, 0x6877 };
		final long[] expectedCiphertext = { 0xc69b, 0xe9bb };
		final long[] ciphertext = cipher.encrypt(plaintext);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
	@Test
	public void testEncryptRound() {
		final long[] plaintext = { 0x8100, 0x0000 };
		final long key = 0x0020;
		final long[] expectedCiphertext = { 0x0423, 0x8100 };
		final long[] ciphertext = encryptRound(plaintext, key);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
	@Test
	public void testPartialEncrypt() {
		final long numPairs = 1 << 18;
		long[] plaintext, ciphertext, expectedCiphertext;

		for (long i = 0; i < numPairs; i++) {
			plaintext = new long[]{ 
				(long)(Math.random() * 0xFFFF) & 0xFFFF, 
				(long)(Math.random() * 0xFFFF) & 0xFFFF
			};
			ciphertext = cipher.encrypt(3, 9, plaintext);
			ciphertext = cipher.encrypt(10, 11, ciphertext);
			expectedCiphertext = cipher.encrypt(3, 11, plaintext);
			assertArrayEquals(expectedCiphertext, ciphertext);
		}
	}
	
	@Test
	public void testKeyIterator() {
		final long keyMask = 0x000A;
		final KeyIterator iterator = new KeyIterator(keyMask);
		final long[] expectedKeyMasks = {
			0b00000000000000000000000000000000, 
			0b00000000000000000000000000000010, 
			0b00000000000000000000000000001000, 
			0b00000000000000000000000000001010
		};
		int i = 0;
		
		while (iterator.hasNext()) {
			assertEquals(expectedKeyMasks[i], iterator.next());
			i++;
		}
	}
	
	@Test
	public void testRotateRight() {
		long value = 0b1011_0010_1011_0011L;
		long expected = 0b011_1011_0010_1011_0L;
		long actual = rotateRight(value, 3, 16);
		assertEquals(expected, actual);
	}
	
	/**
	 * Encrypts a single round of SIMON.
	 * @param roundInput
	 * @param roundKey
	 * @return The state before the round.
	 */
	private long[] encryptRound(long[] roundInput, long roundKey) {
		long L = roundInput[0];
		long R = roundInput[1];
		
		long x = ((L & 0x7FFF) << 1) | ((L >> 15) & 0x0001);
		long y = ((L & 0x00FF) << 8) | ((L >>  8) & 0x00FF);
		long z = ((L & 0x3FFF) << 2) | ((L >> 14) & 0x0003);
		R ^= z;
		R ^= x & y;
		R ^= roundKey;
		
		return new long[]{ R, L };
	}
	
	private long rotateRight(long value, int rotation, int wordSize) {
		return ((value & ((1L << rotation) - 1)) << (wordSize - rotation))
			| ((value >> rotation) & ((1L << (wordSize - rotation)) - 1));
	}
	
}
