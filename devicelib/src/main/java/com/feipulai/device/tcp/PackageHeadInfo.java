package com.feipulai.device.tcp;



/**
 * Created by ww on 2018/1/5.
 */

public class PackageHeadInfo {

    public int m_nTotalLength;//协议中占10位
    public static final String packageLength="0000000000";
    public TCPConst.enmuCommand m_nCommandID;//协议中占6位

    public int m_nCryptLength;
    public int m_nCodeType;   //协议中占1位
    public int m_nEnCrypt;    //0代表不加密，1代表加密， //协议中占1位

    public char[] m_pBuf;
    public char[] m_pFPEncryptKey;
    public int m_nEncryptKeyLength;
    public int m_dwTickCount;
    public  static  final byte[] g_SepFlag1 = {1};


    public char[] g_HeartPack = { '1', 'F', 'P', '-', 'H', 'E', 'A', 'R', 'T', '2', '0' };
   //public char[] g_FPHead = { 'F', 'P', 'T', 'C', 'P', '-', 'P', 'A', 'C', 'K', 'A', 'G', 'E', '-', 'H', 'E', 'A', 'D' };
    public static final String g_FPHead = "FPTCP-PACKAGE-HEAD";
   // public char[] g_FPTail = { 'F', 'P', 'T', 'C', 'P', '-', 'P', 'A', 'C', 'K', 'A', 'G', 'E', '-', 'T', 'A', 'I', 'L' };
    public static final String g_FPTail = "FPTCP-PACKAGE-TAIL";
   // public  static  final byte[] g_SepFlag = { -30, -108, -112};//特殊分隔符的定义
    public  static  final byte[] g_SepFlag = {2};




    public int GetCommandID(String[] ssArray, PackageHeadInfo head){

        return 0;
    }


}
