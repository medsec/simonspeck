package de.mslab.ciphers;
import static org.junit.Assert.assertArrayEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.ciphers.Simon;
import de.mslab.ciphers.Simon128;

public class Simon128Test {
	
	private static Simon cipher;
	private static final long[] key = { 
		0x1f1e1d1c_1b1a1918L, 0x17161514_13121110L, 
		0x0f0e0d0c_0b0a0908L, 0x07060504_03020100L 
	};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Simon128();
		cipher.setKey(key);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cipher = null;
	}
	
	@Test
	public void testDecrypt() {
		final long[] expcectedPlaintext = new long[]{ 0x74206e69_206d6f6fL, 0x6d697320_61207369L };
		final long[] ciphertext = new long[]{ 0x8d2b5579_afc8a3a0L, 0x3bf72a87_efe7b868L };
		final long[] plaintext = cipher.decrypt(ciphertext);
		assertArrayEquals(expcectedPlaintext, plaintext);
	}
	
	@Test
	public void testEncrypt() {
		final long[] plaintext = new long[]{ 0x74206e69_206d6f6fL, 0x6d697320_61207369L };
		final long[] expectedCiphertext = new long[]{ 0x8d2b5579_afc8a3a0L, 0x3bf72a87_efe7b868L };
		final long[] ciphertext = cipher.encrypt(plaintext);
		assertArrayEquals(expectedCiphertext, ciphertext);
	}
	
}
