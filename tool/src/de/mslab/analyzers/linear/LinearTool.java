package de.mslab.analyzers.linear;
import de.mslab.ciphers.Cipher;
import de.mslab.ciphers.CipherFactory;
import de.mslab.ciphers.Simon128;
import de.mslab.ciphers.Simon32;
import de.mslab.ciphers.Simon48;
import de.mslab.ciphers.Simon64;
import de.mslab.ciphers.Simon96;
import de.mslab.ciphers.Speck32;

@SuppressWarnings("unused")
public class LinearTool {
	
	private static enum Direction { FORWARDS, BACKWARDS };
	
	public static void main(String[] args) {
		performAnalysis(
			"simon32", 
			13, 
			Direction.BACKWARDS, 
			new int[]{8},  
			new int[]{}  
		);
	}
	
	private static void performAnalysis(String cipherName, int fromRound, Direction direction,  
		int[] leftActiveBits, int[] rightActiveBits) {
		
		final Cipher cipher = CipherFactory.instantiateCipher(cipherName);
		final LinearAnalyzer analyzer = new SimonLinearAnalyzer(cipher);
		
		if (direction == Direction.FORWARDS) {
			analyzer.propagateForwards(fromRound, leftActiveBits, rightActiveBits);
		} else { 
			analyzer.propagateBackwards(fromRound, leftActiveBits, rightActiveBits);
		}
	}
	
}