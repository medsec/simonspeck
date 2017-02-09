package de.mslab.branches;
import org.junit.Test;

import de.mslab.ciphers.Simon64;

public class Simon64BranchesTest extends SimonBranchesTest {
	
	private static final long[] KEY = { 0x1b1a1918L, 0x13121110L, 0x0b0a0908L, 0x03020100L };
	private static final long[][] EXPECTED_DIFFERENCES = { 
		{ 0x4000_0000, 0x1101_0001 }, 
		{ 0x1101_0000, 0x2000_0000 },
		{ 0x0404_0000, 0x1101_0000 },
		{ 0x0111_0000, 0x0404_0000 },
		{ 0x0040_0000, 0x0111_0000 },
		{ 0x0011_0000, 0x0040_0000 },
		{ 0x0004_0000, 0x0011_0000 },
		{ 0x0001_0000, 0x0004_0000 },
		{ 0, 0x0001_0000 },
		{ 0x0001_0000, 0 },
		{ 0x0004_0000, 0x0001_0000 },
		{ 0x0011_0000, 0x0004_0000 },
		{ 0x0040_0000, 0x0011_0000 },
		{ 0x0111_0000, 0x0040_0000 },
		{ 0x0404_0000, 0x0111_0000 },
		{ 0x1101_0000, 0x0404_0000 },
		{ 0x4000_0000, 0x1101_0000 },
		{ 0x1101_0001, 0x4000_0000 }
	};
	
	public Simon64BranchesTest() {
		super(new Simon64(), KEY, EXPECTED_DIFFERENCES);
	}
	
	@Test
	public void findBranches() {
		final long numPairs = 1L << 23;
		// investigateRounds(numPairs, 1, 5);
		investigateRounds(numPairs, 4, 10);
		investigateRounds(numPairs, 8, 13);
		investigateRounds(numPairs, 12, 17);
	}
	
}
