package de.mslab.branchandbound;

import java.util.concurrent.Callable;

import de.mslab.branchandbound.iterators.BranchIterator;

class BranchAndBoundTask implements Callable<Object> {
	
	private int from;
	private int to;
	private BranchIterator iterator;
	private BranchAndBoundTool tool;
	
	public BranchAndBoundTask(int from, int to, BranchIterator iterator, BranchAndBoundTool tool) {
		this.from = from;
		this.to = to;
		this.iterator = iterator;
		this.tool = tool;
	}
	
	public Object call() throws Exception {
		ActiveBits inputDifference;
		ActiveBits outputDifference;
		
		int numInputsProcessed;
		String key;
		
		for (int i = from; i < to; i++) {
			inputDifference = tool.bestDifferences.get(i);
			
			iterator.setActiveBits(inputDifference);
			iterator.setToFirst();
			
			// For all possible differences which can be produced by the current input difference
			while(iterator.hasNext()) {
				outputDifference = iterator.next();
				key = outputDifference.hashKey();
				
				// We will store too many differences first to avoid sorting 
				// at the time of insertion. We just do not insert the differentials 
				// which are worse than the first NUM_DIFFERENCES_TO_STORE ones.
				if (outputDifference.probabilityLog <= 
					tool.bestOutputDifferenceProbabilityLog.get() + tool.getProbabilityLogOverhead()) {
					
					if (outputDifference.probabilityLog <= tool.bestOutputDifferenceProbabilityLog.get()) {
						tool.bestOutputDifferenceProbabilityLog.set((int)outputDifference.probabilityLog);
					}
					
					if (tool.outputDifferences.containsKey(key)) {
						tool.outputDifferences.get(key).merge(outputDifference.probabilityLog);
					} else {
						tool.outputDifferences.put(key, outputDifference);
					}
				}
				
				tool.numOutputDifferences.incrementAndGet();
			}
			
			numInputsProcessed = tool.numInputDifferencesProcessed.incrementAndGet();
			
			if ((numInputsProcessed & 0x3FF) == 0) {
				tool.logger.log(String.format(
					"Testing %d/%d %s #elements: %d ", 
					numInputsProcessed, tool.bestDifferences.size(), 
					inputDifference, iterator.getNumElements()
				));
			}
			
		}
		
		return null;
	}

}