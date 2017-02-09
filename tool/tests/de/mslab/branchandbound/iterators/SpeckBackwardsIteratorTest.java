package de.mslab.branchandbound.iterators;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Speck32;

public class SpeckBackwardsIteratorTest {
	
	private static SpeckBackwardsIterator iterator;
	private static final ActiveBits activeBits = new ActiveBits(new int[]{9,10,11,12}, new int[]{0,9,10,11,12,13});
	private static final int maxNumSubsequentCarryBits = 6; 
	private static final int current = 238;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Cipher cipher = new Speck32();
		iterator = new SpeckBackwardsIterator(cipher);
		iterator.setActiveBits(activeBits);
		iterator.setMaxNumSubsequentCarryBits(maxNumSubsequentCarryBits);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		iterator = null;
	}
	
	@Test
	public void testCreateActiveBitsFromCurrent() {
		iterator.setToFirst();
		iterator.current = current;
		
		// (9,10,11,12) ^ (0,9,10,11,12) => (0,13)
		// (0,13) >>> 2 => (11,14)
		// (9,10,11,12) + (11,14)
		// 239 => (4,3,1,1,0)
		//  9[4] = (9,10,11,12,13)
		// 10[3] = ( ,10,11,12,13)
		// 11[1] = ( ,  ,11,12)
		// 12[1] = ( ,  ,  ,12,13)
		// 14[0] = ( ,  ,  ,  ,  ,14)
		// Cancel: ( ,  ,11)
		// ---------------------------
		// 		   (9,        ,13,14)
		// Left:   (9,13,14) <<< 7 => (0,4,5)
		// Right:  (11,14)
		
		final ActiveBits expectedActiveBits = new ActiveBits(new int[]{0,4,5}, new int[]{11,14});
		final ActiveBits activeBits = iterator.createActiveBitsFromCurrent();
		final int expectedProbabilityLog = 14;
		
		assertEquals(expectedActiveBits.left, activeBits.left);
		assertEquals(expectedActiveBits.right, activeBits.right);
		assertEquals(expectedProbabilityLog, activeBits.probabilityLog, 0.00001);
	}
	
	//@Test
	public void testDetermineNumElements() {
		// For 9,10,11,12,14
		// Note that at most 6, not 7 carry bits can occur for bit at index 9, and 10.
		final int expectedNumElements = 6 * 6 * 5 * 4 * 2; 
		final long numElements = iterator.determineNumElements();
		assertEquals(expectedNumElements, numElements);
	}
	
	protected void log(Object message) {
		System.out.println(message);
	}
	
}
