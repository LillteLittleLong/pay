package com.shangfudata.gatewaypay.util;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 
 * @author shangfu715
 *	RSA加解密、签名验签
 */
public class RSAUtils {

	/**
	 * 从字符串中加载私钥
	 * @param privateKeyStr
	 * @throws Exception
	 */
	public static RSAPrivateKey loadPrivateKey(String privateKeyStr) throws Exception{
		try {
			byte[] decode = Base64.getDecoder().decode(privateKeyStr);
			/*BASE64Decoder base64Decoder= new BASE64Decoder();
			byte[] buffer= base64Decoder.decodeBuffer(privateKeyStr);*/
			PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(decode);
			KeyFactory keyFactory= KeyFactory.getInstance("RSA");
			RSAPrivateKey privateKey= (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}

	}
	/**
	 * 从字符串中加载公钥
	 * @param publicKeyStr 公钥数据字符串
	 * @throws Exception 加载公钥时产生的异常
	 */
	public static RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception{
		try {
			/*BASE64Decoder base64Decoder= new BASE64Decoder();
			byte[] buffer= base64Decoder.decodeBuffer(publicKeyStr);*/
			byte[] decode = Base64.getDecoder().decode(publicKeyStr);
			KeyFactory keyFactory= KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec= new X509EncodedKeySpec(decode);
			RSAPublicKey publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空");
		}
	}


    /**
     * 	公钥加密
     * @param data
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String publicKeyEncrypt(String data,RSAPublicKey publicKey) throws Exception {
    	Cipher cipher =Cipher.getInstance("RSA");
    	cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    	byte[] doFinal = cipher.doFinal(data.getBytes("UTF-8"));
    	return new String(Base64.getEncoder().encode(doFinal));
    }
	/**
	 * 私钥解密
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String privateKeyDecrypt(String data,RSAPrivateKey privateKey) throws Exception{
		Cipher cipher =Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] decode = Base64.getDecoder().decode(data);
		byte[] doFinal = cipher.doFinal(decode);
		return new String(doFinal,"UTF-8");
	}


    
    /**
     * 	私钥加密
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String privateKeyEncrypt(String data,RSAPrivateKey privateKey) throws Exception {
    	Cipher cipher =Cipher.getInstance("RSA");
    	cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    	byte[] doFinal = cipher.doFinal(data.getBytes("UTF-8"));
    	return new String(Base64.getEncoder().encode(doFinal));
    }
	/**
	 * 	公钥解密
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static String publicKeyDecrypt(String data,RSAPublicKey publicKey) throws Exception{
		Cipher cipher =Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		byte[] decode = Base64.getDecoder().decode(data);
		byte[] doFinal = cipher.doFinal(decode);
		return new String(doFinal,"UTF-8");

	}

	public static final String SIGN_ALGORITHMS = "MD5WithRSA";

	/**
	 * 私钥签名
	 * @param content  签名的参数内容
	 * @param privateKey  用于签名私钥
	 * @return
	 */
	public static String sign(String content, RSAPrivateKey privateKey) {
		String charset = "utf-8";
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(privateKey.getEncoded());
			KeyFactory keyf = KeyFactory.getInstance("RSA");
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(charset));

			byte[] signed = signature.sign();
			return Base64.getEncoder().encodeToString(signed);
			//return Base64.getEncoder().encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 公钥验签
	 * @param content  	验签的参数内容
	 * @param sign		签名
	 * @param publicKey	用于验签的公钥
	 * @return
	 */
	public static boolean doCheck(String content, String sign, RSAPublicKey publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			//byte[] encodedKey = Base64.getDecoder().decode(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey.getEncoded()));

			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);

			signature.initVerify(pubKey);
			signature.update(content.getBytes("utf-8"));

			boolean bverify = signature.verify(Base64.getDecoder().decode(sign));
			return bverify;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}



}
