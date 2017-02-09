package de.mslab.analyzers.differential;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.analyzers.differential.SimonRelatedKeyAnalyzer;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.Simon48;

public class SimonRelatedKeyAnalyzerTest {
	
	private static SimonRelatedKeyAnalyzer analyzer;
	private static Cipher cipher;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cipher = new Simon48();
		analyzer = new SimonRelatedKeyAnalyzer(cipher);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		analyzer = null;
		cipher = null;
	}
	
	@Test
	public void testPropagateDifferentialForwardsIntLongArray() {
		long[] fourRoundKeysDifference = {
			0x0040, 0, 0, 0
		};
		
		analyzer.propagateDifferential(14, fourRoundKeysDifference);
	}
	
}
