package de.mslab.attacks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import de.mslab.ciphers.helpers.TextPair;
import de.mslab.util.MathUtil;
import de.mslab.util.StringUtil;

public class Speck32AttackTest {
	
	private static final Cipher cipher = new Speck32();
	private static final long[] inputDifference = { 
		0b0000_1010_0110_0000, 0b0100_0010_0000_0101 
	};
	private static final long[] outputDifference = { 
		0b1000_0000_0010_1010, 0b1101_0100_1010_1000 
	};
	private static final long numKeys = 1L << 12;
	private static final int numLoops = 1;
	private static final int numRounds = 10;
	private static final long numPairs = 1L << 28;
	
	private static List<TextPair> pairs = Collections.synchronizedList(new ArrayList<TextPair>());
	private static AtomicInteger numProcessedPairs = new AtomicInteger();
	
	private static long maxCountKey;
	private static int maxCount;
	
	@Test
	public void test() {
		for (int i = 0; i < numLoops; i++) {
			initializeAttack();
			setRandomKey();
			encryptTexts();
			decryptTexts();
			logCorrectKey();
		}
	}
	
	private void initializeAttack() {
		pairs.clear();
		numProcessedPairs.set(0);
		maxCountKey = 0L;
		maxCount = 0;
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
		
		final long interval = numPairs / numTasks;
		long from = 0;
		long to = interval;
		Callable<Object> task;
		
		for (int i = 0; i < numTasks; i++) {
			task = new EncryptTask(from, to);
			poolService.submit(task);
			
			from = to;
			to += interval;
			
			if (i + 2 == numTasks) {
				to = numPairs;
			}
		}
		
		threadPool.shutdown();
		
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		log("Stored " + pairs.size() + " pairs.");
	}
	
	private void decryptTexts() {
		final long mask = 0xFFFF;
		
		long[] ciphertext = new long[2];
		long[] ciphertext_ = new long[2];
		long[] difference = new long[2];
		short[] counts = new short[(int)numKeys];
		long roundKey;
		
		for (int i = 0; i < numKeys; i++) {
			roundKey = i << 4;
			
			for (TextPair pair : pairs) {
				ciphertext = Arrays.copyOf(pair.p, 2);
				ciphertext_ = Arrays.copyOf(pair.p_, 2);
				
				decryptRound(ciphertext, roundKey);
				decryptRound(ciphertext_, roundKey);
				
				difference[0] = (ciphertext[0] ^ ciphertext_[0]) & mask;
				difference[1] = (ciphertext[1] ^ ciphertext_[1]) & mask;
				
				if (difference[0] == outputDifference[0] 
					&& difference[1] == outputDifference[1]) {
					counts[i]++;
					
					if (counts[i] > maxCount) {
						maxCount = counts[i];
						maxCountKey = roundKey;
					}
				}
			}
		}
		
		log("Max count: " + maxCount + " for key: " + StringUtil.to16BitBinary(maxCountKey));
		log("Remaining pairs: " + pairs.size());
	}
	
	private void decryptRound(long[] state, long roundKey) {
		state[1] ^= state[0];
		state[0] ^= roundKey;
		state[1] = MathUtil.rotateRight(state[1], Speck32.BETA, Speck32.WORD_SIZE);
		state[0] -= state[1];
		
		if (state[0] < 0) {
			state[0] += Speck32.WORD_MASK + 1;
		}
		
		state[0] = MathUtil.rotateLeft(state[0], Speck32.ALPHA, Speck32.WORD_SIZE);
	}
	
	private void logCorrectKey() {
		final long correctKey = cipher.getRoundKey(10);
		log("Correct key: " + StringUtil.to16BitBinary(correctKey));
	}
	
	private synchronized void log(Object message) {
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
			final long leftDifference = 0x0008;
			final long leftMask = 0x000F;
			final long zeroRoundKey = 0L;
			TextPair pair;
			
			long[] plaintext = new long[2];
			long[] plaintext_ = new long[2];
			long[] state = new long[2];
			long[] state_ = new long[2];
			long[] difference = new long[2];
			long[] ciphertext, ciphertext_;
			
			for (long i = from; i < to; i++) {
				plaintext[0] = (long)(Math.random() * mask);
				plaintext[1] = (long)(Math.random() * mask);
				
				plaintext_[0] = plaintext[0] ^ inputDifference[0];
				plaintext_[1] = plaintext[1] ^ inputDifference[1];
				
				decryptRound(plaintext, zeroRoundKey);
				decryptRound(plaintext_, zeroRoundKey);
				
				ciphertext = cipher.encrypt(1, numRounds, plaintext);
				ciphertext_ = cipher.encrypt(1, numRounds, plaintext_);
				
				state[0] = ciphertext[0];
				state[1] = ciphertext[1];
				state_[0] = ciphertext_[0];
				state_[1] = ciphertext_[1];
				
				decryptRound(state, zeroRoundKey);
				decryptRound(state_, zeroRoundKey);
				
				// Not an error, we use the left word of the ciphertext 
				// and the right word of the state before the final round
				difference[0] = ciphertext[0] ^ ciphertext_[0];
				difference[1] = state[1] ^ state_[1];
				
				if ((difference[0] & leftMask) == (leftDifference & leftMask) 
					&& difference[1] == outputDifference[1]) {
					
					pair = new TextPair(true, ciphertext, ciphertext_);
					pairs.add(pair);
				}
				
				if ((numProcessedPairs.incrementAndGet() & 0xFF_FFFF) == 0) {
					log("Processed " + numProcessedPairs.get() + "/" + numPairs + " pairs.");
				}
			}
			
			return null;
		}
		
	}
	
}
