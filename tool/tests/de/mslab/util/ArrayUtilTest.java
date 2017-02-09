package de.mslab.util;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArrayUtilTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void testActiveBitsToArray() {
		final long input = 0b0001_1010_0000_0000;
		final int[] expectedOutput = { 9,11,12 };
		final int[] output = ArrayUtil.activeBitsToArray(input);
		assertArrayEquals(expectedOutput, output);
	}
	
	@Test
	public void testActiveBitsToLong() {
		final int[] input = { 9,11,12 };
		final long expectedOutput = 0b0001_1010_0000_0000;
		final long output = ArrayUtil.activeBitsToLong(input);
		assertEquals(expectedOutput, output);
	}
	
}
