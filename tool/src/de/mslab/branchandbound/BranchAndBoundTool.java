package de.mslab.branchandbound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.mslab.branchandbound.iterators.BranchIterator;
import de.mslab.branchandbound.iterators.SpeckBackwardsIterator;
import de.mslab.branchandbound.iterators.SpeckForwardsIterator;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.CipherFactory;
import de.mslab.ciphers.Simon48;
import de.mslab.ciphers.Speck32;
import de.mslab.ciphers.Speck48;
import de.mslab.ciphers.Speck64;
import de.mslab.util.Logger;

@SuppressWarnings("unused")
public class BranchAndBoundTool {
	
	public static enum Direction {
		FORWARDS, BACKWARDS
	};
	
	protected final Comparator<ActiveBits> PROBABILITY_COMPARATOR = new ProbabilityComparator();
	protected final Logger logger = new Logger();
	
	protected List<ActiveBits> bestDifferences = Collections.synchronizedList(new ArrayList<ActiveBits>());
	protected Map<String, ActiveBits> outputDifferences = Collections.synchronizedMap(new HashMap<String, ActiveBits>());
	protected List<ActiveBits> temporaryOutputDifferences = Collections.synchronizedList(new ArrayList<ActiveBits>());
	
	protected final AtomicInteger numInputDifferencesProcessed = new AtomicInteger(0);
	protected final AtomicInteger numOutputDifferences = new AtomicInteger(0);
	protected final AtomicInteger bestOutputDifferenceProbabilityLog = new AtomicInteger(0);
	
	private int numDifferencesToProcessPerRun = 1 << 10;
	private int numDifferencesToStore = 1 << 10;
	private double probabilityLogOverhead = 6.0;
	
	public int getNumDifferencesToProcessPerRun() {
		return numDifferencesToProcessPerRun;
	}
	
	public void setNumDifferencesToProcessPerRun(int numDifferencesToProcessPerRun) {
		this.numDifferencesToProcessPerRun = numDifferencesToProcessPerRun;
	}
	
	public int getNumDifferencesToStore() {
		return numDifferencesToStore;
	}
	
	public void setNumDifferencesToStore(int numDifferencesToStore) {
		this.numDifferencesToStore = numDifferencesToStore;
	}
	
	public double getProbabilityLogOverhead() {
		return probabilityLogOverhead;
	}
	
	public void setProbabilityLogOverhead(double probabilityLogOverhead) {
		this.probabilityLogOverhead = probabilityLogOverhead;
	}
	
	public void findBestTrailsBackwards(int fromRound, int toRound, 
		BranchIterator iterator, ActiveBits inputDifference) throws IOException {
		
		logger.openLog();
		bestDifferences.add(inputDifference);
		
		for (int round = toRound; round >= fromRound; round--) {
			findBestTrailsForRound(iterator, round);
		}
		
		logger.closeLog();
	}
	
	/**
	 * Runs the search over the specified range of rounds.
	 * @param fromRound
	 * @param toRound
	 * @param iterator
	 * @param inputDifference
	 * @throws IOException 
	 */
	public void findBestTrailsForwards(int fromRound, int toRound, 
		BranchIterator iterator, ActiveBits inputDifference) throws IOException {
		
		logger.openLog();
		bestDifferences.add(inputDifference);
		
		for (int round = fromRound; round <= toRound; round++) {
			findBestTrailsForRound(iterator, round);
		}
		
		logger.closeLog();
	}
	
	public void setLogFile(String pathname) {
		logger.setLogFile(pathname);
	}
	
	private void clearOutputDifferencesAndResetCounters() {
		outputDifferences.clear();
		temporaryOutputDifferences.clear();
		numOutputDifferences.set(0);
		numInputDifferencesProcessed.set(0);
		bestOutputDifferenceProbabilityLog.set(Integer.MAX_VALUE);
	}
	
	private void determineCumulativeSquaredProbability(List<ActiveBits> bestDifferences) {
		double probability = 0.0;
		double probabilitySquared = 0.0;
		double sum = 0.0;
		
		for (ActiveBits activeBits : bestDifferences) {
			probability = Math.pow(2, -activeBits.probabilityLog);
			probabilitySquared = probability * probability;
			sum += probabilitySquared; 
		}
		
		sum = Math.log10(sum) / Math.log10(2);
		sum /= 2;
		
		logger.log(String.format("sqrt(Sum Pr^2): 2^{%s}", NumberFormat.getInstance(Locale.ENGLISH).format(sum)));
	}
	
	private int determineNumRuns() {
		final int numInputDifferences = bestDifferences.size();
		
		if (numInputDifferences < numDifferencesToProcessPerRun) {
			return 1;
		} else {
			return (int)Math.ceil(
				(double)numInputDifferences / (double)numDifferencesToProcessPerRun
			);
		}
	}
	
	private void findBestTrailsForRound(BranchIterator iterator, int round) {
		logger.logRound(round);
		
		if (bestDifferences.isEmpty()) {
			return;
		}
		
		// Prepare 
		clearOutputDifferencesAndResetCounters();
		
		// Find number of runs
		final int numRuns = determineNumRuns();
		int startFrom = 0;
		
		for (int i = 0; i < numRuns; i++) {
			// Perform the search
			startAndWaitForTasks(iterator, bestDifferences, outputDifferences, startFrom);
			
			// Move the found output differences to the temporary buffer 
			// and clear the output buffer
			move(outputDifferences, temporaryOutputDifferences);
			
			sortForProbability(temporaryOutputDifferences);
			temporaryOutputDifferences = prune(temporaryOutputDifferences);
			
			startFrom += numDifferencesToProcessPerRun;
		}
		
		// Store the results
		bestDifferences.clear();
		move(temporaryOutputDifferences, bestDifferences);
		
		// Merge duplicates and prune again
		if (numRuns > 1) {
			merge(bestDifferences, outputDifferences);
			logger.log(String.format("Sorting %d elements w.r.t. probability", bestDifferences.size()));
			sortForProbability(bestDifferences);
			bestDifferences = prune(bestDifferences);
			logger.log(String.format("Storing %d differences", bestDifferences.size()));
		}
		
		// Log to file and console and present the results
		logger.logNDifferentialsWithLowestProbability(bestDifferences, 3);
		logger.logDifferencesToFile(bestDifferences);
		determineCumulativeSquaredProbability(bestDifferences);
	}
	
	private void merge(List<ActiveBits> source, Map<String, ActiveBits> destination) {
		String key;
		
		for (ActiveBits outputDifference : source) {
			key = outputDifference.hashKey();
			
			if (destination.containsKey(key)) {
				destination.get(key).merge(outputDifference.probabilityLog);
			} else {
				destination.put(key, outputDifference);
			}
		}
		
		source.clear();
		source.addAll(destination.values());
		destination.clear();
	}
	
	private void move(Map<String, ActiveBits> source, List<ActiveBits> destination) {
		destination.addAll(source.values());
		source.clear();
	}
	
	private void move(List<ActiveBits> source, List<ActiveBits> destination) {
		destination.addAll(source);
		source.clear();
	}
	
	/** 
	 * Prune the tree, only store those output differences with the smallest probabilities
	 * @param outputDifferences
	 * @return 
	 */
	private List<ActiveBits> prune(List<ActiveBits> differences) {
		final int numDifferences = differences.size() > this.numDifferencesToStore ?
			this.numDifferencesToStore : differences.size();
		
		List<ActiveBits> result = new ArrayList<ActiveBits>(numDifferences);
		
		for (int i = 0; i < numDifferences; i++) {
			result.add(differences.get(i));
		}
		
		return result;
	}
	
	/**
	 * Sorting for probability allows us to have the differentials with highest probability
	 * at the beginning of the list, so we can prune the list simply afterwards.
	 * 
	 * Since {@code ActiveBits} implements the {@link Comparable} interface, we simply
	 * have to sort the list.
	 * @param differences
	 */
	private void sortForProbability(List<ActiveBits> differences) {
		Collections.sort(differences, PROBABILITY_COMPARATOR);
	}
	
	/**
	 * Executes the branch-&-bound search with the help of multiple threads.
	 * @param iterator
	 * @param inputDifferences
	 * @param outputDifferences
	 * @param startFrom 
	 */
	private void startAndWaitForTasks(BranchIterator iterator, 
		List<ActiveBits> inputDifferences, Map<String, ActiveBits> outputDifferences, int startFrom) {
		
		// Preparing a pool for our tasks
		final int numDifferencesToProcess = numDifferencesToProcessPerRun < inputDifferences.size() ? 
			numDifferencesToProcessPerRun : inputDifferences.size();
		final int numTasks = numDifferencesToProcess < 16 ? 
			numDifferencesToProcess : 16;
		
		final ExecutorService threadPool = Executors.newFixedThreadPool(numTasks);
		final CompletionService<Object> poolService = new ExecutorCompletionService<Object>(threadPool);
		
		// Creating and starting tasks
		final int interval = numDifferencesToProcess / numTasks;
		int from = startFrom;
		int to = startFrom + interval;
		Callable<Object> task;
		
		for (int i = 0; i < numTasks; i++) {
			task = new BranchAndBoundTask(from, to, iterator.clone(), this);
			poolService.submit(task);
			
			from = to;
			to += interval;
			
			if (i + 2 == numTasks) {
				to = numDifferencesToProcess;
			}
		}
		
		// Shutting tasks down and waiting for them
		threadPool.shutdown();
		
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}











