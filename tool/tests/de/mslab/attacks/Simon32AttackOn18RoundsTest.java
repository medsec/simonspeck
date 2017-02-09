package de.mslab.attacks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.helpers.TextPair;
import de.mslab.util.MathUtil;
import de.mslab.util.StringUtil;

@SuppressWarnings("unused")
public class Simon32AttackOn18RoundsTest {
	
	private static final Cipher cipher = new Simon32();
	private static final long[][] differenceMasks = { 
		null, null, null, null, null, null, null, null, null, null, // rounds 0-9
		null, null, null, null, // rounds 10-13
		{ 0b11111111_11111111, 0b11111111_11111111 }, // 14 
		{ 0b01111111_10111111, 0b11111111_11111111 }, // 15 
		{ 0b10111110_01111100, 0b01111111_10111111 }, // 16
		{ 0b01111000_10110000, 0b10111110_01111100 }, // 17
		{ 0b10100000_01000000, 0b01111000_10110000 }  // 18
	};
	private static final long[][] expectedDifferences = {
		{ 0x0040, 0x0000 },
		{ 0x0000, 0x0040 },
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		null, 
		{ 0x4000, 0x0000 }, // 14 
		{ 0x0001, 0x4000 }, // 15
		{ 0x0004, 0x0001 }, // 16
		{ 0x0010, 0x0004 }, // 17
		{ 0x0040, 0x0010 }  // 18 
	};
	private static final long[][] randomKeys = {
//		{ 0x4688, 0x8b7a, 0xc22e, 0xf12e }, 
//		{ 0x4f45, 0x55dc, 0x1bfe, 0xd52f }, 
//		{ 0xc126, 0x87a9, 0xc4d3, 0x5db5 }, 
//		{ 0x61a5, 0xbeb0, 0x4761, 0xccab }, 
//		{ 0x0e2b, 0xbbdb, 0x1799, 0xf7e8 }, 
//		{ 0x313f, 0xcc6b, 0x5683, 0xc6b0 }, 
//		{ 0xcca8, 0x07fe, 0xa793, 0x9ef4 }, 
//		{ 0xb89c, 0xbebf, 0x66a5, 0x31bd }, 
//		{ 0xe849, 0xbd5f, 0x821c, 0xc488 }, 
//		{ 0x7c57, 0x5ebe, 0x78cf, 0x4ba8 }, 
//		{ 0x2c1c, 0xbd61, 0x7fb8, 0x67cb }, 
//		{ 0x16b0, 0xb91f, 0x9f96, 0x9f4b }, 
//		{ 0x5d1e, 0xef98, 0xaefb, 0xf9f5 }, 
//		{ 0xff53, 0x386c, 0x6967, 0x7c4e }, 
//		{ 0x5590, 0xb040, 0x8878, 0xdd95 }, 
		{ 0x2879, 0xe00a, 0xb050, 0x3a14 }
	};
	
	private static final int numAttackRounds = 18;
	private static final int numDifferentialRounds = 14;
	// Our differential has a probability of 2^{-30.2}, so let's use 2^{31} pairs
	private static final long numChosenTextPairs = 2147483648L;
	// We want to recover 18 secret-key bits
	private static final int numKeyCandidates = 1 << 18;
	
	private static final int numCores = Runtime.getRuntime().availableProcessors();
	private static final int numTasks = 4 * numCores;
	private static final int numTestedKeys = randomKeys.length;
	private static final long keyLogInterval = 0xFFF;
	private static final long pairLogInterval = 0xFF_FFFF; 
	
	private static final String pairsFilePath = "temp/simon32_pairs.dat";
	
	// 64/16 = 4 key words for Simon32/64
	private static long[] currentKey = new long[cipher.getKeySize() / cipher.getWordSize()]; 
	private static List<TextPair> pairs = Collections.synchronizedList(new ArrayList<TextPair>());
	private static AtomicIntegerArray correctPairCounters = new AtomicIntegerArray(numKeyCandidates);
	
	private static AtomicInteger maxNumCorrectPairsForOneKey = new AtomicInteger();
	private static AtomicInteger numKeysWithPairs = new AtomicInteger();
	private static AtomicLong numCorrectPairs = new AtomicLong();
	private static AtomicLong numProcessedPairs = new AtomicLong();
	private static AtomicLong numProcessedKeys = new AtomicLong();
	
	@Test
	public void test() throws IOException {
		for (int i = 0; i < numTestedKeys; i++) {
			reset(i);
			collectPairs();
			storePairs(pairsFilePath);
			readPairs(pairsFilePath);
			testKeyCandidates();
			logResults();
		}
	}
	
	protected void reset(int run) {
		log(String.format("Testing key %s", (run + 1)));
		
		currentKey = randomKeys[run];
		cipher.setKey(currentKey);
		
		pairs.clear();
		maxNumCorrectPairsForOneKey.set(0);
		numCorrectPairs.set(0);
		numKeysWithPairs.set(0);
		numProcessedKeys.set(0);
		numProcessedPairs.set(0);

		for (int i = 0; i < numKeyCandidates; i++) {
			correctPairCounters.set(i, 0);
		}
	}
	
	protected void collectPairs() {
		log("Start collecting pairs");
		createTasks(numTasks, numChosenTextPairs, new CollectPairsTaskFactory());
		
		log(String.format(
			"Found %d correct pairs after Round 14. Stored %d pairs after Round 17.", 
			numCorrectPairs.get(), pairs.size()
		));
	}
	
	protected void testKeyCandidates() {
		log("Start testing key candidates");
		createTasks(numTasks, numKeyCandidates, new TestKeysTaskFactory());
	}
	
	protected void logResults() {
		final int correctKey = determineCorrectKey();
		
		log(String.format("Found %d pairs", pairs.size()));
		log(String.format("Found %d keys with pairs", numKeysWithPairs.get()));
		log(String.format("Found %d pairs as maximum", maxNumCorrectPairsForOneKey.get()));
		log(String.format("Found %d pairs for correct key %s",
			correctPairCounters.get(correctKey), StringUtil.toBinary(correctKey))
		);
	}
	
	private void createTasks(int numTasks, long numElements, AttackTaskFactory taskFactory) {
		final ExecutorService threadPool = Executors.newFixedThreadPool(numTasks);
		final CompletionService<Object> poolService = new ExecutorCompletionService<Object>(threadPool);
		final long interval = numElements / numTasks;
		
		long from = 0;
		long to = from + interval;
		AttackTask task;
		
		for (int i = 0; i < numTasks; i++) {
			task = taskFactory.createTask();
			task.from = from;
			task.to = to;
			
			poolService.submit(task);
			
			from = to;
			to += interval;
			
			if (i + 2 == numTasks) {
				to = numElements;
			}
		}
		
		threadPool.shutdown();
		
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private int determineCorrectKey() {
		// 18 bits from 
		// K^{17}_{0,1,5,7,8,9,10,11,14,15}
		// K^{16}_{6,7,8,9,13,15}
		// K^{15}_{7,9}
		
		final long[] roundKeys = {
			cipher.getRoundKey(18), 
			cipher.getRoundKey(17), 
			cipher.getRoundKey(16) 
		};
		final long[] inputBits = {
			0b1100_1111_1010_0011, 
			0b1010_0011_1100_0000, 
			0b0000_0010_1000_0000
		};
		final int wordSize = cipher.getWordSize();
		
		long inputMask;
		long outputMask = 1L;
		int correctKey = 0;
		
		for (int i = 0; i < inputBits.length; i++) {
			inputMask = 1L;
			
			for (int j = 0; j < wordSize; j++) {
				if ((inputBits[i] & inputMask) != 0) {
					if ((roundKeys[i] & inputMask) != 0) {
						correctKey |= outputMask;
					}
					
					outputMask <<= 1;
				}
				
				inputMask <<= 1;
			}
		}
		
		return correctKey;
	}
	
	private synchronized long[] generatePlaintext(long i) {
		long left = (i >> 16) & 0xFFBF;
		long right = i & 0xFFFF;
		
		if (((i >> 16) & 0x0040L) != 0) {
			left |= 0x8000L;
		}
		
		return new long[]{ left, right };
	}
	
	private synchronized long[] generatePlaintext_(long[] plaintext, long[] difference) {
		long left = MathUtil.rotateLeft(plaintext[0], 1, 16) 
			& MathUtil.rotateLeft(plaintext[0], 8, 16);
		left ^= MathUtil.rotateLeft(plaintext[0], 2, 16);
		
		long left_ = plaintext[0] ^ difference[0];
		left_ = (MathUtil.rotateLeft(left_, 1, 16) & MathUtil.rotateLeft(left_, 8, 16))
			^ MathUtil.rotateLeft(left_, 2, 16);
		return new long[]{ 
			plaintext[0] ^ difference[0], 
			left ^ left_ ^ plaintext[1]
		};
	}
	
	private long[] generateRoundKeys(long i, long[] roundKeys) {
		roundKeys[17] = (i & 0b0000_0000_0000_0011)
			| ((i & 0b0000_0000_0000_0100) << 3)
			| ((i & 0b0000_0000_1111_1000) << 4)
			| ((i & 0b0000_0011_0000_0000) << 6);
		
		roundKeys[16] = ((i & 0b0011_1100_0000_0000) >> 4) // 6-9
			| ((i & 0b0100_0000_0000_0000) >> 1) // 13
			| (i & 0b1000_0000_0000_0000); // 15
		
		roundKeys[15] = ((i & 0b01_0000_0000_0000_0000) >> 9) // 7
			| ((i & 0b10_0000_0000_0000_0000) >> 8);
		
		return roundKeys;
	}
	
	private long[] decryptRound(long[] roundInput, long roundKey) {
		long L = roundInput[1];
		long R = roundInput[0];
		
		long x = ((L & 0x7FFF) << 1) | ((L >> 15) & 0x0001);
		long y = ((L & 0x00FF) << 8) | ((L >>  8) & 0x00FF);
		long z = ((L & 0x3FFF) << 2) | ((L >> 14) & 0x0003);
		R ^= z;
		R ^= x & y;
		R ^= roundKey;
		
		return new long[]{ L, R };
	}
	
	private long[] decryptRounds(int fromRound, int toRound, long[] input, long[] roundKeys) {
		long[] state = new long[]{ input[0], input[1] };
		
		for (int round = toRound; round >= fromRound; round--) {
			state = decryptRound(state, roundKeys[round]);
		}
		
		return state;
	}
	
	private synchronized void log(Object message) {
		System.out.println(message.toString());
	}
	
	private void readPairs(String pathname) throws IOException {
		final File file = new File(pathname);
		
		if (!file.exists()) {
			file.createNewFile();
		}
		
		final FileInputStream fileStream = new FileInputStream(file);
		final DataInputStream dataStream = new DataInputStream(fileStream);
		final int numPairsInFile = dataStream.readInt();
		pairs.clear();
		
		TextPair pair;
		long[] p, p_;
		boolean isCorrect;
		
		for (int i = 0; i < numPairsInFile; i++) {
			isCorrect = dataStream.readBoolean();
			p = new long[2];
			p_ = new long[2];
			p[0] = dataStream.readLong();
			p[1] = dataStream.readLong();
			p_[0] = dataStream.readLong();
			p_[1] = dataStream.readLong();
			
			pair = new TextPair(isCorrect, p, p_);
			pairs.add(pair);
		}
		
		dataStream.close();
		fileStream.close();
	}
	
	private void storePairs(String pathname) throws IOException {
		final File file = new File(pathname);
		
		if (!file.exists()) {
			file.createNewFile();
		}
		
		final FileOutputStream fileStream = new FileOutputStream(file);
		final DataOutputStream dataStream = new DataOutputStream(fileStream);
		dataStream.writeInt(pairs.size());
		
		for (TextPair pair : pairs) {
			dataStream.writeBoolean(pair.isCorrect);
			dataStream.writeLong(pair.p[0]);
			dataStream.writeLong(pair.p[1]);
			dataStream.writeLong(pair.p_[0]);
			dataStream.writeLong(pair.p_[1]);
		}
		
		dataStream.close();
		fileStream.close();
	}
	
	abstract class AttackTask implements Callable<Object> {

		protected long from;
		protected long to;
		
	}
	
	class CollectPairsTask extends AttackTask {
		
		public Object call() throws Exception {
			final long[] inputDifference = expectedDifferences[0];
			final long[] expectedOutputDifference = expectedDifferences[numDifferentialRounds];
			
			long[] plaintext, plaintext_;
			long[] ciphertext, ciphertext_, state, state_;
			long[] difference = new long[]{ 0, 0 };
			boolean isCorrect;
			TextPair pair;
			
			for (long i = from; i < to; i++) {
				plaintext = generatePlaintext(i);
				plaintext_ = generatePlaintext_(plaintext, inputDifference);
				
				ciphertext = cipher.encrypt(1, numDifferentialRounds, plaintext);
				ciphertext_ = cipher.encrypt(1, numDifferentialRounds, plaintext_);
				
				difference[0] = ciphertext[0] ^ ciphertext_[0];
				difference[1] = ciphertext[1] ^ ciphertext_[1];
				
				isCorrect = (difference[0] == expectedOutputDifference[0])
					&& (difference[1] == expectedOutputDifference[1]);
				
				if (isCorrect) {
					numCorrectPairs.incrementAndGet();
				}
				
				ciphertext = cipher.encrypt(numDifferentialRounds + 1, numAttackRounds, ciphertext);
				ciphertext_ = cipher.encrypt(numDifferentialRounds + 1, numAttackRounds, ciphertext_);
				
				state = decryptRound(ciphertext, 0);
				state_ = decryptRound(ciphertext_, 0);
				
				difference[0] = (state[0] ^ state_[0]) & differenceMasks[numAttackRounds - 1][0];
				difference[1] = (state[1] ^ state_[1]) & differenceMasks[numAttackRounds - 1][1];
				
				if (difference[0] == expectedDifferences[numAttackRounds - 1][0]
					&& difference[1] == expectedDifferences[numAttackRounds - 1][1]) {
					
					pair = new TextPair(isCorrect, ciphertext, ciphertext_);
					pairs.add(pair);
				}
				
				if ((numProcessedPairs.incrementAndGet() & pairLogInterval) == 0) {
					log(String.format("Processed %d/%d pairs", numProcessedPairs.get(), 
						numChosenTextPairs));
				}
			}
			
			return null;
		}
		
	}
	
	class TestKeysTask extends AttackTask {
		
		public Object call() throws Exception {
			long[] state, state_;
			long[] difference = new long[]{ 0, 0 };
			long[] roundKeys = new long[numAttackRounds + 1];
			int count;
			
			for (int i = (int)from; i < to; i++) {
				generateRoundKeys(i, roundKeys);
				
				for (TextPair pair : pairs) {
					state = decryptRounds(numDifferentialRounds + 1, numAttackRounds, pair.p, roundKeys);
					state_ = decryptRounds(numDifferentialRounds + 1, numAttackRounds, pair.p_, roundKeys);
					
					difference[0] = (state[0] ^ state_[0]) & differenceMasks[numDifferentialRounds][0];
					difference[1] = (state[1] ^ state_[1]) & differenceMasks[numDifferentialRounds][1];
					
					if (difference[0] == expectedDifferences[numDifferentialRounds][0]
						&& difference[1] == expectedDifferences[numDifferentialRounds][1]) {
						
						count = correctPairCounters.incrementAndGet(i);
						
						if (count > maxNumCorrectPairsForOneKey.get()) {
							maxNumCorrectPairsForOneKey.set(count);
						}
						
						if (count == 1) {
							numKeysWithPairs.incrementAndGet();
						}
					}
				}
				
				if ((numProcessedKeys.incrementAndGet() & keyLogInterval) == 0) {
					final double percents = (double)numProcessedKeys.get() / (double)numKeyCandidates;
					log(String.format("Processed %4d keys", percents));
				}
			}
			
			return null;
		}
		
	}
	
	abstract class AttackTaskFactory {
		
		public abstract AttackTask createTask();
		
	}
	
	class TestKeysTaskFactory extends AttackTaskFactory {
		
		public AttackTask createTask() {
			return new TestKeysTask();
		}
		
	}
	
	class CollectPairsTaskFactory extends AttackTaskFactory {
		
		public AttackTask createTask() {
			return new CollectPairsTask();
		}
		
	}
	
}











