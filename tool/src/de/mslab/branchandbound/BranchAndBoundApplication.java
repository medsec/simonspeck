package de.mslab.branchandbound;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import de.mslab.branchandbound.BranchAndBoundTool.Direction;
import de.mslab.branchandbound.iterators.BranchIterator;
import de.mslab.branchandbound.iterators.SimonBackwardsIterator;
import de.mslab.branchandbound.iterators.SimonForwardsIterator;
import de.mslab.branchandbound.iterators.SpeckBackwardsIterator;
import de.mslab.branchandbound.iterators.SpeckForwardsIterator;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.CipherFactory;
import de.mslab.ciphers.Simon;

public class BranchAndBoundApplication {
	
	private static final Options options = new Options();
	
	private static Cipher cipher;
	private static String cipherName;
	private static Direction direction;
	private static ActiveBits inputDifference;
	
	private static int fromRound;
	private static int toRound;
	
	private static int maxNumElements;
	private static int numDifferencesToStore;
	private static int numDifferencesToProcessPerRun;
	private static double probabilityLogOverhead;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException {
		try {
			if (readAndParseArgs(args)) {
				startTool();
			}
		} catch (ParseException e) {
			printHelp();
		}
	}
	
	private static boolean readAndParseArgs(String[] args) throws ParseException {
		options.addOption("d", "direction", true, "Direction, must be <fw|bw>. Defaults to <fw>.");
		options.addOption("c", "cipher", true, "Cipher, must be <simon<2n>|speck<2n>>. <2n> must be <32|48|64|96|128>. Defaults to <simon32>.");
		options.addOption("i", "in", true, "Input difference. Defaults to <0x0, 0x40>.");
		options.addOption("f", "fromround", true, "From round, must be > 0. Defaults to <1>.");
		options.addOption("t", "toround", true, "To round, must be > 0 and > fromround. Defaults to <10>.");
		options.addOption("m", "maxnumdiff", true, "Max #output differences, must be > 0. Defaults to <262144>.");
		options.addOption("n", "diffstore", true, "#differences stored, must be > 0. Defaults to <32768>.");
		options.addOption("o", "diffperrun", true, "#differences per run, must be > 0. Defaults to <256>.");
		options.addOption("p", "problogo", true, "Probability log overhead, must be > 0. Defaults to <8.0>.");
		options.addOption("h", "help", false, "Prints help.");
		
		final CommandLineParser parser = new PosixParser();
		final CommandLine commandLine = parser.parse(options, args);
		
		if (commandLine.hasOption("h")) {
			printHelp();
			return false;
		}
		
		parseDirection(commandLine.getOptionValue("d", "fw"));
		parseCipher(commandLine.getOptionValue("c", "simon32"));
		parseInputDifference(commandLine.getOptionValue("i", "0,40"));
		parseRounds(commandLine.getOptionValue("f", "1"), commandLine.getOptionValue("t", "10"));
		parseParams(
			commandLine.getOptionValue("m", "262144"), 
			commandLine.getOptionValue("n", "32768"), 
			commandLine.getOptionValue("o", "256"), 
			commandLine.getOptionValue("p", "8.0")
		);
		return true;
	}
	
	private static void parseInputDifference(String inputDifferenceString) {
		String[] parts = inputDifferenceString.split(",");
		final long left = Long.parseLong(parts[0], 16);
		final long right = Long.parseLong(parts[1], 16);
		inputDifference = new ActiveBits(left, right);
	}
	
	private static void parseCipher(String cipherNameString) {
		cipherName = cipherNameString;
		cipher = CipherFactory.instantiateCipher(cipherName);
	}
	
	private static void parseDirection(String directionString) {
		if (directionString.equals("fw")) {
			direction = Direction.FORWARDS;
		} else if (directionString.equals("bw")) {
			direction = Direction.BACKWARDS;
		} else {
			throw new Error(
				String.format("Direction %s unknown. Must be 'fw' or 'bw'.", directionString)
			);
		}
	}
	
	private static void parseParams(String maxNumElementsString, String numDifferencesToStoreString, 
		String numDifferencesToProcessPerRunString, String probabilityLogOverheadString) throws NumberFormatException {
		
		maxNumElements = Integer.parseInt(maxNumElementsString);
		
		if (maxNumElements < 1) {
			throw new IllegalArgumentException("#maxNumElements must be > 0.");
		}
		
		numDifferencesToStore = Integer.parseInt(numDifferencesToStoreString);
		
		if (numDifferencesToStore < 1) {
			throw new IllegalArgumentException("#numDifferencesToStore must be > 0.");
		}
		
		numDifferencesToProcessPerRun = Integer.parseInt(numDifferencesToProcessPerRunString);
		
		if (numDifferencesToProcessPerRun < 1) {
			throw new IllegalArgumentException("#numDifferencesToProcessPerRun must be > 0.");
		}
		
		probabilityLogOverhead = Float.parseFloat(probabilityLogOverheadString);
		
		if (probabilityLogOverhead < 0.0001) {
			throw new IllegalArgumentException("#probabilityLogOverhead must be > 0.");
		}
		
	}
	
	private static void parseRounds(String fromRoundString, String toRoundString) {
		fromRound = Integer.parseInt(fromRoundString);
		toRound = Integer.parseInt(toRoundString);
		
		if (fromRound < 1) {
			throw new IllegalArgumentException(
				String.format("FromRound must be greater than 0. Given %d", fromRound)
			);
		}
		
		if (toRound < 1) {
			throw new IllegalArgumentException(
				String.format("ToRound must be greater than 0. Given %d", toRound)
			);
		}
		
		if (fromRound > toRound) {
			throw new IllegalArgumentException(
				String.format("FromRound must be <= toRound. Given %d, %d", fromRound, toRound)
			);
		}
	}
	
	private static void startTool() throws IOException {
		final BranchIterator iterator = instantiateIterator(direction, cipher);
		iterator.setMaxNumElements(maxNumElements);
		
		final BranchAndBoundTool tool = new BranchAndBoundTool();
		tool.setNumDifferencesToStore(numDifferencesToStore);
		tool.setNumDifferencesToProcessPerRun(numDifferencesToProcessPerRun);
		tool.setProbabilityLogOverhead(probabilityLogOverhead);
		
		if (direction.equals(Direction.FORWARDS)) {
			tool.setLogFile("results/" + cipherName + "_forwards.txt");
			tool.findBestTrailsForwards(fromRound, toRound, iterator, inputDifference);
		} else {
			tool.setLogFile("results/" + cipherName + "_backwards.txt");
			tool.findBestTrailsBackwards(fromRound, toRound, iterator, inputDifference);
		}
	}
	
	private static BranchIterator instantiateIterator(Direction direction, Cipher cipher) {
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
	
	private static void printHelp() {
		final HelpFormatter formatter = new HelpFormatter();
		final String syntax = "Branch-&-bound algorithm for differential search";
		formatter.printHelp(syntax, options);
	}
	
}
