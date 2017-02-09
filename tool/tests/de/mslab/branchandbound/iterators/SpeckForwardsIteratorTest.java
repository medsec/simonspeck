package de.mslab.branchandbound.iterators;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Speck32;

public class SpeckForwardsIteratorTest {
	
	private static SpeckForwardsIterator iterator;
	private static final ActiveBits activeBits = new ActiveBits(new int[]{0, 2, 3, 4}, new int[]{11,14});
	private static final int maxNumSubsequentCarryBits = 6; 
	private static final int current = 130;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Cipher cipher = new Speck32();
		iterator = new SpeckForwardsIterator(cipher);
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
		
		// (9,11,12,13) + (11,14) => (9,11,12,13,14)
		// (9,11,12,13,14) => (6,5,4,3,2)
		// 130 => (4,1,0,1,0)
		//  9[4] = (9,10,11,12,13)
		// 11[1] = ( ,  ,11,12)
		// 12[0] = ( ,  ,  ,12)
		// 13[1] = ( ,  ,  ,  ,13,14)
		// 14[0] = ( ,  ,  ,  ,  ,14)
		// Cancel: ( ,  ,11)
		// ---------------------------
		// Left:   (9,10,11,12)
		// Right:  (9,10,11,12) ^ (0,13) => (0,9,10,11,12,13)
		
		final ActiveBits expectedActiveBits = new ActiveBits(new int[]{9,10,11,12}, new int[]{0,9,10,11,12,13});
		final ActiveBits activeBits = iterator.createActiveBitsFromCurrent();
		final int expectedProbabilityLog = 11;
		
		assertEquals(expectedActiveBits.left, activeBits.left);
		assertEquals(expectedActiveBits.right, activeBits.right);
		assertEquals(expectedProbabilityLog, activeBits.probabilityLog, 0.00001);
	}
	
	@Test
	public void testDetermineNumElements() {
		// For 9,11,12,13,14
		// Note that at most 6, not 7 carry bits can occur for bit at index 9, 
		// since we have set maxNumSubsequentCarryBits at the beginning to 6.
		final int expectedNumElements = 6 * 5 * 4 * 3 * 2; 
		final long numElements = iterator.determineNumElements();
		assertEquals(expectedNumElements, numElements);
	}
	
	@Test()
	public void testPerformance() {
		final int numTimes = 10;
		final ActiveBits newActiveBits = new ActiveBits(
			new int[]{1, 2, 6, 9, 15}, new int[]{2, 5, 0, 1, 3, 4, 6, 9, 10, 15}
		);
		iterator.setActiveBits(newActiveBits);
		
		for (int j = 0; j < numTimes; j++) {
			iterator.setToFirst();
			
			while (iterator.hasNext()) {
				iterator.next();
			}
		}
		
		// Cleaning up for the further tests
		iterator.setActiveBits(activeBits);
	}
	
	protected void log(Object message) {
		System.out.println(message);
	}
	
}
