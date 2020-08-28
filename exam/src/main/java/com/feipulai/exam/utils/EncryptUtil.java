package com.feipulai.exam.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.StringChineseUtil;
import com.feipulai.exam.netUtils.HttpResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.Key;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密工具，对称加密算法，采用随机Key
 */
public class EncryptUtil {

    /**
     * 解密是否成功的校验码
     */
    private static String CHECK_CODE = "fp2018";

    // 以下为生成方法
    private static final String BASESTRING = "0123456789abcdefghijklmnopqrstuvwxyz";

    /**
     * 解密是否成功的校验码
     */
    private static String AES_KEY = CHECK_CODE;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String randomString(String baseString, int length) {
        final StringBuilder sb = new StringBuilder();

        if (length < 1) {
            length = 1;
        }
        int baseLength = baseString.length();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
//            int number = getRandom().nextInt(baseLength);
            int number = random.nextInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 签名内容包含：  bizType的值 +  data的值
     *
     * @param signData
     * @return
     */
    public static String getSignData(String signData) {
//        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
//        String data = gson.toJson(signData);
//        String sign = DigestUtils.sha1Hex(data);
        String sign = new String(Hex.encodeHex(DigestUtils.sha1(signData)));
        Logger.i("getSignData===>" + sign);
        String randomS = randomString(BASESTRING, 10);
        AES_KEY = CHECK_CODE + randomS;
        String startString = sign.substring(0, 8);
        String endString = sign.substring(8, sign.length());
        return startString + randomS + endString;
//        return startString + AES_KEY + endString;
    }

    /**
     * 加密数据
     *
     * @param paramsData
     * @return
     */
    public static String setEncryptData(Object paramsData) {
        // 加密
        SecretKey secretKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encoderStr = cipher.doFinal(gson.toJson(paramsData).getBytes("UTF-8"));
            String hexE = new String(Hex.encodeHex(encoderStr));
            return hexE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 加密数据
     *
     * @param paramsData
     * @return
     */
    public static String setEncryptString(String key, String paramsData) {
        // 加密
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encoderStr = cipher.doFinal(paramsData.getBytes("UTF-8"));
            String hexE = new String(Hex.encodeHex(encoderStr));
            LogUtil.logDebugMessage("加密16：" + StringChineseUtil.parseByte2HexStr(encoderStr));
            LogUtil.logDebugMessage("加密：" + StringChineseUtil.encode(encoderStr));
            return hexE;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 解密数据
     *
     * @param encryptData
     * @return
     */
    public static String setDecodeData(String encryptData, SecretKey secretKey) {

        try {
            Cipher cipher = Cipher.getInstance("AES");
//            Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decode = Hex.decodeHex(encryptData.toCharArray());
            byte[] decodeStr = cipher.doFinal(decode);
//            int index = paddingIndex(decodeStr);
//            byte[] noPaddingBytes = new byte[index];
//            System.arraycopy(decodeStr, 0, noPaddingBytes, 0, index);
            return new String(decodeStr, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取补齐的位置
     *
     * @param paddingBytes 源数组
     * @return 补齐的位置
     */
    private static int paddingIndex(byte[] paddingBytes) {
        for (int i = paddingBytes.length - 1; i >= 0; i--) {
            if (paddingBytes[i] != 6) {
                return i + 1;
            }
        }
        return -1;
    }

    public static String decodeHttpData(HttpResult data) {
        // 原始签名
        String sign = data.getSign();

        // 分离签名
        String a = sign.substring(0, 8);
        String b = sign.substring(8, 18);
        String c = sign.substring(18);

        // 正常签名
        String normalSign = a + c;

        // 数据
        String json = data.getBody().toString();

        // 加密key
        String key = CHECK_CODE + b;
        System.out.println("key: " + key);
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        String jsonData = setDecodeData(json, secretKey);
        return jsonData;
    }
}
