package com.shangfu.pay.reconciliation.reconciliation.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * aes加解密工具类
 */
public class AesUtils {

	public static String CHARSET = "UTF-8";
	public static String keyAlgorithm = "AES";
	public static String cipherAlgorithm = "AES/ECB/PKCS5Padding";

	/**
	 * 计算字符串的MD5值
	 * 
	 * @param str
	 * @return
	 */
	public static String md5(String str) {
		if (null == str) {
			return null;
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		byte[] md5Bytes = md5.digest(str.getBytes());
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString().toUpperCase();
	}

	/**
	 * AES加密
	 * 
	 * @param content
	 * @param password
	 * @return
	 */
	public static String aesEn(String content, String password) {
		if (null == content) {
			return null;
		}
		try {
			byte[] plainBytes = content.getBytes(CHARSET);
			password = password.substring(0, 16);
			byte[] keyBytes = password.getBytes(CHARSET);
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			SecretKeySpec secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encryptedBytes = cipher.doFinal(plainBytes);

			return parseByte2HexStr(encryptedBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AES解密
	 * 
	 * @param content
	 * @param password
	 * @return
	 */
	public static String aesDe(String content, String password) {
		if (null == content||"".equals(content)) {
			return null;
		}
		try {
			byte[] encryptedBytes = parseHexStr2Byte(content);
			password = password.substring(0, 16);
			byte[] keyBytes = password.getBytes(CHARSET);
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			SecretKeySpec secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			return new String(decryptedBytes, CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

}
