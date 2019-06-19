package com.feipulai.host.utils;
/**
 * 深圳市菲普莱体育发展有限公司		秘密级别：绝密
 */


import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpUtils{
	
	private static final String TOKEN = "fpl@*!";
	
	/**
	 * MD5加密算法
	 */
	public static String getMD5(String str) throws Exception{
		try{
			return DigestUtils.md5Hex(str.getBytes("UTF-8"));
		}catch(Exception e){
			throw new Exception("MD5加密失败",e);
		}
	}
	
	//MD5加密
	//	1.将接口需要的参数和值组装成数组
	//	2.将数组中的参数进行字典排序
	//	3.将排序好的数组字符串拼接成一个字符串,在再该字符串的末尾连接一个自行约定的字符串
	//	4.将这串字符MD5加密
	//	5.将加密之后的MD5值作为请求接口的参数传递
	public static String getSignature(Map<String,String> paramMap){
		try{
			StringBuilder stringBuilder = new StringBuilder();
			List<String> list = new ArrayList<>();
			if(paramMap != null && paramMap.size() != 0){
				for(Map.Entry<String,String> entry : paramMap.entrySet()){
					list.add(entry.getKey());
					list.add(entry.getValue());
				}
			}
			//字典排序
			Collections.sort(list);
			for(int i = 0;i < list.size();i++){
				stringBuilder.append(list.get(i));
			}
			stringBuilder.append(TOKEN);
			return getMD5(stringBuilder.toString());
		}catch(Exception e){
			return null;
		}
	}
	
}
