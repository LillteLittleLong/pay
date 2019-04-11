package com.shangfu.pay.reconciliation.reconciliation.util;

import cn.hutool.core.util.CharsetUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by tinlly to 2019/3/7
 * Package for com.example.demo.encryption
 */
public class RsaEncryption {

    public List<Key> initKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 获取 Rsa 密钥对生成器
        KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
        // 指定密钥大小 , 随机生成 key 大小
        rsa.initialize(1024 , new SecureRandom());
        // 生成密钥对生成器
        KeyPair keyPair = rsa.generateKeyPair();
        // 获取公钥
        PublicKey aPublic = keyPair.getPublic();
        // 获取密钥
        PrivateKey aPrivate = keyPair.getPrivate();

        byte[] publicKey = Base64.getEncoder().encode(aPublic.getEncoded());
        byte[] privateKey = Base64.getEncoder().encode(aPrivate.getEncoded());

        return codingKey(publicKey , privateKey);
    }

    public List<Key> codingKey(byte[] publicKeyByte, byte[] privateKeyByte) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] publicKey = Base64.getDecoder().decode(publicKeyByte);
        byte[] privateKey = Base64.getDecoder().decode(privateKeyByte);

        // 获取 RSA 密钥生成器
        KeyFactory rsa1 = KeyFactory.getInstance("RSA");

        // 包装公钥
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        // 包装密钥
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        // 生成处理后的公钥 密钥
        PublicKey publicKey1 = rsa1.generatePublic(x509EncodedKeySpec);
        PrivateKey privateKey1 = rsa1.generatePrivate(pkcs8EncodedKeySpec);

        ArrayList<Key> keys = new ArrayList<>();
        keys.add(publicKey1);
        keys.add(privateKey1);

        return keys;
    }

    /**
     * 加密
     * @param content 明文
     * @param publicKey1 公钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     */
    public static String encrypt(String content , PublicKey publicKey1) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, UnsupportedEncodingException {
        // 开始真正的加密
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE , publicKey1);
        byte[] bytes = Base64.getEncoder().encode(rsa.doFinal(content.getBytes()));
        System.out.println("公钥加密后的数据 => " + new String(Base64.getEncoder().encode(bytes)));
        return new String(Base64.getEncoder().encode(bytes) , CharsetUtil.UTF_8);
        //return bytes;
    }

    /**
     * 解密
     * @param bytes 密文
     * @param privateKey1 私钥
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String decrypt(String bytes , PrivateKey privateKey1) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        byte[] cipherText = Base64.getDecoder().decode(bytes);
        Cipher rsa2 = Cipher.getInstance("RSA");
        rsa2.init(Cipher.DECRYPT_MODE , privateKey1);
        byte[] bytes1 = rsa2.doFinal(cipherText);
        System.out.println("密钥解密后的数据 => " + new String(bytes1));
        return new String(bytes1);
    }

}