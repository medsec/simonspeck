package de.mslab.branches;
import org.junit.Test;

import de.mslab.ciphers.Simon96;


public class Simon96BranchesTest extends SimonBranchesTest {
	
	private static final long[] KEY = { 0x1b1a1918L, 0x13121110L, 0x0b0a0908L, 0x03020100L };
	private static final long[][][] EXPECTED_DIFFERENCES = { 
		{ {24,28,32,40,44},{46,30} }, 
		{ {26,34,42},{24,28,32,40,44} },
		{ {24,32,36,40},{26,34,42} }, 
		{ {38},{24,32,36,40} },
		{ {24,32,36},{38} },
		{ {26,34},{24,32,36} },
		{ {24,28,32},{26,34} },
		{ {30},{24,28,32} },
		{ {24,28},{30} },
		{ {26},{24,28} },
		{ {24},{26} },
		{ {},{24} },
		{ {24},{} },
		{ {26},{24} },
		{ {24,28},{26} },
		{ {30},{24,28} },
		{ {24,28,32},{30} },
		{ {26,34},{24,28,32} },
		{ {24,32,36},{26,34} },
		{ {38},{24,32,36} },
		{ {24,32,36,40},{38} },
		{ {26,34,42},{24,32,36,40} },
		{ {24,28,32,40,44},{26,34,42} }
	};
	
	public Simon96BranchesTest() {
		super(new Simon96(), KEY, createDifferences(EXPECTED_DIFFERENCES));	
	}
	
	@Test
	public void findBranches() {
		final long numPairs = 1L << 23;
		investigateRounds(numPairs, 1, 5);
		investigateRounds(numPairs, 4, 9);
		investigateRounds(numPairs, 5, 11);
		investigateRounds(numPairs, 7, 14);
		investigateRounds(numPairs, 9, 16);
		investigateRounds(numPairs, 12, 18);
		investigateRounds(numPairs, 15, 20);
		investigateRounds(numPairs, 17, 22);
	}
	
}
