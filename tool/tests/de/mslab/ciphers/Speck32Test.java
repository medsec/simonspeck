package de.mslab.ciphers;

import static org.junit.Assert.assertArrayEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class Speck32Test {
	
	private static Speck cipher;
	private static final long[] key = { 0x1918, 0x1110, 0x0908, 0x0100 };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Speck32();
		cipher.setKey(key);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cipher = null;
	}
	
	@Test
	public void testDecrypt() {
		final long[] ciphertext = { 0xa868, 0x42f2 };
		final long[] expectedPlaintext = { 0x6574, 0x694c };
		final long[] plaintext = cipher.decrypt(ciphertext);
		assertArrayEquals(expectedPlaintext, plaintext);
	}
	
	@Test
	public void testEncrypt() {
		final long[] plaintext = { 0x6574, 0x694c };
		final long[] expectedCiphertext = { 0xa868, 0x42f2 };
		final long[] ciphertext = cipher.encrypt(plaintext);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
}
