package com.feipulai.device.ic.utils;

/**
 * Created by James on 2018/10/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ICBlockIndex {

    //IC卡共16个扇区(Sector)，每个扇区4个块(Block)，每个块可以存储16个字节(byte)。
    // 1 sector = 4 blocks
    // 1 block = 16 bytes
    // 第一扇区的第一块存取了厂家的信息,已固化不可用;    block[0] 不可用
    // 每个扇区的第4块为密码控制块，用于加密;           block[4 * n + 3] 不可用
    // NFC读取数据时没有扇区这种说法,只是读取块,文档中所有与扇区相关都要转换为块

    // 学生信息块号
    // 第一扇区用于存储考生信息
    // 包括准考证号(考号)，姓名，性别，因为第一块已固化,从第二块开始存储数据
    public static final int[] STU_INFO_BLOCK_NO = {1, 2};
    public static final int[] LIN_NAN_STU_INFO_BLOCK_NO = {60, 61, 62};
    // 扩展区(存放单位等信息放在第60,61,62块),共48个字节
    // BYTE[0]---表示项目属性描述页所占块数 目前为6
    // BYTE[1]---表示考生考试项目数
    // 单位：byte[2]—byte[21]    20个字节，10个汉字
    // 学年：byte[22]—byte[29]   8个字节  第61块，6字节到13字节为学年。
    public static final int[] EXPAND_BLOCK_NO = {60, 61, 62};

    // 这里数据从第二个扇区开始,也即开始块号为4(从0开始),
    // 默认属性页起始块号
    public static final int START_BLOCK_NO = 4;        //默认属性页起始块号

    // 属性页 存储项目属性,每个项目属性存在如下内容:
    // 机器码(1字节)	 存储地址起始块号(1字节)	所占存储块数(1字节)	组次(2字节)	道次(1字节)
    public static final int[] ITEM_PROPERTY_BLOCK_NO = {4, 5, 6, 8, 9, 10};


    /**
     * 返回指定数据块后的下一个可存储数据块的块号
     */
    public static int addBlockNo(int blockNo) {
        // 第一扇区的第一块存取了厂家的信息,已固化不可用;    block[0] 不可用
        // 每个扇区的第4块为密码控制块，用于加密;           block[4 * n + 3] 不可用
        blockNo = (blockNo + 1) % 4 == 3 ? (blockNo + 2) : (blockNo + 1);
        return blockNo;
    }

}
