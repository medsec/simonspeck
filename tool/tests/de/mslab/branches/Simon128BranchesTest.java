package de.mslab.branches;

import org.junit.Test;

import de.mslab.ciphers.Simon128;

public class Simon128BranchesTest extends SimonBranchesTest {

	private static final long[] KEY = { 
		0x1f1e1d1c_1b1a1918L, 0x17161514_13121110L, 
		0x0f0e0d0c_0b0a0908L, 0x07060504_03020100L 
	};
	private static final long[][][] EXPECTED_DIFFERENCES = { 
		{ {32,36,48,52,56},{34,50,58} }, 
		{ {38,54},{32,36,48,52,56} },
		{ {32,36,40,48,52},{38,54} }, 
		{ {34,42,50},{32,36,40,48,52} },
		{ {32,40,44,48},{34,42,50} },
		{ {46},{32,40,44,48} },
		{ {32,40,44},{46} },
		{ {34,42},{32,40,44} },
		{ {32,36,40},{34,42} },
		{ {38},{32,36,40} },
		{ {32,36},{38} },
		{ {34},{32,36} },
		{ {32},{34} },
		{ {},{32} },
		{ {32},{} },
		{ {34},{32} },
		{ {32,36},{34} },
		{ {38},{32,36} },
		{ {32,36,40},{38} },
		{ {34,42},{32,36,40} },
		{ {32,40,44},{34,42} },
		{ {46},{32,40,44} },
		{ {32,40,44,48},{48} },
		{ {34,42,50},{32,40,44,48} },
		{ {32,36,40,48,52},{34,42,50} },
	};
	
	public Simon128BranchesTest() {
		super(new Simon128(), KEY, createDifferences(EXPECTED_DIFFERENCES));	
	}
	
	@Test
	public void findBranches() {
		final long numPairs = 1L << 20;
		/*
		investigateRounds(numPairs, 1, 5);
		investigateRounds(numPairs, 2, 7);
		investigateRounds(numPairs, 3, 8);
		investigateRounds(numPairs, 5, 10);
		investigateRounds(numPairs, 7, 12);
		investigateRounds(numPairs, 9, 14);
		investigateRounds(numPairs, 11, 16);
		*/
		investigateRounds(numPairs, 13, 18);
		/*
		investigateRounds(numPairs, 15, 20);
		investigateRounds(numPairs, 17, 22);
		investigateRounds(numPairs, 18, 23);
		investigateRounds(numPairs, 19, 23);
		investigateRounds(numPairs, 20, 24);
		*/
	}
	
}
