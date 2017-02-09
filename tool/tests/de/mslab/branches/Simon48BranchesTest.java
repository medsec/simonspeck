package de.mslab.branches;
import org.junit.Test;

import de.mslab.ciphers.Simon48;

public class Simon48BranchesTest extends SimonBranchesTest {
	
	private static final long[] KEY = { 0x1a1918, 0x121110, 0x0a0908, 0x020100 };
	private static final long[][] EXPECTED_DIFFERENCES = { 
		{ 0b00010001_00000001, 0b01000000_00000000 }, 
		{ 0b00000100_00000100, 0b00010001_00000001 },
		{ 0b00000001_00010001, 0b00000100_00000100 },
		{ 0b00000000_01000000, 0b00000001_00010001 },
		{ 0b00000000_00010001, 0b00000000_01000000 },
		{ 0b00000000_00000100, 0b00000000_00010001 },
		{ 0b00000000_00000001, 0b00000000_00000100 },
		{ 0b00000000_00000000, 0b00000000_00000001 },
		{ 0b00000000_00000001, 0b00000000_00000000 },
		{ 0b00000000_00000100, 0b00000000_00000001 },
		{ 0b00000000_00010001, 0b00000000_00000100 },
		{ 0b00000000_01000000, 0b00000000_00010001 },
		{ 0b00000001_00010001, 0b00000000_01000000 },
		{ 0b00000100_00000100, 0b00000001_00010001 },
		{ 0b00010001_00000001, 0b00000100_00000100 }
	};
	
	public Simon48BranchesTest() {
		super(new Simon48(), KEY, EXPECTED_DIFFERENCES);
	}
	
	@Test
	public void findBranches() {
		final long numPairs = 1L << 23;
		investigateRounds(numPairs, 1, 7);
		investigateRounds(numPairs, 8, 14);
		investigateRounds(numPairs, 4, 12);
	}
	
}
