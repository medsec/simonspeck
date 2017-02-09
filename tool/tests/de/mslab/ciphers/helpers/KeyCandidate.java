package de.mslab.ciphers.helpers;

public class KeyCandidate {
	
	public long[] keys;

	public KeyCandidate() {
		this.keys = new long[4];
	}
	
	public KeyCandidate(long k0) {
		this.keys = new long[]{ k0 };
	}

	public KeyCandidate(long k0, long k1) {
		this.keys = new long[]{ k0, k1 };
	}

	public KeyCandidate(long k0, long k1, long k2) {
		this.keys = new long[]{ k0, k1, k2 };
	}

	public KeyCandidate(long k0, long k1, long k2, long k3) {
		this.keys = new long[]{ k0, k1, k2, k3 };
	}
	
	public KeyCandidate(long[] keys) {
		this.keys = keys;
	}
	
}
