package de.mslab.attacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon32;
import de.mslab.util.StringUtil;

@SuppressWarnings("unused")
public class LinearizingSimonTest {
	
	private static final Cipher cipher = new Simon32();
	private static final long numLoops = 64;
	private static final int numRounds = 9;
	private static final int numTexts = 1 << 18;
	private static final int wordSize = cipher.getWordSize();
	
	private static long count = 0;
	private static long[] key = new long[4];
	private static long keyBit = 0;
	private static long numCorrect = 0;
	
	private static List<Integer> ciphertexts = Collections.synchronizedList(new ArrayList<Integer>());
	private static List<Integer> plaintexts = Collections.synchronizedList(new ArrayList<Integer>());
	
	@Test
	public void test() {
		for (int i = 0; i < numLoops; i++) {
			setRandomKey();
			generateRandomPlaintexts(i);
			encryptPlaintexts();
			recoverKeyBits();
			uncoverTheKeyBit();
			log(String.format("%d/%d loops.", i, numLoops));
		}
		
		logResult();
		clearLists();
	}
	
	private void setRandomKey() {
		for (int i = 0; i < 4; i++) {
			key[i] = (long)(Math.random() * (double)0xFFFF);
		}
		
		cipher.setKey(key);
	}
	
	private void generateRandomPlaintexts(int loop) {
		int plaintext;
		
		if (loop == 0) {
			for (int i = 0; i < numTexts; i++) {
				plaintext = (int)(Math.random() * 0x7FFF_FFFF);
				plaintexts.add(plaintext);
				ciphertexts.add(0);
			}
		} else {
			for (int i = 0; i < numTexts; i++) {
				plaintext = (int)(Math.random() * 0x7FFF_FFFF);
				plaintexts.set(i, plaintext);
			}
		}
	}
	
	private void encryptPlaintexts() {
		final int numTasks = 2 * Runtime.getRuntime().availableProcessors();
		final ExecutorService threadPool = Executors.newFixedThreadPool(numTasks);
		final CompletionService<Object> poolService = new ExecutorCompletionService<Object>(threadPool);
		
		final int interval = numTexts / numTasks;
		int from = 0;
		int to = interval;
		Callable<Object> task;
		
		for (int i = 0; i < numTasks; i++) {
			task = new EncryptTask(from, to);
			poolService.submit(task);
			
			from = to;
			to += interval;
			
			if (i + 2 == numTasks) {
				to = numTexts;
			}
		}
		
		threadPool.shutdown();
		
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void recoverKeyBits() {
		final int bit0Mask = 0b0000_0000_0000_0001;
		final int bit2Mask = 0b0000_0000_0000_0100;
		final int bit4Mask = 0b0000_0000_0001_0000;
		final int bit6Mask = 0b0000_0000_0100_0000;
		final int bit8Mask = 0b0000_0001_0000_0000;

		final int bit1Mask = 0b0000_0000_0000_0010;
		final int bit3Mask = 0b0000_0000_0000_1000;
		final int bit7Mask = 0b0000_0000_1000_0000;
		final int bit10Mask= 0b0000_0100_0000_0000;
		final int bit12Mask= 0b0001_0000_0000_0000;
		final int mask = 0xFFFF;
		
		count = 0;
		int bit;
		int plaintext, ciphertext;
		int plaintextLeft, plaintextRight;
		int ciphertextLeft, ciphertextRight;
		
		for (int i = 0; i < numTexts; i++) {
			plaintext = plaintexts.get(i);
			plaintextLeft = (plaintext >> 16) & mask;
			plaintextRight = plaintext & mask;
			
			ciphertext = ciphertexts.get(i);
			ciphertextLeft = (ciphertext >> 16) & mask;
			ciphertextRight = ciphertext & mask;
			
			bit = (plaintextLeft & bit0Mask)
				^ (plaintextLeft & bit4Mask) >> 4
				^ (plaintextLeft & bit8Mask) >> 8
				^ (plaintextRight & bit2Mask) >> 2
				^ (ciphertextLeft & bit2Mask) >> 2
				^ (ciphertextRight & bit0Mask) >> 0
				^ (ciphertextRight & bit4Mask) >> 4
				^ (ciphertextRight & bit8Mask) >> 8;
				
			if (bit != 0) {
				count++;
			}
		}
	}
	
	private void uncoverTheKeyBit() {
		final long[] k = new long[numRounds]; 
		
		for (int i = 0; i < numRounds; i++) {
			k[i] = cipher.getRoundKey(i + 1);
		}
		
		final int bit2Mask = 0b0000_0000_0000_0100;
		final int bit4Mask = 0b0000_0000_0001_0000;
		final int bit6Mask = 0b0000_0000_0100_0000;
		final int bit8Mask = 0b0000_0001_0000_0000;
		
		keyBit = ((k[0] & bit2Mask) >> 2)
			^ ((k[1] & bit4Mask) >> 4)
			^ ((k[1] & bit8Mask) >> 8)
			^ ((k[2] & bit6Mask) >> 6)
			^ ((k[3] & bit8Mask) >> 8)
			^ ((k[5] & bit8Mask) >> 8)
			^ ((k[6] & bit6Mask) >> 6)
			^ ((k[7] & bit4Mask) >> 4)
			^ ((k[7] & bit8Mask) >> 8)
			^ ((k[8] & bit2Mask) >> 2);
		
		double percent = (double)count / (double)numTexts;
		
		// log("Percent: " + percent + " guess: " + (percent >= 0.5) + " keyBit: " + keyBit);
		
		if (percent >= 0.5 && keyBit == 1) {
			numCorrect++;
		} else if (percent < 0.5 && keyBit == 0) {
			numCorrect++;
		}
	}
	
	private void clearLists() {
		ciphertexts = null;
		plaintexts = null;
	}
	
	private void logResult() {
		log(String.format("Correct in %d/%d cases.", numCorrect, numLoops));
	}
	
	private void log(Object message) {
		System.out.println(message);
	}
	
	class EncryptTask implements Callable<Object> {
		
		public int from;
		public int to;
		
		public EncryptTask(int from, int to) {
			this.from = from;
			this.to = to;
		}
		
		public Object call() throws Exception {
			final int mask = 0xFFFF;
			final int logMask = 0xFF_FFFF;
			long[] plaintext = new long[2];
			long[] ciphertext = new long[2];
			
			for (int i = from; i < to; i++) {
				plaintext[0] = (plaintexts.get(i) >> 16) & mask;
				plaintext[1] = plaintexts.get(i) & mask;
				ciphertext = cipher.encrypt(1, numRounds, plaintext);
				ciphertexts.set(i, 
					(((int)ciphertext[0] & mask) << 16) | ((int)ciphertext[1] & mask)
				);
			}
			
			return null;
		}
		
	}
	
}

