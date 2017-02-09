package de.mslab.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class MathUtilTest {

	@Test
	public void testCreateDifference() {
		final long[] shouldBe = {
			0b000100010000000100010001000000000000000000000000L, 
			0b000001000000010000000100000000000000000000000000L
		};
		final long[][] description = { {24,28,32,40,44},{26,34,42} };
		final long[] result = {
			MathUtil.createDifference(description[0]), 
			MathUtil.createDifference(description[1])
		};
		assertArrayEquals(shouldBe, result);
	}
	
}
