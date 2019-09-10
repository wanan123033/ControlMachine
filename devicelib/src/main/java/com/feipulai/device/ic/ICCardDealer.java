package com.feipulai.device.ic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.feipulai.device.AdaptiveConfig;
import com.feipulai.device.ic.entity.ExpandInfo;
import com.feipulai.device.ic.entity.ItemProperty;
import com.feipulai.device.ic.entity.ItemResult;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.device.ic.utils.ICBlockIndex;
import com.feipulai.device.ic.utils.IC_ResultResolve;
import com.feipulai.device.ic.utils.ItemDefault;


/**
 * Created by James on 2018/10/15
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ICCardDealer {

    private static final String TAG = "ICCardDealer";

    private StuInfo mStuInfo;
    private ExpandInfo mExpandInfo;
    private ItemProperty[] mItemProperties;

    private NFCDevice nfcd;

    /**
     * 创建一个使用IC卡的处理对象
     * 注意:对每一张卡,都应该有其唯一的{@link #ICCardDealer}对象
     */
    public ICCardDealer(NFCDevice nfcd) {
        this.nfcd = nfcd;
    }

    /**
     * 读学生信息
     *
     * @return 学生信息对象, 含学号、姓名和性别等信息,读取失败时返回null
     */
    public StuInfo IC_ReadStuInfo() {
        byte[] stuData;
        switch (AdaptiveConfig.ADAPTIVE_TYPE) {
            case AdaptiveConfig.DEFAULT:
                stuData = readBlocks(ICBlockIndex.STU_INFO_BLOCK_NO);
                if (stuData != null) {
                    mStuInfo = IC_ResultResolve.getStuInfo(stuData);
                }
                break;
            case AdaptiveConfig.LIN_NAN_SHI_FAN:
                ItemDefault.keyA = "73796E707562".toCharArray();
                stuData = readBlocks(ICBlockIndex.LIN_NAN_STU_INFO_BLOCK_NO);
                if (stuData != null) {
                    mStuInfo = IC_ResultResolve.getLinNanStuInfo(stuData);
                }
                break;
        }


        //Log.i(TAG,mStuInfo.toString());
        return mStuInfo;
    }

    /**
     * 向IC卡中写入学生信息
     * 注意:在写入学生数据之前必须先读取学生信息,在读取出来的对象上修改后再写入
     *
     * @return 成功返回true, 失败时返回false
     */
    public boolean IC_WriteStuInfo() {
        if (mStuInfo == null) {
            return false;
        }
        byte[] stuData = IC_ResultResolve.getRawStuInfo(mStuInfo);
        return writeBlocks(stuData, ICBlockIndex.STU_INFO_BLOCK_NO);
    }

    /**
     * 读取扩展信息
     *
     * @return 扩展信息对象，含考生项目树、学年、单位信息等,读取失败时返回null
     */
    public ExpandInfo IC_ReadExpandInfo() {
        byte[] expandData = readBlocks(ICBlockIndex.EXPAND_BLOCK_NO);
        if (expandData != null) {
            mExpandInfo = IC_ResultResolve.getExpandInfo(expandData);
        }
        //Log.i(TAG,mExpandInfo.toString());
        return mExpandInfo;
    }

    /**
     * 写入扩展信息
     * 注意:在写入扩展信息必须先读取扩展信息,在读取出来的对象上修改再写入
     *
     * @return 成功返回true, 失败时返回false
     */
    public boolean IC_WriteExpandInfo() {
        byte[] expandData = IC_ResultResolve.getRawExpandInfo(mExpandInfo);
        return writeBlocks(expandData, ICBlockIndex.EXPAND_BLOCK_NO);
    }

    /**
     * 读取项目属性信息,目前最多16个的项目属性
     *
     * @return 具体项目属性信息数组, 每个项目属性信息包含对应项目存储在IC卡中的位置, 读取失败时返回null
     */
    public ItemProperty[] IC_ReadItemProperties() {
        byte[] itemPropertyData = readBlocks(ICBlockIndex.ITEM_PROPERTY_BLOCK_NO);
        if (itemPropertyData != null) {
            mItemProperties = IC_ResultResolve.getItemProperties(itemPropertyData);
        }

        //for(ItemProperty itemProperty : mItemProperties){
        //	Log.i(TAG,itemProperty.toString());
        //}
        return mItemProperties;
    }

    /**
     * 写入项目属性信息
     * 注意:在写入扩展信息必须先读取扩展信息,在读取出来的对象上修改再写入
     *
     * @return 成功返回true, 失败时返回false
     */
    public boolean IC_WriteItemProperties() {
        byte[] itemPropertiesData = IC_ResultResolve.getRawItemProperties(mItemProperties);
        byte[] itemPropertyBlockData = new byte[16 * 6];
        System.arraycopy(itemPropertiesData, 0, itemPropertyBlockData, 0, 16 * 6);
        return writeBlocks(itemPropertyBlockData, ICBlockIndex.ITEM_PROPERTY_BLOCK_NO);
    }

    /**
     * 指定项目是否报名
     *
     * @param machineCode 机器码
     * @return 报名了则返回true, 否则返回false
     */
    public boolean isItemRegistered(int machineCode) {
        //long startTime = System.currentTimeMillis();
        if (mItemProperties == null) {
            IC_ReadItemProperties();
        }
        //Log.i(" ",System.currentTimeMillis() - startTime + "ms");
        if (mItemProperties == null) {
            return false;
        }
        //startTime = System.currentTimeMillis();
        for (ItemProperty itemProperty : mItemProperties) {
            if (itemProperty.getMachineCode() == machineCode) {
                return true;
            }
        }
        //Log.i("ItemProperties compare",System.currentTimeMillis() - startTime + "ms");
        return false;
    }

    /**
     * 读取IC卡中指定项目成绩
     *
     * @param machineCode 机器码
     * @return 指定项目成绩信息对象, 一个该对象可能有多个成绩, 身高体重和视力成绩格式比较特殊, 具体参考 {@link ItemResult} 中的注释;
     * 读取的项目成绩单位为IC卡中的单位,与数据库中取出的数据进行比较需要先进行单位转换,具体可以参考 {@link com.feipulai.device.ic.utils.ICResultConverter}
     * 没有报名该项目时或成绩信息读取失败时返回null,因为成绩信息读取失败可能性很小,如果返回null,可以认为其就是没有报名该项目
     */
    public ItemResult IC_ReadItemResult(int machineCode) {

        ItemProperty itemProperty = null;
        if (mItemProperties == null) {
            IC_ReadItemProperties();
        }

        for (ItemProperty property : mItemProperties) {
            if (property.getMachineCode() == machineCode) {
                itemProperty = property;
                break;
            }
        }

        if (itemProperty == null) {
            return null;
        }

        int[] blocks = new int[itemProperty.getBlockNum()];

        blocks[0] = itemProperty.getStartBlockNo();

        for (int i = 1; i < blocks.length; i++) {
            blocks[i] = ICBlockIndex.addBlockNo(blocks[i - 1]);
        }

        byte[] resultInBytes = readBlocks(blocks);
        if (resultInBytes == null) {
            return null;
        }
        ItemResult itemResult = IC_ResultResolve.getItemResult(resultInBytes, machineCode);
        Log.i(TAG, itemResult.toString());
        return itemResult;
    }

    /**
     * 向IC卡中写入指定项目成绩
     *
     * @param itemResult  要写入的成绩对象,身高体重和视力成绩格式比较特殊,具体参考 {@link ItemResult} 注释;
     *                    在写入成绩到IC卡中之前,需要将成绩转换为IC卡中的单位格式,具体参考 {@link com.feipulai.device.ic.utils.ICResultConverter}
     *                    写入的成绩应该是在读取的项目成绩上进行修改,而不是新建一个成绩并设置参数,后一种方式是不安全的
     * @param machineCode 机器码
     * @return 报名了则返回true, 否则返回false
     */
    public boolean IC_WriteItemResult(ItemResult itemResult, int machineCode) {
        ItemProperty itemProperty = null;
        if (mItemProperties == null) {
            IC_ReadItemProperties();
        }

        for (ItemProperty property : mItemProperties) {
            if (property.getMachineCode() == machineCode) {
                itemProperty = property;
                break;
            }
        }

        if (itemProperty == null) {
            return false;
        }

        byte[] resultData = IC_ResultResolve.getRawResult(itemProperty, itemResult, machineCode);
        if (resultData == null) {
            // 不会发生
            return false;
        }
        int[] blocks = new int[resultData.length / 16];

        blocks[0] = itemProperty.getStartBlockNo();

        for (int i = 1; i < blocks.length; i++) {
            blocks[i] = ICBlockIndex.addBlockNo(blocks[i - 1]);
        }

        return writeBlocks(resultData, blocks);
    }

    private byte[] readBlocks(@NonNull int[] blocks) {
        int lastSector = -1;
        byte[] result = new byte[blocks.length * 16];
        for (int i = 0; i < blocks.length; i++) {
            // 每个扇区只需要验证一次密码
            boolean loadKey;
            if (blocks[i] / 4 != lastSector) {
                loadKey = nfcd.loadKey((short) 0, (short) (blocks[i] / 4), ItemDefault.keyA);
                if (!loadKey) {
                    return null;
                }
                boolean auth = nfcd.authentication((short) 0, (short) (blocks[i] / 4));
                if (!auth) {
                    return null;
                }
                lastSector = blocks[i] / 4;
            }
            char[] resultChars = nfcd.read((short) blocks[i]);
            if (resultChars == null) {
                return null;
            }
            byte[] data = new byte[resultChars.length];
            for (int j = 0; j < resultChars.length; j++) {
                data[j] = (byte) (resultChars[j] & 0xff);
            }
            System.arraycopy(data, 0, result, i * 16, 16);
            Log.i(TAG, "第" + blocks[i] + "块读取成功!");
        }
        return result;
    }

    private boolean writeBlocks(byte[] datas, int[] blocks) {
        int lastSector = -1;
        for (int i = 0; i < blocks.length; i++) {
            // 每个扇区只需要验证一次密码
            boolean loadKey;
            if (blocks[i] / 4 != lastSector) {
                loadKey = nfcd.loadKey((short) 0, (short) (blocks[i] / 4), ItemDefault.keyA);
                if (!loadKey) {
                    return false;
                }
                boolean auth = nfcd.authentication((short) 0, (short) (blocks[i] / 4));
                if (!auth) {
                    return false;
                }
                lastSector = blocks[i] / 4;
            }
            byte[] data = new byte[16];
            System.arraycopy(datas, i * 16, data, 0, 16);
            char[] toWrite = new char[data.length];
            for (int j = 0; j < data.length; j++) {
                toWrite[j] = (char) data[j];
                //data[j] = (byte)(toWrite[j] & 0xff);
            }
            nfcd.write((short) blocks[i], toWrite);
            Log.i(TAG, "第" + blocks[i] + "块写入成功！");
        }
        return true;
    }

}
