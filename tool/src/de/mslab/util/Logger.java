package de.mslab.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import de.mslab.branchandbound.ActiveBits;

public class Logger {

	private File logFile;
	private OutputStreamWriter writer;
	
	public void closeLog() {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void log(Object message) {
		System.out.println(message);
		
		try {
			if (writer != null) {
				writer.write(message + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs the {@code n} differences with lowest probability.
	 * Requires the differences list to be sorted before, but does not sort the differences list. 
	 * @param differences
	 * @param n
	 */
	public void logNDifferentialsWithLowestProbability(List<ActiveBits> differences, int n) {
		ActiveBits difference;
		
		if (differences.size() < n) {
			n = differences.size();
		}
		
		for (int i = 0; i < n; i++) {
			difference = differences.get(i);
			log((i + 1) + " " + difference);
		}
	}
	
	public void logRound(int round) {
		log("-------------------------------------");
		log("Round " + round);
		log("-------------------------------------");
	}
	
	public void logDifferencesToFile(List<ActiveBits> differences) {
		try {
			final int numDifferences = differences.size();
			ActiveBits element;
			
			for (int i = 0; i < numDifferences; i++) {
				element = differences.get(i);
				writer.write(element.toString() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void openLog() throws IOException {
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
		
		OutputStream stream = new FileOutputStream(logFile);
		writer = new OutputStreamWriter(stream);
	}
	
	public void setLogFile(String pathname) {
		logFile = new File(pathname);
	}
	
}
