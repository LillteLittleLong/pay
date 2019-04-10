
package com.shangfudata.collpay.util;

import cn.hutool.crypto.digest.MD5;

import java.io.UnsupportedEncodingException;
import java.util.*;

//import org.apache.commons.codec.digest.DigestUtils;

public class SignUtils {

	public static String sign(Map<String, String> reqMap, String signKey) {
		Collection<String> keyset= reqMap.keySet(); 
		List list=new ArrayList<String>(keyset);
		Collections.sort(list);
		String signStr = "";
		for(int i = 0; i < list.size(); i++) {
			signStr += "&" + list.get(i) + "=" + reqMap.get(list.get(i));
		}
		signStr = signStr.substring(1) + "&key=" + signKey;
		System.out.println("签名串:" + signStr);
		String sign = null;
		try {
			//sign = DigestUtils.md5Hex(signStr.getBytes("utf-8"));
			sign = new MD5().digestHex(signStr.getBytes("utf-8"));
			//byteToHex(signStr.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sign;
	}
}
