package de.mslab.branches;

import org.junit.Test;

import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon32;

public class Simon32PropagateFurtherTest {

	private static final Cipher cipher = new Simon32();
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
		{ { } }, // ((2), (0,4)) => 15
		{ { } }, // ((0), (2))
		{ { } }, // ((-), (0))
		{ { } }, // ((0), (-))
		{ { } }, // ((2), (0))
		{ { 0, 0x0010 } } // ((0,4), (2)) => 20
	};
	private static final long[] key = { 0x65AC, 0x701C, 0x801A, 0xBB86 };
	private static final long numPairs = 1L << 31;
	
	@Test
	public void runAttack() {
		setKey();
		foo();
	}
	
	private void setKey() {
		cipher.setKey(key);
	}
	
	private void foo() {
		final int differenceMask = 0xFF_FFFF;
		final long[] inputDifference = expectedOutputDifferences[1][0];
		long[] plaintext = {0,0}, plaintext_ = {0,0};
		long[] ciphertext = {0,0}, ciphertext_ = {0,0};
		long diff = 0;
		int[] counters = new int[1 << 24];
		
		long logInterval = (1L << 24) - 1;
		double percent;
		
		for (long i = 0; i < numPairs; i++) {
			generatePlaintext(i, plaintext);
			generatePlaintextPrime(plaintext, plaintext_, inputDifference);
			
			ciphertext = cipher.encrypt(2, 20, plaintext);
			ciphertext_ = cipher.encrypt(2, 20, plaintext_);
			diff = ((ciphertext[0] ^ ciphertext_[0]) << 16)
				| (ciphertext[1] ^ ciphertext_[1]);
			
			counters[(int)diff & differenceMask]++;
			
			if ((i & logInterval) == 0) {
				percent = (double)(i + 1) / (double)numPairs;
				log(i + "/" + numPairs + "(" + percent + ") done.");
			}
		}
		
		int threshold = 1 << 12;
		
		for (int i = 0; i < counters.length; i++) {
			if (counters[i] > threshold) {
				log("i: " + counters[i]);
			}
		}
	}
	
	private void generatePlaintext(long i, long[] plaintext) {
		// Ps are always even, P's are later always odd since the LSB in the input difference
		// is set to '1'. Thus, we cover the entire codebook.
		i <<= 1; 
		
		plaintext[0] = (i >> 16) & 0xFFFF; 
		plaintext[1] = i & 0xFFFF;
	}
	
	private void generatePlaintextPrime(long[] plaintext, long[] plaintext_, long[] inputDifference) {
		plaintext_[0] = plaintext[0] ^ inputDifference[0];
		plaintext_[1] = plaintext[1] ^ inputDifference[1];
	}
	
	private void log(Object message) {
		System.out.println(message.toString());
	}
	
}

@SuppressWarnings("unused")
class Worker implements Runnable {
	
	private Thread thread;
	private int from;
	private int to;
	
	public Worker(int from, int to) {
		// TODO
		this.thread = new Thread(this);
		this.from = from;
		this.to = to;
	}
	
	public void start() {
		thread.run();
	}
	
	public void run() {
		long[] plaintext = {0,0}, plaintext_ = {0,0};
		long[] ciphertext = {0,0}, ciphertext_ = {0,0};
		
		/*for (long i = from; i < to; i++) {
			generatePlaintext(i, plaintext);
			generatePlaintextPrime(plaintext, plaintext_, inputDifference);
			
			ciphertext = cipher.encrypt(2, 20, plaintext);
			ciphertext_ = cipher.encrypt(2, 20, plaintext_);
			diff = ((ciphertext[0] ^ ciphertext_[0]) << 16)
				| (ciphertext[1] ^ ciphertext_[1]);
			
			counters[(int)diff & differenceMask]++;
			
			if ((i & logInterval) == 0) {
				percent = (double)(i + 1) / (double)numPairs;
				log(i + "/" + numPairs + "(" + percent + ") done.");
			}
		}*/
	}
	
}
