package de.mslab.attacks;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import de.mslab.ciphers.Simon32;
import de.mslab.util.StringUtil;

public class SpeckCodebookTest {
	
	private static final Cipher cipher = new Simon32();
	private static final long[] inputDifference = { 
		0x0040, 0x0000 
	};
	private static final long[] key = { 0x1918, 0x1a5e, 0x4011, 0x89ac };
	private static final int numCounters = 1 << 27;
	private static final long numPairs = 1L << 21;
	private static final int numRounds = 20;
	
	private static int[] counters;
	private static AtomicInteger numProcessedPairs = new AtomicInteger();
	private static List<Long> outputDifferences = new ArrayList<Long>();
	
	@Test
	public void test() throws IOException {
		initialize();
		encryptTexts();
		determineMostOftenOccurringDifferences();
		storeMostOftenOccurringDifferences();
		clearLists();
	}
	
	private void initialize() {
		counters = null;
		counters = new int[numCounters];
		cipher.setKey(key);
		numProcessedPairs.set(0);
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
	}
	
	private void determineMostOftenOccurringDifferences() {
		final int[] masks = {
			0xFF00_0000, 0x00FF_0000, 0x0000_FF00, 0x0000_00FF
		};
		final int[] shifts = { 0, 8, 16, 24 };
		final int lsbBits = 0b11;
		final int numMasks = masks.length;
		
		int count, j, mask;
		int current;
		long difference;
		int max = 0;
		long maxOccurringDifference = 0;
		
		for (int i = 0; i < numCounters; i++) {
			count = counters[i];
			
			for (j = 0; j < numMasks; j++) {
				mask = masks[j];
				
				if ((count & mask) != 0) { // Max count
					difference = (count << 2) | (count & lsbBits);
					outputDifferences.add(difference);
					
					current = count >> shifts[j];
					
					if (current > max) {
						max = current;
						maxOccurringDifference = difference;
					}
				}
			}
		}
		
		log(outputDifferences.size() + " differences have occurred often.");
		log(StringUtil.toNBitBinary(maxOccurringDifference, 32) + " occurred " + max + " times.");
	}
	
	private void storeMostOftenOccurringDifferences() throws IOException {
		File file = new File("temp/foobar.dat");
		
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream stream = new FileOutputStream(file);
		DataOutputStream dataStream = new DataOutputStream(stream);
		
		dataStream.writeInt(outputDifferences.size());
		
		for (Long difference : outputDifferences) {
			dataStream.writeLong(difference);
		}
		
		dataStream.close();
	}
	
	private void clearLists() {
		counters = null;
		outputDifferences.clear();
		outputDifferences = null;
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
			final long msb = 0x8000;
			final long mask = 0xFFFF;
			final long differenceMask = 0xFF;
			
			long[] plaintext = new long[2];
			long[] plaintext_ = new long[2];
			long[] difference = new long[2];
			long[] ciphertext, ciphertext_;
			
			// Table-related stuff
			final int[] masks = {
				0xFF00_0000, 0x00FF_0000, 0x0000_FF00, 0x0000_00FF
			};
			final int[] increments = {
				0x0100_0000, 0x0001_0000, 0x0000_0100, 0x0000_0001
			};
			final int insideIntMask = 0b11;
			int byteIndex;
			int intIndex;
			int value;
			int valueByteMask;
			
			for (long i = from; i < to; i++) {
				plaintext[0] = (i >> 16) & mask;
				plaintext[1] = i & mask;
				
				if ((plaintext[0] & inputDifference[0]) != 0) {
					plaintext[0] |= msb;
				}
				
				plaintext_[0] = plaintext[0] ^ inputDifference[0];
				plaintext_[1] = plaintext[1] ^ inputDifference[1];
				
				ciphertext = cipher.encrypt(1, numRounds, plaintext);
				ciphertext_ = cipher.encrypt(1, numRounds, plaintext_);
				
				difference[0] = ciphertext[0] ^ ciphertext_[0];
				difference[1] = ciphertext[1] ^ ciphertext_[1];
				
				byteIndex = (int)(((difference[0] & differenceMask) << 16) | (difference[1])); 
				// (13)_10 = (1101)_2 is stored in integer (...11)_2 = (3)_10, at position (01)_2 = (1)_10
				intIndex = byteIndex >> 2; // = (...11)
				value = counters[intIndex]; // = 32-bit integer for indices 12..15
				valueByteMask = masks[byteIndex & insideIntMask];
				value &= valueByteMask; // = the eight bits for index 13
				
				if (value != valueByteMask) {  
					// Is the byte already FF? Then do not increment else, we would
					// write into the next cell
					counters[intIndex] += increments[byteIndex & insideIntMask];
				}
				
				if ((numProcessedPairs.incrementAndGet() & 0xFF_FFFF) == 0) {
					log("Processed " + numProcessedPairs.get() + "/" + numPairs + " pairs.");
				}
			}
			
			return null;
		}
		
	}
	
}
