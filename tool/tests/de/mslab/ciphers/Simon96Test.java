package de.mslab.ciphers;
import static org.junit.Assert.assertArrayEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.ciphers.Simon;
import de.mslab.ciphers.Simon96;

public class Simon96Test {
	
	private static Simon cipher;
	private static final long[] key = { 0x1514_13121110L, 0x0d0c_0b0a0908L, 0x0504_03020100L };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Simon96();
		cipher.setKey(key);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cipher = null;
	}
	
	@Test
	public void testDecrypt() {
		final long[] expectedPlaintext = new long[]{ 0x7461_68742074L, 0x7375_6420666fL };
		final long[] ciphertext = new long[]{ 0xecad_1c6c451eL, 0x3f59_c5db1ae9L };
		final long[] plaintext = cipher.decrypt(ciphertext);
		assertArrayEquals(expectedPlaintext, plaintext);
	}
	
	@Test
	public void testEncrypt() {
		final long[] plaintext = new long[]{ 0x7461_68742074L, 0x7375_6420666fL };
		final long[] expectedCiphertext = new long[]{ 0xecad_1c6c451eL, 0x3f59_c5db1ae9L };
		final long[] ciphertext = cipher.encrypt(plaintext);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
}
