package com.orhanobut.logger.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES是一种对称加密算法;
 * 注意:DES加密和解密过程中,密钥字节数都必须是8的倍数;
 */
public class DESTool{
	
	private DESTool(){}
	
	/**
	 * 加密,返回加密后的字节数组
	 * 如果加密失败,返回null
	 */
	public static byte[] encrypt(byte[] datasource,String password){
		byte[] result = null;
		SecureRandom random = new SecureRandom();
		try{
			DESKeySpec desKeySpec = new DESKeySpec(password.getBytes());
			// 创建一个密匙工厂，然后用它把 DESKeySpec 转换成 秘钥
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE,secretKey,random);
			// 获取数据并加密
			result = cipher.doFinal(datasource);
		}catch(InvalidKeyException e){
			e.printStackTrace();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}catch(NoSuchPaddingException e){
			e.printStackTrace();
		}catch(BadPaddingException e){
			e.printStackTrace();
		}catch(InvalidKeySpecException e){
			e.printStackTrace();
		}catch(IllegalBlockSizeException e){
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 解密,返回解密后的字节数组
	 * 如果解密失败,返回null
	 */
	public static byte[] decrypt(byte[] src,String password){
		byte[] result = null;
		SecureRandom random = new SecureRandom();
		try{
			DESKeySpec desKeySpec = new DESKeySpec(password.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKeySpec);
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE,securekey,random);
			// 解密
			result = cipher.doFinal(src);
		}catch(InvalidKeyException e){
			e.printStackTrace();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}catch(NoSuchPaddingException e){
			e.printStackTrace();
		}catch(BadPaddingException e){
			e.printStackTrace();
		}catch(InvalidKeySpecException e){
			e.printStackTrace();
		}catch(IllegalBlockSizeException e){
			e.printStackTrace();
		}
		return result;
	}
	
	//测试
	//public static void main(String args[]) throws IOException{
	//
	//	File encryptFile = new File("./encrypt.txt");
	//	File decryptFile = new File("./decrypt.txt");
	//
	//	// 密码，字节长度要是8的倍数
	//	String password = "12345678";
	//
	//	String[] toEncrypt = {"abc","反映到父母你卡不大","fdaqetgfda","该和接起来观看连接器风很大块话费卡的时刻恢复IQ和你分开去",
	//			"iuofhdqwirboipmfdshnficumdfhkj;m vfmadskhfkhasdfhkdh富华达卡夫卡爱德华咖啡壶aksuhfkahsdkfmuih"};
	//
	//	FileOutputStream fos = new FileOutputStream(encryptFile,true);
	//
	//	System.out.println("encryt");
	//	byte[] tmp = new byte[8];
	//	int strByteLength;
	//	for(int i = 0;i < toEncrypt.length;i++){
	//		strByteLength = toEncrypt[i].getBytes().length;
	//		int byteLength;
	//		for(int j = 0;j < strByteLength / 8;j++){
	//			if((j + 1) * 8 > strByteLength){
	//				Arrays.fill(tmp,(byte)0);
	//				byteLength = strByteLength - 8 * j;
	//			}else{
	//				byteLength = 8;
	//			}
	//			System.arraycopy(toEncrypt[i].getBytes(),8 * j,tmp,0,byteLength);
	//			byte[] result = encrypt(tmp,password);
	//			System.out.println(result.length == tmp.length);
	//			fos.write(result);
	//		}
	//	}
	//	fos.close();
	//
	//	FileInputStream fis = new FileInputStream(encryptFile);
	//	fos = new FileOutputStream(decryptFile,true);
	//	System.out.println("decrypt");
	//	byte[] readBytes = new byte[8];
	//	while(fis.read(readBytes) == 8){
	//		fos.write(decrypt(readBytes,password));
	//	}
	//	fis.close();
	//	fos.close();
	//	System.out.println("finish");
	//}
	
}