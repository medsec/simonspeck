package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;

public class SimonForwardsIterator extends SimonIterator {
	
	protected long outputDifferenceLeft;
	
	public SimonForwardsIterator(Cipher cipher) {
		super(cipher);
	}
	
	public BranchIterator clone() {
		final SimonForwardsIterator copy = new SimonForwardsIterator(null);
		copyProperties(copy);
		return copy;
	}
	
	protected ActiveBits createActiveBitsFromCurrent() {
		// Assume current = 130 = 1000 0010 and
		// Delta L_{in} = (0,2,3,4)
		// Delta R_{in} = (11,14)
		// Then, Delta L_{out} = (2,4,5,6,8,11,12,14) and
		// Delta R_{out} = Delta L_{in}
		
		long mask = 1L;
		long left = this.outputDifferenceLeft;
		
		for (int i = 0; i < this.numActiveMasks; i++) {
			if ((current & mask) != 0) {
				left ^= this.activeBitMasks[i];
			}
			
			mask <<= 1;
		}
		
		return new ActiveBits(
			left, 
			inputDifference.left, 
			outputProbabilityLog, 
			inputDifference.numPaths, 
			inputDifference.left, 
			inputDifference.right
		);
	}
	
	protected long determineNumElements() {
		this.numActiveBits = Long.bitCount(inputDifference.left);
		this.numActiveMasks = 2 * numActiveBits;
		return internalDetermineNumElements();
	}
	
	protected void precomputeFirstStepsOfRound() {
		this.outputDifferenceLeft = internalPrecomputeFirstStepsOfRound(inputDifference.left, inputDifference.right);
		this.outputProbabilityLog = inputDifference.probabilityLog + numActiveMasks;
	}
	
}
