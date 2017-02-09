package de.mslab.ciphers.helpers;

import de.mslab.util.StringUtil;

public class TextPair {
	
	public boolean isCorrect;
	public long[] p;
	public long[] p_;
	
	public TextPair(boolean isCorrect, long[] p, long[] p_) {
		this.isCorrect = isCorrect;
		this.p = p;
		this.p_ = p_;
	}
	
	public String toString() {
		return "P:  " + StringUtil.to16BitBinary(p[0]) + " " + StringUtil.to16BitBinary(p[1]) + "\n"
			+ "P': " + StringUtil.to16BitBinary(p_[0]) + " " + StringUtil.to16BitBinary(p_[1]);
	}
	
}
