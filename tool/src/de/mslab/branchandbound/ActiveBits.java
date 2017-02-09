package de.mslab.branchandbound;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import de.mslab.util.ArrayUtil;

public class ActiveBits implements Comparable<ActiveBits> {
	
	public long previousLeft;
	public long previousRight;
	
	/**
	 * The active bits in the left part of the output difference of the trail.
	 */
	public long left;
	/**
	 * The active bits in the right part of the output difference of the trail.
	 */
	public long right;
	/**
	 * The -log2 of the probability of the trail.
	 * E.g. if the trail \alpha \to \beta has a probability of 2^{-9}, this
	 * value is {@code 9}.  
	 */
	public double probabilityLog;
	/**
	 * Indicates how many paths exist to this difference. 
	 */
	public int numPaths = 1;
	
	private static final double LOG2 = Math.log10(2);
	
	public ActiveBits() {
		
	}
	
	public ActiveBits(long left, long right) {
		this.left = left;
		this.right = right;
	}
	
	public ActiveBits(int[] left, int[] right) {
		this.left = ArrayUtil.activeBitsToLong(left);
		this.right = ArrayUtil.activeBitsToLong(right);
	}
	
	public ActiveBits(long left, long right, double probabilityLog, int numPaths) {
		this.left = left;
		this.right = right;
		this.probabilityLog = probabilityLog;
		this.numPaths = numPaths;
	}
	
	public ActiveBits(int[] left, int[] right, double probabilityLog, int numPaths) {
		this.left = ArrayUtil.activeBitsToLong(left);
		this.right = ArrayUtil.activeBitsToLong(right);
		this.probabilityLog = probabilityLog;
		this.numPaths = numPaths;
	}
	
	public ActiveBits(long left, long right, double probabilityLog, int numPaths, 
		long previousLeft, long previousRight) {
		
		this.left = left;
		this.right = right;
		this.probabilityLog = probabilityLog;
		this.numPaths = numPaths;
		this.previousLeft = previousLeft;
		this.previousRight = previousRight;
	}
	
	public int compareTo(ActiveBits other) {
		if (this.left < other.left) {
			return -1;
		} else if (this.left > other.left) {
			return 1;
		}
		
		if (this.right < other.right) {
			return -1;
		} else if (this.right > other.right) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof ActiveBits)) {
			return false;
		}
		
		final ActiveBits instance = (ActiveBits)other;
		return this.left == instance.left
			&& this.right == instance.right;
	}
	
	public String hashKey() {
		return left + "" + right;
	}
	
	public void merge(double otherProbabilityLog) {
		final double probability = Math.pow(2, -this.probabilityLog) + Math.pow(2, -otherProbabilityLog);
		this.probabilityLog = -(Math.log10(probability) / LOG2);
		this.numPaths++;
	}
	
	public String toString() {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(3);
		
		final String paths = numPaths <= 1 ? "" : " " + numPaths + " paths ";
		return String.format("%s %s Pr: 2^{-%s} %s%s %s", 
			Arrays.toString(ArrayUtil.activeBitsToArray(left)), 
			Arrays.toString(ArrayUtil.activeBitsToArray(right)), 
			nf.format(probabilityLog), 
			paths, 
			Arrays.toString(ArrayUtil.activeBitsToArray(previousLeft)), 
			Arrays.toString(ArrayUtil.activeBitsToArray(previousRight))
		);
	}
	
}
