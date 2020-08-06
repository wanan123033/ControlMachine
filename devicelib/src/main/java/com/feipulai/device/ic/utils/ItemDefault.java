package com.feipulai.device.ic.utils;


import java.util.HashMap;
import java.util.Map;

/**
 * 作者 吴国强
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * created 2017/9/27 9:32
 */
public class ItemDefault {

    public static final int CODE_HW = 1;        //身高、体重代码
    public static final int CODE_FHL = 2;        //肺活量代码
    public static final int CODE_LDTY = 3;    //立定跳远代码
    public static final int CODE_MG = 4;        //摸高代码
    public static final int CODE_FWC = 5;        //俯卧撑代码
    public static final int CODE_YWQZ = 6;    //仰卧起坐代码
    public static final int CODE_ZWTQQ = 7;    //坐位体前屈
    public static final int CODE_TS = 8;        //跳绳
    public static final int CODE_SL = 9;        //视力
    public static final int CODE_YTXS = 10;    //引体向上
    public static final int CODE_HWSXQ = 11;    //红外实心球
    public static final int CODE_ZCP = 12;    //中长跑（800,1000...）
    public static final int CODE_PQ = 13;        //排球
    public static final int CODE_LQYQ = 14;    //篮球运球
    public static final int CODE_ZFP = 15;    //红外计时仪
    public static final int CODE_1500MJBZ = 16;//1500米健步走
    public static final int CODE_2000MJBZ = 17;//2000米健步走
    public static final int CODE_50M = 18;    //50M
    public static final int CODE_ZQYQ = 19;    //足球运球
    public static final int CODE_TJZ = 20;    //踢毽子
    public static final int CODE_YY = 21;        //游泳
    public static final int CODE_GPS = 0xff;
    public static final int CODE_WLJ = 22;//握力计
    public static final int CODE_JGCJ = 24;//激光测距
    public static final int CODE_SHOOT = 26;//一分钟投篮26

    public static char[] keyA = {0xff, 0xff, 0xff, 0xff, 0xff, 0xff};//默认密码
    public static final byte[] ctrlW = {(byte) 0xff, (byte) 0x07, (byte) 0x80, (byte) 69};//密码控制区

    // 每个项目主机号范围
    public static final Map<Integer, Integer> HOST_IDS_MAP = new HashMap<>();

    static {
        // 每个项目对应的主机号范围
        HOST_IDS_MAP.put(ItemDefault.CODE_HW, 10);
        HOST_IDS_MAP.put(ItemDefault.CODE_FHL, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_LDTY, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_MG, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_FWC, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_YWQZ, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_ZWTQQ, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_TS, 9);
        HOST_IDS_MAP.put(ItemDefault.CODE_YTXS, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_HWSXQ, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_PQ, 9);
        HOST_IDS_MAP.put(ItemDefault.CODE_ZFP, 3);
        HOST_IDS_MAP.put(ItemDefault.CODE_LQYQ, 10);
        HOST_IDS_MAP.put(ItemDefault.CODE_ZQYQ, 10);
        HOST_IDS_MAP.put(ItemDefault.CODE_ZCP, 15);
        HOST_IDS_MAP.put(ItemDefault.CODE_WLJ, 10);
        HOST_IDS_MAP.put(ItemDefault.CODE_JGCJ, 10);
        HOST_IDS_MAP.put(ItemDefault.CODE_SL, 10);
        HOST_IDS_MAP.put(ItemDefault.CODE_SHOOT, 10);
    }
}
