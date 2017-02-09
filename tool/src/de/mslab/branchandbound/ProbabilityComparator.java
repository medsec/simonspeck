package de.mslab.branchandbound;

import java.util.Comparator;

public class ProbabilityComparator implements Comparator<ActiveBits> {
	
	public int compare(ActiveBits first, ActiveBits second) {
		if (first.probabilityLog == second.probabilityLog) {
			return 0;
		} else if (first.probabilityLog < second.probabilityLog) {
			return -1;
		} else {
			return 1;
		}
	}
	
}
