package com.feipulai.device.ic.utils;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;


import com.feipulai.device.ic.entity.ExpandInfo;
import com.feipulai.device.ic.entity.ItemProperty;
import com.feipulai.device.ic.entity.ItemResult;
import com.feipulai.device.ic.entity.StuInfo;

import java.io.UnsupportedEncodingException;

/**
 * 作者 王伟、吴国强
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * created 2017/9/27 9:31
 */
public class IC_ResultResolve {

    // 解析获取考生信息
    // 准考证号（学号）：20字节（不够20字节，后面填0）
    // 姓名：11个字节（不够11字节，后面填0）
    // 性别：1个字节 0x00 –男 0x01-女
    public static StuInfo getStuInfo(byte[] data) {
        byte[] stuCodeBytes = new byte[20];
        System.arraycopy(data, 0, stuCodeBytes, 0, 20);
        String stuCode = Converter.decode(stuCodeBytes).trim();

        byte[] stuNameBytes = new byte[11];
        System.arraycopy(data, 20, stuNameBytes, 0, 11);
        String stuName = Converter.decode(stuNameBytes).trim();

        int sex = (data[31] & 0xff) == 0 ? 0 : 1;

        return new StuInfo(stuCode, stuName, sex);
    }

    // 解析获取考生信息
    public static StuInfo getLinNanStuInfo(byte[] data) {

//        账号(低位在前)  15              0           0~3           4
//        姓名            15              0           4~13          10
//        身份代码        15              0           14~15         2
//        身份证号1       15              1           0~7           8
//        身份证号2       15              1           8~11          4
//        性别代码        15              1           12            1
//        国籍代码        15              1           13            1
//        民族代码        15              1           14            1
//        保留代码        15              1           15            1
//        学工号          15              2           0~15          15

        byte[] stuCodeBytes = new byte[15];
        System.arraycopy(data, 32, stuCodeBytes, 0, 15);
        String stuCode = Converter.decode(stuCodeBytes).trim();

        byte[] stuNameBytes = new byte[10];
        System.arraycopy(data, 4, stuNameBytes, 0, 10);
        String stuName = Converter.decode(stuNameBytes).trim();

        int sex = (data[28] & 0xff) == 0 ? 0 : 1;

        return new StuInfo(stuCode, stuName, sex);
    }


    // 将写入考生数据解析成byte数组
    public static byte[] getRawStuInfo(StuInfo stuInfo) {
        try {
            byte[] stuData = new byte[ICBlockIndex.STU_INFO_BLOCK_NO.length * 16];
            byte[] stuCode = stuInfo.getStuCode().getBytes("GBK");
            // 准考证号
            System.arraycopy(stuCode, 0, stuData, 0, stuCode.length > 20 ? 20 : stuCode.length);
            // 姓名
            byte[] stuName = stuInfo.getStuName().getBytes("GBK");
            System.arraycopy(stuName, 0, stuData, 20, stuName.length > 20 ? 20 : stuName.length);
            // 性别
            if (stuInfo.getSex() == 0) {
                // 男
                stuData[31] = (byte) 0x00;
            } else {
                // 女
                stuData[31] = (byte) 0x01;
            }
            return stuData;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    public static ExpandInfo getExpandInfo(byte[] data) {
        /**
         * 扩展区(存放单位等信息放在第60,61,62块),共48个字节
         * BYTE[0]---表示项目属性描述页所占块数 目前为6
         * BYTE[1]---表示考生考试项目数
         * byte[2]—byte[21]    单位,20个字节，10个汉字
         * byte[22]—byte[29]   学年,8个字节,2017090120180631,表示有效时间从2017年9月1日---2018年6月31日为有效测量时间,其余时间测量无效
         * 其中年--2个字节,月--1个字节,日---1个字节
         * byte[30]-byte[47]   18个字节，用于扩展信息,文档中写的是如果项目属性超过16个,这里存储项目属性页,但与小胖确认,最多16个项目,该部分未使用
         */
        int projectPropertyBlockSum = data[0] & 0xff;

        int testProjectSum = data[1] & 0xff;

        byte[] organizationBytes = new byte[20];
        System.arraycopy(data, 2, organizationBytes, 0, 20);
        String organization = Converter.decode(organizationBytes).trim();

        byte[] schoolYearBytes = new byte[8];
        System.arraycopy(data, 22, schoolYearBytes, 0, 8);

        StringBuilder sb = new StringBuilder(16);
        int startYear = ((schoolYearBytes[0] & 0xff) << 8) + (schoolYearBytes[1] & 0xff);
        sb.append(String.format("%04d", startYear));
        int startMonth = (schoolYearBytes[2] & 0xff);
        sb.append(String.format("%02d", startMonth));
        int startDay = (schoolYearBytes[3] & 0xff);
        sb.append(String.format("%02d", startDay));
        int endYear = ((schoolYearBytes[4] & 0xff) << 8) + (schoolYearBytes[5] & 0xff);
        sb.append(String.format("%04d", endYear));
        int endMonth = (schoolYearBytes[6] & 0xff);
        sb.append(String.format("%02d", endMonth));
        int endDay = schoolYearBytes[7] & 0xff;
        sb.append(String.format("%02d", endDay));

        String schoolYear = sb.toString();

        return new ExpandInfo(projectPropertyBlockSum, testProjectSum, organization, schoolYear);
    }

    public static byte[] getRawExpandInfo(ExpandInfo expandInfo) {
        try {
            byte[] result = new byte[48];
            result[0] = (byte) (expandInfo.getItemPropertyBlockSum() & 0xff);
            result[1] = (byte) (expandInfo.getTestProjectSum() & 0xff);

            byte[] organizationBytes = expandInfo.getOrganization().getBytes("GBK");
            System.arraycopy(organizationBytes, 0, result, 2, organizationBytes.length);

            //byte[] schoolYearBytes = expandInfo.getSchoolYear().getBytes("GBK");
            //System.arraycopy(schoolYearBytes,0,result,22,schoolYearBytes.length);
            String schoolYear = expandInfo.getSchoolYear();
            StringBuilder sb = new StringBuilder(16);
            int startYear = Integer.parseInt(schoolYear.substring(0, 4));
            int startMonth = Integer.parseInt(schoolYear.substring(4, 6));
            int startDay = Integer.parseInt(schoolYear.substring(6, 8));
            result[22] = (byte) ((startYear & 0xff_00) >> 8);
            result[23] = (byte) (startYear & 0xff);
            result[24] = (byte) (startMonth & 0xff);
            result[25] = (byte) (startDay & 0xff);

            int endYear = Integer.parseInt(schoolYear.substring(8, 12));
            int endMonth = Integer.parseInt(schoolYear.substring(12, 14));
            int endDay = Integer.parseInt(schoolYear.substring(14, 16));

            result[26] = (byte) ((endYear & 0xff_00) >> 8);
            result[27] = (byte) (endYear & 0xff);
            result[28] = (byte) (endMonth & 0xff);
            result[29] = (byte) (endDay & 0xff);

            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解析项目属性页
    public static ItemProperty[] getItemProperties(byte[] data) {
        ItemProperty[] result = new ItemProperty[data.length / 6];

        for (int i = 0; i < data.length; i = i + 6) {
            // 项目属性页:机器码 (1字节) 存储地址起始块号 (1字节) 所占存储块数 (1字节) 组次 (2字节) 道次 (1字节)
            //机器码
            int machineCode = data[i] & 0xff;

            // 存储地址起始块号
            int startBlock = data[i + 1] & 0xff;

            // 所占存储块数
            int blockNum = data[i + 2] & 0xff;

            // 组次
            int groupNum = ((data[i + 3] & 0xff) << 8) + (data[i + 4] & 0xff);

            //道次
            int trackNum = data[i + 5] & 0xff;

            ItemProperty property = new ItemProperty(machineCode, startBlock, blockNum, groupNum, trackNum);

            result[i / 6] = property;
        }
        return result;
    }

    public static byte[] getRawItemProperties(ItemProperty[] itemProperties) {
        byte[] result = new byte[itemProperties.length * 6];
        ItemProperty itemProperty;
        for (int i = 0; i < itemProperties.length; i++) {
            itemProperty = itemProperties[i];
            result[6 * i] = (byte) itemProperty.getMachineCode();
            result[6 * i + 1] = (byte) itemProperty.getStartBlockNo();
            result[6 * i + 2] = (byte) itemProperty.getBlockNum();
            result[6 * i + 3] = (byte) ((itemProperty.getGroupNum() & 0xff00) >>> 8);
            result[6 * i + 4] = (byte) ((itemProperty.getGroupNum() & 0xff));
            result[6 * i + 5] = (byte) itemProperty.getTrackNum();
        }
        return result;
    }

    public static ItemResult getItemResult(@NonNull byte[] resultInBytes, int machineCode) {
        switch (machineCode) {

            case ItemDefault.CODE_HW://身高体重
            case ItemDefault.CODE_SL://视力
                return getResult1(resultInBytes);

            //无判罚值高低字节的项目
            case ItemDefault.CODE_FHL://肺活量
            case ItemDefault.CODE_LDTY://立定跳远
            case ItemDefault.CODE_MG://摸高
            case ItemDefault.CODE_ZWTQQ://坐位体前屈
            case ItemDefault.CODE_HWSXQ://红外实心球
            case ItemDefault.CODE_ZCP://中长跑(1000,800)
            case ItemDefault.CODE_LQYQ://篮球运球
            case ItemDefault.CODE_ZFP://折返跑
            case ItemDefault.CODE_1500MJBZ://1500米健步走
            case ItemDefault.CODE_2000MJBZ://2000米健步走
            case ItemDefault.CODE_50M://50米
            case ItemDefault.CODE_ZQYQ://足球运球
            case ItemDefault.CODE_YY://游泳
            case ItemDefault.CODE_SHOOT:
                return getResult2(resultInBytes);

            //有判罚值高低字节的项目
            case ItemDefault.CODE_FWC://俯卧撑
            case ItemDefault.CODE_YWQZ://仰卧起坐
            case ItemDefault.CODE_TS://跳绳
            case ItemDefault.CODE_YTXS://引体向上
            case ItemDefault.CODE_PQ://排球
            case ItemDefault.CODE_TJZ://踢毽子
                return getResult3(resultInBytes);
            default:
                Log.i("getItemResult", "机器码为:" + machineCode + "的项目不存在！");
                return null;
        }
    }

    /**
     * 解析身高体重、视力成绩
     * <p>
     * 字节地址	    存储内容	    存储格式	    备注说明
     * 0	        项目代码	    0X01/09	    身高/体重/视力
     * 1	        成绩1	    	   	    身高/左眼成绩高字节
     * 2		                	   	    身高/左眼成绩低字节
     * 3		                	   	    体重/右眼成绩高字节
     * 4		                	   	    体重/右眼成绩低字节
     * 5		                	   	    成绩标识
     * 6		                	   	    判罚值 // 这个没有用
     * 7		                	   	    拓展位
     * 8	        成绩2	    	   	    身高/左眼成绩高字节
     * 9		                	   	    身高/左眼成绩低字节
     * 10		                	   	    体重/右眼成绩高字节
     * 11 		                	   	    体重/右眼成绩低字节
     * 12	    	            	   	    成绩标识
     * 13		                	   	    判罚值 // 这个没有用
     * 14		                	   	    拓展位
     * 15
     */
    public static ItemResult getResult1(byte[] dataInBytes) {

        int machineCode = dataInBytes[0] & 0xff;

        int blockSum = dataInBytes.length / 16;

        int result[] = new int[4 * blockSum];
        int validState[] = new int[4 * blockSum];
        int foulState[] = new int[4 * blockSum];
        int penalValue[] = new int[4 * blockSum];
        int retain[] = new int[4 * blockSum];

        int base;
        int resultIndex;

        for (int i = 0; i < 2 * blockSum; i++) {

            base = 7 * i + i / 2;

            resultIndex = 2 * i;

            result[resultIndex] = ((dataInBytes[base + 1] & 0x7f) << 8) | (dataInBytes[base + 2] & 0xff);
            result[resultIndex + 1] = ((dataInBytes[base + 3] & 0x7f) << 8) | (dataInBytes[base + 4] & 0xff);

            validState[resultIndex] = validState[resultIndex + 1] = (dataInBytes[base + 5] >>> 7) == 1
                    ? ItemResult.RESULT_STATE_VALID : ItemResult.RESULT_STATE_INVALID;
            foulState[resultIndex] = foulState[resultIndex + 1] = (dataInBytes[base + 5] >>> 6) == 1
                    ? ItemResult.RESULT_STATE_FOUL : ItemResult.RESULT_STATE_UN_FOUL;

            retain[resultIndex] = retain[resultIndex + 1] = dataInBytes[base + 7] & 0xff;
        }

        return new ItemResult(machineCode, result, validState, foulState, penalValue, retain);
    }

    /**
     * 获取 肺活量/立定跳远/摸高/坐位体前屈/红外实心球
     * 中长跑/篮球运球/折返跑/1500米健步走/2000米健步走/50米/足球运球/游泳
     * <p>
     * 字节地址      存储内容
     * 0              项目代码
     * 1 成绩1       成绩最高字节
     * 2             成绩次高字节
     * 3             成绩次低字节
     * 4             成绩最低字节
     * 5             成绩标识
     * 6             判罚值    // 这个没有用
     * 7             拓展位
     * 8 成绩2      成绩最高字节
     * 9             成绩次高字节
     * 10            成绩次低字节
     * 11            成绩最低字节
     * 12            成绩标识
     * 13            判罚值    // 这个没有用
     * 14            拓展位
     * 15
     */
    public static ItemResult getResult2(byte[] dataInBytes) {
        // 一个块可以存储两个成绩
        int blockSum = dataInBytes.length / 16;

        int machineCode = dataInBytes[0] & 0xff;

        int result[] = new int[2 * blockSum];
        int validState[] = new int[2 * blockSum];
        int foulState[] = new int[2 * blockSum];
        int penalValue[] = new int[2 * blockSum];
        int retain[] = new int[2 * blockSum];

        int base;
        int sign;
        int value;
        for (int i = 0; i < 2 * blockSum; i++) {
            base = 7 * i + i / 2;

            sign = (dataInBytes[base + 1] & 0x80) >>> 7;
            value = ((dataInBytes[base + 1] & 0x7f) << 24)
                    | ((dataInBytes[base + 2] & 0xff) << 16)
                    | ((dataInBytes[base + 3] & 0xff) << 8)
                    | (dataInBytes[base + 4] & 0xff);
            if (sign == 1) {  // 负值
                result[i] = -value;
            } else {
                result[i] = value;
            }
            validState[i] = (dataInBytes[base + 5] >>> 7) == 1 ? ItemResult.RESULT_STATE_VALID : ItemResult.RESULT_STATE_INVALID;
            foulState[i] = (dataInBytes[base + 5] >>> 6) == 1 ? ItemResult.RESULT_STATE_FOUL : ItemResult.RESULT_STATE_UN_FOUL;
            retain[i] = dataInBytes[base + 7] & 0xff;
        }
        return new ItemResult(machineCode, result, validState, foulState, penalValue, retain);
    }

    /**
     * 解析 俯卧撑、仰卧起坐、跳绳、引体向上、排球、踢毽子
     * <p>
     * 字节地址	    存储内容
     * 0	        项目代码
     * 1    成绩1   第一次成绩高字节
     * 2            第一次成绩低字节
     * 3            第一次判罚高字节    裁判判罚犯规次数值
     * 4            第一次判罚低字节
     * 5            成绩标识
     * 6            判罚值  // 这个没有用
     * 7            拓展位
     * 8    成绩2   第二次成绩高字节
     * 9            第二次成绩低字节
     * 10           第二次判罚高字节
     * 11           第二次判罚低字节
     * 12           成绩标识
     * 13           判罚值  // 这个没有用
     * 14           拓展位
     * 15
     * 真实成绩 = 计数值 – 判罚值(判罚高字节和判罚低字节决定,最高位为1时为负值)
     */
    public static ItemResult getResult3(byte[] dataInBytes) {
        // 一个块可以存储两个成绩
        int blockSum = dataInBytes.length / 16;

        int machineCode = dataInBytes[0] & 0xff;

        int result[] = new int[2 * blockSum];
        int validState[] = new int[2 * blockSum];
        int foulState[] = new int[2 * blockSum];
        int penalValue[] = new int[2 * blockSum];
        int retain[] = new int[2 * blockSum];

        int base;
        int sign;
        int value;
        for (int i = 0; i < 2 * blockSum; i++) {
            base = 7 * i + i / 2;

            sign = (dataInBytes[base + 1] & 0x80) >>> 7;
            value = ((dataInBytes[base + 1] & 0x7f) << 8) | (dataInBytes[base + 2] & 0xff);
            // 负值
            if (sign == 1) {
                result[i] = -value;
            } else {
                result[i] = value;
            }

            sign = (dataInBytes[base + 3] & 0x80) >>> 7;
            value = ((dataInBytes[base + 3] & 0x7f) << 8) | (dataInBytes[base + 4] & 0xff);
            // 负值
            if (sign == 1) {
                penalValue[i] = -value;
            } else {
                penalValue[i] = value;
            }

            validState[i] = (dataInBytes[base + 5] >>> 7) == 1 ? ItemResult.RESULT_STATE_VALID : ItemResult.RESULT_STATE_INVALID;
            foulState[i] = (dataInBytes[base + 5] >>> 6) == 1 ? ItemResult.RESULT_STATE_FOUL : ItemResult.RESULT_STATE_UN_FOUL;

            retain[i] = dataInBytes[base + 7] & 0xff;
        }
        return new ItemResult(machineCode, result, validState, foulState, penalValue, retain);
    }

    public static byte[] getRawResult(ItemProperty itemProperty, ItemResult itemResult, int machineCode) {
        switch (machineCode) {

            case ItemDefault.CODE_HW://身高体重
            case ItemDefault.CODE_SL://视力
                return IC_ResultResolve.getRawResult1(itemProperty, itemResult);

            case ItemDefault.CODE_FHL://肺活量
            case ItemDefault.CODE_LDTY://立定跳远
            case ItemDefault.CODE_MG://摸高
            case ItemDefault.CODE_ZWTQQ://坐位体前屈
            case ItemDefault.CODE_HWSXQ://红外实心球
            case ItemDefault.CODE_ZCP://中长跑(1000,800)
            case ItemDefault.CODE_LQYQ://篮球运球
            case ItemDefault.CODE_ZFP://折返跑
            case ItemDefault.CODE_1500MJBZ://1500米健步走
            case ItemDefault.CODE_2000MJBZ://2000米健步走
            case ItemDefault.CODE_50M://50米
            case ItemDefault.CODE_ZQYQ://足球运球
            case ItemDefault.CODE_YY://游泳
                return IC_ResultResolve.getRawResult2(itemProperty, itemResult);

            case ItemDefault.CODE_FWC://俯卧撑
            case ItemDefault.CODE_YWQZ://仰卧起坐
            case ItemDefault.CODE_TS://跳绳
            case ItemDefault.CODE_YTXS://引体向上
            case ItemDefault.CODE_PQ://排球
            case ItemDefault.CODE_TJZ://踢毽子
                return IC_ResultResolve.getRawResult3(itemProperty, itemResult);
            default:
                Log.i("IC_WriteItemResult", "机器码为:" + machineCode + "的项目不存在！");
                return null;
        }
    }

    /**
     * 将身高体重、视力成绩转换为可存储至IC卡中的字节流
     * <p>
     * 字节地址	    存储内容	    存储格式	    备注说明
     * 0	        项目代码	    0X01	    身高/体重
     * 1	        成绩1	    	        身高/左眼成绩高字节
     * 2		                            身高/左眼成绩低字节
     * 3		                            体重/右眼成绩高字节
     * 4		                	        体重/右眼成绩低字节
     * 5		                	        成绩标识
     * 6		                	        判罚值     // 不使用这个值
     * 7		                	   	    拓展位
     * 8	        成绩2	    	   	    身高/左眼成绩高字节
     * 9		                	   	    身高/左眼成绩低字节
     * 10		                	   	    体重/右眼成绩高字节
     * 11 		                	   	    体重/右眼成绩低字节
     * 12	    	            	   	    成绩标识
     * 13		                	        判罚值     // 不使用这个值
     * 14		                	   	    拓展位
     * 15
     */
    public static byte[] getRawResult1(ItemProperty itemProperty, ItemResult itemResult) {

        int blockNum = itemProperty.getBlockNum();

        byte[] resultData = new byte[blockNum * 16];

        int resultIndex;
        for (int i = 0; i < blockNum; i++) {
            resultData[16 * i] = (byte) (itemProperty.getMachineCode() & 0xff);
        }
        int[] result = itemResult.getResult();
        for (int i = 0; i < 2 * blockNum; i++) {

            int base = 7 * i + i / 2;
            resultIndex = 2 * i;

            resultData[base + 1] = (byte) ((result[resultIndex] & 0xff00) >> 8);
            resultData[base + 2] = (byte) (result[resultIndex] & 0xff);

            resultData[base + 3] = (byte) ((result[resultIndex + 1] & 0xff00) >> 8);
            resultData[base + 4] = (byte) (result[resultIndex + 1] & 0xff);

            byte resultFlag = 0;
            if (itemResult.getValidState()[resultIndex] == ItemResult.RESULT_STATE_VALID) {
                resultFlag = (byte) 0x80;
            }
            if (itemResult.getFoulState()[resultIndex] == ItemResult.RESULT_STATE_FOUL) {
                resultFlag |= 0x40;
            }

            resultData[base + 5] = resultFlag;
            resultData[base + 7] = (byte) (itemResult.getRetain()[resultIndex] & 0xff);
        }
        return resultData;
    }

    /**
     * 将肺活量、立定跳远、摸高、坐位体前屈、红外实心球、中长跑(1000,800)、篮球运球
     * 折返跑、1500米健步走、2000米健步走、50米、足球运球、游泳成绩转换为可存储至IC卡中的字节流
     * <p>
     * 字节地址       存储内容
     * 0             项目代码
     * 1成绩1         成绩最高字节
     * 2             成绩次高字节
     * 3             成绩次低字节
     * 4             成绩最低字节
     * 5             成绩标识 // 不使用这个值
     * 6             判罚值
     * 7             拓展位
     * 8成绩2        成绩最高字节
     * 9             成绩次高字节
     * 10            成绩次低字节
     * 11            成绩最低字节
     * 12            成绩标识 // 不使用这个值
     * 13            判罚值
     * 14            拓展位
     * 15
     */
    private static byte[] getRawResult2(ItemProperty itemProperty, ItemResult itemResult) {

        int blockNum = itemProperty.getBlockNum();

        byte[] resultData = new byte[blockNum * 16];

        for (int i = 0; i < blockNum; i++) {
            resultData[16 * i] = (byte) (itemProperty.getMachineCode() & 0xff);
        }

        int result[] = itemResult.getResult();
        for (int i = 0; i < 2 * blockNum; i++) {

            int base = 7 * i + i / 2;

            int resultVal = result[i];
            if (resultVal < 0) {
                resultVal = Math.abs(resultVal);
                resultVal |= 0x80_00_00_00;
            }

            byte[] convertedBytes = Converter.intToByteArray(resultVal);
            System.arraycopy(convertedBytes, 0, resultData, base + 1, 4);

            byte resultFlag = 0;
            if (itemResult.getValidState()[i] == ItemResult.RESULT_STATE_VALID) {
                resultFlag = (byte) 0x80;
            }
            if (itemResult.getFoulState()[i] == ItemResult.RESULT_STATE_FOUL) {
                resultFlag |= 0x40;
            }

            resultData[base + 5] = resultFlag;
            resultData[base + 7] = (byte) (itemResult.getRetain()[i] & 0xff);
        }
        return resultData;

    }

    /**
     * 解析俯卧撑、仰卧起坐、跳绳、引体向上、排球、踢毽子
     * <p>
     * 字节地址	    存储内容
     * 0	        项目代码
     * 1    成绩1    第一次成绩高字节
     * 2            第一次成绩低字节
     * 3            第一次判罚高字节
     * 4            第一次判罚低字节
     * 5            成绩标识
     * 6            判罚值
     * 7            拓展位
     * 8成绩2    第二次成绩高字节
     * 9         第二次成绩低字节
     * 10        第二次判罚高字节
     * 11        第二次判罚低字节
     * 12           成绩标识
     * 13           判罚值
     * 14           拓展位
     * 15
     */
    private static byte[] getRawResult3(ItemProperty itemProperty, ItemResult itemResult) {

        int blockNum = itemProperty.getBlockNum();

        byte[] resultData = new byte[blockNum * 16];

        for (int i = 0; i < blockNum; i++) {
            resultData[16 * i] = (byte) (itemProperty.getMachineCode() & 0xff);
        }

        int result[] = itemResult.getResult();
        int penalValue[] = itemResult.getPenalValue();
        int[] validState = itemResult.getValidState();
        int[] foulState = itemResult.getFoulState();

        for (int i = 0; i < 2 * blockNum; i++) {

            int base = 7 * i + i / 2;

            int resultVal = result[i];
            if (resultVal < 0) {
                resultVal = Math.abs(resultVal);
                resultVal |= 0x80_00;
            }
            resultData[base + 1] = (byte) (resultVal >> 8);
            resultData[base + 2] = (byte) (resultVal & 0xff);

            int penalVal = penalValue[i];
            if (penalVal < 0) {
                penalVal = Math.abs(resultVal);
                penalVal |= 0x80_00;
            }
            resultData[base + 3] = (byte) (penalVal >> 8);
            resultData[base + 4] = (byte) (penalVal & 0xff);

            byte resultFlag = 0;
            if (validState[i] == ItemResult.RESULT_STATE_VALID) {
                resultFlag = (byte) 0x80;
            }
            if (foulState[i] == ItemResult.RESULT_STATE_FOUL) {
                resultFlag |= 0x40;
            }

            resultData[base + 5] = resultFlag;
            resultData[base + 7] = (byte) (itemResult.getRetain()[i] & 0xff);
        }
        return resultData;
    }

}
