package de.mslab.util;

public class StringUtil {
	
	public static String toBinary(long value) {
		final String s = "01";
		final long v = 0xFFFFFFFFL;
		long mask = 1L << 31;
		String result = "";
		
		for (int i = 0; i < 32; i++) {
			if ((value & mask) != 0) {
				result += s.charAt(1);
			} else {
				result += s.charAt(0);
			}
			
			mask = (mask >> 1) & v;
		}
		
		return result;
	}
	
	public static String to16BitBinary(long value) {
		final String s = "01";
		final long v = 0xFFFFFFFFL;
		long mask = 1L << 15;
		String result = "";
		
		for (int i = 0; i < 16; i++) {
			if ((value & mask) != 0) {
				result += s.charAt(1);
			} else {
				result += s.charAt(0);
			}
			
			mask = (mask >> 1) & v;
		}
		
		return result;
	}
	
	public static String to48BitBinary(long value) {
		final String s = "01";
		final long v = 0xFFFF_FFFFFFFFL;
		long mask = 1L << 47;
		String result = "";
		
		for (int i = 0; i < 48; i++) {
			if ((value & mask) != 0) {
				result += s.charAt(1);
			} else {
				result += s.charAt(0);
			}
			
			mask = (mask >> 1) & v;
		}
		
		return result;
	}
	
	public static String toNBitBinary(long value, int n) {
		long mask = 1L;
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < n; i++) {
			if (i != 0 && (i & 0x7) == 0) {
				buffer.append(' ');
			}
			
			if ((value & mask) != 0) {
				buffer.append('1');
			} else {
				buffer.append('0');
			}
			
			mask <<= 1;
		}
		
		buffer.reverse();
		return buffer.toString();
	}
	
}
