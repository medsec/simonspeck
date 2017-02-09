package de.mslab.branchandbound;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mslab.branchandbound.BranchAndBoundTool.Direction;
import de.mslab.branchandbound.iterators.BranchIterator;
import de.mslab.branchandbound.iterators.SimonBackwardsIterator;
import de.mslab.branchandbound.iterators.SimonForwardsIterator;
import de.mslab.branchandbound.iterators.SpeckBackwardsIterator;
import de.mslab.branchandbound.iterators.SpeckForwardsIterator;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.CipherFactory;
import de.mslab.ciphers.Simon;
import de.mslab.ciphers.Simon128;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.Simon48;
import de.mslab.ciphers.Simon64;
import de.mslab.ciphers.Simon96;
import de.mslab.ciphers.Speck128;
import de.mslab.ciphers.Speck32;
import de.mslab.ciphers.Speck48;
import de.mslab.ciphers.Speck64;
import de.mslab.ciphers.Speck96;

@SuppressWarnings("unused")
public class BranchAndBoundTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
	}
	
	@Test
	public void test() throws IOException {
		final Direction direction = Direction.FORWARDS;
		final String cipherName = Speck32.NAME;
		final Cipher cipher = CipherFactory.instantiateCipher(cipherName);
		
		final ActiveBits inputDifference = new ActiveBits(new int[]{6}, new int[]{});
		final int fromRound = 1;
		final int toRound = 15;
		final BranchAndBoundTool tool = new BranchAndBoundTool();
		tool.setNumDifferencesToStore(1 << 13);
		tool.setNumDifferencesToProcessPerRun(1 << 8);
		tool.setProbabilityLogOverhead(8.0);
		
		final BranchIterator iterator = instantiateIterator(direction, cipher);
		iterator.setMaxNumElements(1L << 18);
		
		if (direction.equals(Direction.FORWARDS)) {
			tool.setLogFile("results/" + cipherName + "_forwards.txt");
			tool.findBestTrailsForwards(fromRound, toRound, iterator, inputDifference);
		} else {
			tool.setLogFile("results/" + cipherName + "_backwards.txt");
			tool.findBestTrailsBackwards(fromRound, toRound, iterator, inputDifference);
		}
	}
	
	private BranchIterator instantiateIterator(Direction direction, Cipher cipher) {
		if (cipher instanceof Simon) {
			if (direction.equals(Direction.FORWARDS)) {
				return new SimonForwardsIterator(cipher);
			} else {
				return new SimonBackwardsIterator(cipher);
			}
		} else {
			if (direction.equals(Direction.FORWARDS)) {
				return new SpeckForwardsIterator(cipher);
			} else {
				return new SpeckBackwardsIterator(cipher);
			}
		}
	}
	
}
