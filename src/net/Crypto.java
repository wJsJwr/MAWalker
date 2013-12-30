package net;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.apache.commons.codec.binary.*;

import walker.Info;

public class Crypto {
	private static final String BaseSecretKey = "uH9JF2cHf6OppaC1";
	//private static final String UnusedSecretKey = "A1dPUcrvur2CRQyl";	
	
	
	
	private static String GetSecretKey(boolean useLoginId) {
		String pw = BaseSecretKey;
		if (useLoginId) pw += Info.LoginId;
		while(pw.length() < 32) pw += "0";
		return pw;
	}
	
	private static String encrypt2Base64(String toEncrypt,boolean useLoginId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(useLoginId).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, keyspec);
		return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes()));
	}

	public static String Encrypt2Base64NoKey(String toEncrypt) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		return encrypt2Base64(toEncrypt, false);
	}
	
	public static String Encrypt2Base64WithKey(String toEncrypt) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return encrypt2Base64(toEncrypt, true);
	}
	
	public static String DecryptBase64NoKey2Str(String cyphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(false).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return new String(c.doFinal(Base64.decodeBase64(cyphertext)));
	}
	public static String DecryptBase64WithKey2Str(String cyphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(true).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return new String(c.doFinal(Base64.decodeBase64(cyphertext)));
	}

	private static byte[] decrypt2Bytes(byte[] ciphertext, boolean useLoginId) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		SecretKeySpec keyspec = new SecretKeySpec(GetSecretKey(useLoginId).getBytes(),"AES");
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		c.init(Cipher.DECRYPT_MODE, keyspec);
		return c.doFinal(ciphertext);
	}
	
	public static byte[] DecryptNoKey(byte[] Ciphertext) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		return decrypt2Bytes(Ciphertext, false);
	}

	public static byte[] DecryptWithKey(byte[] ciphertext) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return decrypt2Bytes(ciphertext, true);
	}
}
