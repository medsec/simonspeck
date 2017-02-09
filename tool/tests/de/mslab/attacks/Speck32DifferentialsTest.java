package de.mslab.attacks;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Speck32;

public class Speck32DifferentialsTest {
	
	private static final Cipher cipher = new Speck32();
	private static final long[] inputDifference = { 
		0b0000_1010_0110_0000, 0b0100_0010_0000_0101 
	}; 
	private static final long[] outputDifference = { 
		0b1000_0000_0010_1010, 0b1101_0100_1010_1000 
	}; 
	private static final int numLoops = 16;
	private static final int numRounds = 8;
	private static final long numTexts = 1L << 25;
	
	private static AtomicInteger numOutputDifferenceOccurences = new AtomicInteger();
	
	@Test
	public void test() {
		for (int i = 0; i < numLoops; i++) {
			setRandomKey();
			encryptTexts();
			log("Loop " + i + "/" + numLoops);
		}
		
		logResult();
	}
	
	private void setRandomKey() {
		long[] key = new long[4];
		
		for (int i = 0; i < 4; i++) {
			key[i] = (long)(Math.random() * 0xFFFF);
		}
		
		cipher.setKey(key);
	}
	
	private void encryptTexts() {
		final int numTasks = 2 * Runtime.getRuntime().availableProcessors();
		final ExecutorService threadPool = Executors.newFixedThreadPool(numTasks);
		final CompletionService<Object> poolService = new ExecutorCompletionService<Object>(threadPool);
		
		final long interval = numTexts / numTasks;
		long from = 0;
		long to = interval;
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
	
	private void logResult() {
		double numOccurences = numOutputDifferenceOccurences.get();
		double average = numOccurences / (double)numLoops; 
		log("Occurences: " + numOccurences + " Average: " + average);
	}
	
	private void log(Object message) {
		System.out.println(message);
	}

	class EncryptTask implements Callable<Object> {
		
		public long from;
		public long to;
		
		public EncryptTask(long from, long to) {
			this.from = from;
			this.to = to;
		}
		
		public Object call() throws Exception {
			final long mask = 0xFFFF;
			
			long[] plaintext = new long[2];
			long[] plaintext_ = new long[2];
			long[] difference = new long[2];
			long[] ciphertext, ciphertext_;
			
			for (long i = from; i < to; i++) {
				plaintext[0] = (long)(Math.random() * mask);
				plaintext[1] = (long)(Math.random() * mask);
				
				plaintext_[0] = plaintext[0] ^ inputDifference[0];
				plaintext_[1] = plaintext[1] ^ inputDifference[1];
				
				ciphertext = cipher.encrypt(1, numRounds, plaintext);
				ciphertext_ = cipher.encrypt(1, numRounds, plaintext_);
				
				difference[0] = ciphertext[0] ^ ciphertext_[0];
				difference[1] = ciphertext[1] ^ ciphertext_[1];
				
				if (difference[0] == outputDifference[0] && difference[1] == outputDifference[1]) {
					numOutputDifferenceOccurences.incrementAndGet();
				}
			}
			
			return null;
		}
		
	}
	
}
