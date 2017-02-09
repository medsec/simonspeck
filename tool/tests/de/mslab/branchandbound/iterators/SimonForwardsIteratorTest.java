package de.mslab.branchandbound.iterators;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon32;

public class SimonForwardsIteratorTest {
	
	private static BranchIterator iterator;
	private static final ActiveBits activeBits = new ActiveBits(new int[]{0,2,3,4}, new int[]{11,14});
	private static final int current = 130; // = 0b1000_0010;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Cipher cipher = new Simon32();
		iterator = new SimonForwardsIterator(cipher);
		iterator.setActiveBits(activeBits);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		iterator = null;
	}
	
	@Test
	public void testCreateActiveBitsFromCurrent() {
		iterator.setToFirst();
		iterator.current = current;
		
		// Each active bit can influence two bits after the AND operation 
		// in the round function of SIMON
		// Bits 1,0 of current represent if Bits 1 and/or 8 are active (from the 0)
		// Bits 3,2 of current represent if Bits 3 and/or 10 are active (from the 2)
		// Bits 5,4 of current represent if Bits 4 and/or 11 are active (from the 3)
		// Bits 7,6 of current represent if Bits 5 and/or 12 are active (from the 4)
		// (0,2,3,4) => (1,8) (3,10) (4,11) (5,12)
		// So, we have in total 2^8 = 256 possibilities, each with prob 2^{-8}
		// 
		// Possibility 130 means 1000 0010 => 12 is active, and 8 is active after 
		// the AND operation.
		// Left: (11,14)
		// Right: (8,12) ^ ((0,2,3,4) <<< 2) ^ (11,14) => (2,4,5,6,8,11,12,14)
		
		final ActiveBits expectedActiveBits = new ActiveBits(new int[]{2,4,5,6,8,11,12,14}, new int[]{0,2,3,4});
		final ActiveBits activeBits = iterator.createActiveBitsFromCurrent();
		final int expectedProbabilityLog = 8;
		
		assertEquals(expectedActiveBits.left, activeBits.left);
		assertEquals(expectedActiveBits.right, activeBits.right);
		assertEquals(expectedProbabilityLog, activeBits.probabilityLog, 0.00001);
	}
	
	@Test
	public void testDetermineNumElements() {
		// For SIMON, n active bits can produce 2^{2n} possible output differences
		final int expectedNumElements = 1 << 8; 
		final long numElements = iterator.determineNumElements();
		assertEquals(expectedNumElements, numElements);
	}
	
	protected void log(Object message) {
		System.out.println(message);
	}
	
}
