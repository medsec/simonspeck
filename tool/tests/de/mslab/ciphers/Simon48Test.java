package de.mslab.ciphers;
import static org.junit.Assert.assertArrayEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.ciphers.Simon;
import de.mslab.ciphers.Simon48;

public class Simon48Test {
	
	private static Simon cipher;
	private static final long[] key = { 0x1a1918, 0x121110, 0x0a0908, 0x020100 };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Simon48();
		cipher.setKey(key);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cipher = null;
	}

	@Test
	public void testDecrypt() {
		final long[] ciphertext = { 0x6e06a5, 0xacf156 };
		final long[] expectedPlaintext = { 0x726963, 0x20646e };
		final long[] plaintext = cipher.decrypt(ciphertext);
		assertArrayEquals(expectedPlaintext, plaintext);
	}
	
	@Test
	public void testEncrypt() {
		final long[] plaintext = { 0x726963, 0x20646e };
		final long[] expectedCiphertext = { 0x6e06a5, 0xacf156 };
		final long[] ciphertext = cipher.encrypt(plaintext);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
}
