package de.mslab.ciphers.helpers;

public class MaskSet {
	
	public long filterMask = 0b00000001_01010101;
	public long[] initialPatterns = {
		0b00000000_00000010,
		0b00000000_00001000,
		0b00000000_00100000,
		0b00000000_10000000,
		0b00000010_00000000,
		0b10000000_00000000
	};
	public long[] initialReplacements = {
		0b00000001_00000000,
		0b00000100_00000000,
		0b00001000_00000000,
		0b00010000_00000000,
		0b00100000_00000000,
		0b01000000_00000000
	};
	public long[] patterns;
	public long[] replacements;
	
}