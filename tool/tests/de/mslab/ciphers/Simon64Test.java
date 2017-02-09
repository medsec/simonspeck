package de.mslab.ciphers;
import static org.junit.Assert.assertArrayEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.ciphers.Simon;
import de.mslab.ciphers.Simon64;

public class Simon64Test {
	
	private static Simon cipher;
	private static final long[] key = { 0x1b1a1918L, 0x13121110L, 0x0b0a0908L, 0x03020100L };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Simon64();
		cipher.setKey(key);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cipher = null;
	}
	
	@Test
	public void testDecrypt() {
		final long[] ciphertext = { 0x44c8fc20L, 0xb9dfa07aL };
		final long[] expectedPlaintext = { 0x656b696cL, 0x20646e75L };
		final long[] plaintext = cipher.decrypt(ciphertext);
		assertArrayEquals(expectedPlaintext, plaintext);
	}
	
	@Test
	public void testEncrypt() {
		final long[] plaintext = { 0x656b696cL, 0x20646e75L };
		final long[] expectedCiphertext = { 0x44c8fc20, 0xb9dfa07aL };
		final long[] ciphertext = cipher.encrypt(plaintext);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
}
