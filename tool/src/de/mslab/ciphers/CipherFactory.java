package de.mslab.ciphers;

public class CipherFactory {
	
	public static Cipher instantiateCipher(String cipherName) {
		switch(cipherName) {
			case Simon32.NAME: return new Simon32();
			case Simon48.NAME: return new Simon48();
			case Simon64.NAME: return new Simon64();
			case Simon96.NAME: return new Simon96();
			case Simon128.NAME:return new Simon128();
			case Speck32.NAME: return new Speck32();
			case Speck48.NAME: return new Speck48();
			case Speck64.NAME: return new Speck64();
			case Speck96.NAME: return new Speck96();
			case Speck128.NAME:return new Speck128();
			default:
				throw new Error("Unknown cipher name: " + cipherName);
		}
	}
	
}
