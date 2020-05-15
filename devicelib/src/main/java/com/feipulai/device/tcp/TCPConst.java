package com.feipulai.device.tcp;

/**
 * Created by ww on 2018/1/8.
 */

public class TCPConst {
    public static final int SCHEDULE = -1;
    public static final int GAME = -2;
    public static final int SORT = -3;
    public static final int EVENT = -4;
    public static final int TRACK = -5;//径赛和接力
    public static final int SENDCHECK = -6;
    public static final int FIELD = -7;//田赛
    public static final int SPORTS = -8;
    public static final int HEAD_LEN = (20 + 28);
    public static final int MAX_HIGHT_NUM = 30;   //高度增加最大个数
    public static final int MAX_FIELD_EVENT = 100;  //一个单元显示项目数目
    public static final int MAX_RECORD_COUNT = 5;   //纪录的个数
    public static final int MAX_FPPACK_LEN = (1024 * 1024); //一个数据包的长度
    public static final int MAX_RUN_SPORTER_NUM = 50;
    public static final int GTP_OFFSET_COMMANDLENGTH = 0;
    public static final int GTP_OFFSET_COMMANDID = 10;
    public static final int GTP_OFFSET_HEAD_LEN = (20 + 28);
    public static final int MAX_FIELD_SPORTER_NUM = 60;

    public static final int MAX_EVENT_PARTITION_COUNT = 100;//总项目分段数

    public enum enumEvent {//通讯事件
        EventScore(0),//上传成绩包
        EventData(1),//下发一组数据包，主要指某项目的分组分到信息
        EventAllData(2),//下发指定类型所有数据,获取分组信息和分组分到信息
        EventRequestEnd(3);//事件请求结束，服务端提示客户端某个请求结束，即使下发数据完成
        private int index;

        enumEvent(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    public enum enmuCommand {
        CommandUnkown(0),
        CommandGTResp(1),//回包
        CommandPTResult(2),
        CommandPTResp(3),
        CommandSDResult(4),
        CommandSDResp(5),
        CommandRAResult(6),
        CommandRAResp(7),
        CommandSTResult(8),
        CommandSTResp(9),
        CommandLTResult(10),
        CommandLTResp(11),
        CommandFPExit(12),
        CommandLITEResult(13),
        CommandLITEResp(14),
        CommandFPFDPack(15),
        CommandPTOnePersonResult(16),
        CommandWSResult(17),
        CommandRAOneGroupResult(18),//测距整组成绩包
        CommandHAResult(19),
        CommandFSResult(20),//远度个人成绩包
        CommandHSResult(21), //高度个人成绩
        CommandTCRollCall(22), //体测检录---曾辉2014-09-05新增
        CommandTCITResult(23), //体测项目成绩---曾辉2014-09-05新增
        CommandStickResult(24),//
        CommandStickResp(25);//
        private int index;

        enmuCommand(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

    }

    //通讯编码方式
    public enum enumCodeType {
        CodeANSI(0),//英文字符模式
        CodeGB2312(1),//简体中文
        CodeBIG5(2),//繁体中文
        CodeUincode(3);//Unicode编码
        private int index1;

        enumCodeType(int index1) {
            this.index1 = index1;
        }

        public int getIndex() {
            return this.index1;
        }

    }
}
