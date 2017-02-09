package de.mslab.analyzers.differential;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.CipherFactory;
import de.mslab.ciphers.Simon128;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.Simon48;
import de.mslab.ciphers.Simon64;
import de.mslab.ciphers.Simon96;
import de.mslab.ciphers.Speck32;

@SuppressWarnings("unused")
public class DifferentialTool {
	
	private static enum Direction { FORWARDS, BACKWARDS };
	
	public static void main(String[] args) {
		performAnalysis(
			"simon32", 
			1, 
			Direction.FORWARDS, 
			new int[]{},  
			new int[]{},  
			new int[]{1,2,3,4,5,9,10,11,12,13,14,15},  
			new int[]{0,1,2,3,4,5,6,7,9,10,11,12,13,14,15}  
		);
	}
	
	private static void performAnalysis(String cipherName, int fromRound, Direction direction,  
		int[] leftActiveBits, int[] rightActiveBits, int[] leftUnknownBits, int[] rightUnknownBits) {
		
		final Cipher cipher = CipherFactory.instantiateCipher(cipherName);
		final DifferentialAnalyzer analyzer = new SimonDifferentialAnalyzer(cipher);
		
		if (direction == Direction.FORWARDS) {
			analyzer.propagateFurtherForwards(fromRound, leftActiveBits, rightActiveBits);
		} else { 
			analyzer.propagateFurtherBackwards(fromRound, leftActiveBits, rightActiveBits);
		}
	}
	
	private static void performRotationalAnalysis(String cipherName, int fromRound, Direction direction,  
		int[] leftActiveBits, int[] rightActiveBits) {
		
		final Cipher cipher = CipherFactory.instantiateCipher(cipherName);
		final SimonRotationalAnalyzer analyzer = new SimonRotationalAnalyzer(cipher);
		final int[][] secretKeyErrors = {
			null, {}, {}, {}, {}
		};
		analyzer.initializeSecretKeyErrors(secretKeyErrors);
		
		if (direction == Direction.FORWARDS) {
			analyzer.propagateDifferentialForwards(fromRound, leftActiveBits, rightActiveBits);
		} else { 
			analyzer.propagateDifferentialForwards(fromRound, leftActiveBits, rightActiveBits);
		}
	}
	
}