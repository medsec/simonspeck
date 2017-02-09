package de.mslab.branchandbound.iterators;

import de.mslab.branchandbound.ActiveBits;
import de.mslab.ciphers.Cipher;

public class SimonBackwardsIterator extends SimonIterator {
	
	protected long outputDifferenceRight;
	
	public SimonBackwardsIterator(Cipher cipher) {
		super(cipher);
	}
	
	public BranchIterator clone() {
		final SimonBackwardsIterator copy = new SimonBackwardsIterator(null);
		copyProperties(copy);
		return copy;
	}
	
	protected ActiveBits createActiveBitsFromCurrent() {
		// Assume current = 130 = 1000 0010 and
		// Delta L_{in} = (2,4,5,6,8,11,12,14)
		// Delta R_{in} = (0,2,3,4)
		// Then, Delta L_{out} = (Delta R_{in}) and
		// Delta R_{out} = (11,14)
		
		long mask = 1L;
		long right = this.outputDifferenceRight;
		
		for (int i = 0; i < this.numActiveMasks; i++) {
			if ((current & mask) != 0) {
				right ^= this.activeBitMasks[i];
			}
			
			mask <<= 1;
		}
		
		return new ActiveBits(
			inputDifference.right, 
			right, 
			outputProbabilityLog, 
			inputDifference.numPaths, 
			inputDifference.left, 
			inputDifference.right
		);
	}
	
	protected long determineNumElements() {
		this.numActiveBits = Long.bitCount(inputDifference.right);
		this.numActiveMasks = 2 * numActiveBits;
		return internalDetermineNumElements();
	}
	
	protected void precomputeFirstStepsOfRound() {
		this.outputDifferenceRight = internalPrecomputeFirstStepsOfRound(inputDifference.right, inputDifference.left);
		this.outputProbabilityLog = inputDifference.probabilityLog + numActiveMasks;
	}
	
}
