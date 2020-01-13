//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hcnetsdk.jna;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public interface HCNetSDKByJNA extends Library {
    int NET_DVR_SET_PTZPOS = 292;
    int NET_DVR_GET_PTZPOS = 293;
    int NET_DVR_GET_TIMECFG = 118;
    int NET_DVR_SET_TIMECFG = 119;
    int NET_DVR_SET_ALARMIN_PARAM_V50 = 1200;
    int NET_DVR_GET_ALARMIN_PARAM_V50 = 1201;
    int NET_DVR_GET_WEEK_PLAN_CFG = 2100;
    int NET_DVR_SET_WEEK_PLAN_CFG = 2101;
    int NET_DVR_GET_ACS_WORK_STATUS = 2123;
    int NET_DVR_GET_VERIFY_WEEK_PLAN = 2124;
    int NET_DVR_SET_VERIFY_WEEK_PLAN = 2125;
    int NET_DVR_GET_CARD_RIGHT_WEEK_PLAN = 2126;
    int NET_DVR_SET_CARD_RIGHT_WEEK_PLAN = 2127;
    int NET_DVR_GET_CARD_RIGHT_WEEK_PLAN_V50 = 2304;
    int NET_DVR_SET_CARD_RIGHT_WEEK_PLAN_V50 = 2305;
    int NET_DVR_GET_DOOR_STATUS_HOLIDAY_PLAN = 2102;
    int NET_DVR_SET_DOOR_STATUS_HOLIDAY_PLAN = 2103;
    int NET_DVR_GET_VERIFY_HOLIDAY_PLAN = 2128;
    int NET_DVR_SET_VERIFY_HOLIDAY_PLAN = 2129;
    int NET_DVR_GET_CARD_RIGHT_HOLIDAY_PLAN = 2130;
    int NET_DVR_SET_CARD_RIGHT_HOLIDAY_PLAN = 2131;
    int NET_DVR_GET_ALARMIN_PARAM_LIST_V50 = 2237;
    int NET_DVR_GET_CARD_RIGHT_HOLIDAY_PLAN_V50 = 2310;
    int NET_DVR_SET_CARD_RIGHT_HOLIDAY_PLAN_V50 = 2311;
    int NET_DVR_GET_DOOR_STATUS_HOLIDAY_GROUP = 2104;
    int NET_DVR_SET_DOOR_STATUS_HOLIDAY_GROUP = 2105;
    int NET_DVR_GET_VERIFY_HOLIDAY_GROUP = 2132;
    int NET_DVR_SET_VERIFY_HOLIDAY_GROUP = 2133;
    int NET_DVR_GET_CARD_RIGHT_HOLIDAY_GROUP = 2134;
    int NET_DVR_SET_CARD_RIGHT_HOLIDAY_GROUP = 2135;
    int NET_DVR_GET_CARD_RIGHT_HOLIDAY_GROUP_V50 = 2316;
    int NET_DVR_SET_CARD_RIGHT_HOLIDAY_GROUP_V50 = 2317;
    int NET_DVR_GET_DOOR_STATUS_PLAN_TEMPLATE = 2106;
    int NET_DVR_SET_DOOR_STATUS_PLAN_TEMPLATE = 2107;
    int NET_DVR_GET_VERIFY_PLAN_TEMPLATE = 2136;
    int NET_DVR_SET_VERIFY_PLAN_TEMPLATE = 2137;
    int NET_DVR_GET_CARD_RIGHT_PLAN_TEMPLATE = 2138;
    int NET_DVR_SET_CARD_RIGHT_PLAN_TEMPLATE = 2139;
    int NET_DVR_GET_CARD_RIGHT_PLAN_TEMPLATE_V50 = 2322;
    int NET_DVR_SET_CARD_RIGHT_PLAN_TEMPLATE_V50 = 2323;
    int NET_DVR_GET_DOOR_CFG = 2108;
    int NET_DVR_SET_DOOR_CFG = 2109;
    int NET_DVR_GET_CARD_READER_CFG_V50 = 2505;
    int NET_DVR_SET_CARD_READER_CFG_V50 = 2506;
    int NET_DVR_GET_ACS_WORK_STATUS_V50 = 2180;
    int NET_DVR_SET_WIFI_WORKMODE = 308;
    int NET_DVR_GET_WIFI_WORKMODE = 309;
    int NET_DVR_CLEAR_ACS_PARAM = 2118;
    int NET_DVR_COMPLETE_RESTORE_CTRL = 3420;
    int NET_DVR_GET_CARD_CFG = 2116;
    int NET_DVR_VIDEO_CALL_SIGNAL_PROCESS = 16032;
    int NET_DVR_GET_ALARMHOSTSUBSYSTEM_CFG = 2001;
    int NET_DVR_SET_ALARMHOSTSUBSYSTEM_CFG = 2002;
    int NET_DVR_GET_ALARMHOST_OTHER_STATUS_V50 = 2228;
    int NET_DVR_GET_ALARMHOST_OTHER_STATUS_V51 = 2236;
    int NET_DVR_GET_MULTI_STREAM_COMPRESSIONCFG = 3216;
    int NET_DVR_SET_MULTI_STREAM_COMPRESSIONCFG = 3217;
    int NET_DVR_START_GET_INPUTVOLUME = 3370;
    int NET_DVR_GET_LOITERING_DETECTION = 3521;
    int NET_DVR_SET_LOITERING_DETECTION = 3522;
    int NET_DVR_GET_LED_AREA_INFO_LIST = 9295;
    int NET_DVR_MATRIX_GETWINSTATUS = 9009;
    int NET_SDK_GET_INPUTSTREAMCFG = 1551;
    int NET_DVR_GET_EZVIZ_ACCESS_CFG = 3398;
    int NET_DVR_SET_EZVIZ_ACCESS_CFG = 3399;
    int NET_SDK_GET_VIDEOWALLDISPLAYNO = 1553;
    int NET_SDK_GET_ALLSUBSYSTEM_BASIC_INFO = 1554;
    int NET_SDK_SET_ALLSUBSYSTEM_BASIC_INFO = 1555;
    int NET_SDK_GET_AUDIO_INFO = 1556;
    int NET_DVR_GET_VIDEOWALLDISPLAYNO = 1732;
    int NET_DVR_SET_VIDEOWALLDISPLAYPOSITION = 1733;
    int NET_DVR_GET_VIDEOWALLDISPLAYPOSITION = 1734;
    int NET_DVR_GET_CURTRIGGERMODE = 3130;
    int NET_ITC_GET_RS485_ACCESSINFO = 3117;
    int NET_ITC_SET_RS485_ACCESSINFO = 3118;
    int NET_DVR_GET_SHOWSTRING_V30 = 1030;
    int NET_DVR_SET_SHOWSTRING_V30 = 1031;
    int NET_ITS_GET_OVERLAP_CFG = 5072;
    int NET_ITS_SET_OVERLAP_CFG = 5073;
    int NET_ITC_GET_TRIGGERCFG = 3003;
    int NET_ITC_SET_TRIGGERCFG = 3004;
    int NET_DVR_GET_CCDPARAMCFG_EX = 3368;
    int NET_DVR_SET_CCDPARAMCFG_EX = 3369;
    int NET_DVR_FFC_MANUAL_CTRL = 3411;
    int NET_DVR_GET_THERMOMETRY_BASICPARAM_CAPABILITIES = 3620;
    int NET_DVR_GET_THERMOMETRY_BASICPARAM = 3621;
    int NET_DVR_SET_THERMOMETRY_BASICPARAM = 3622;
    int NET_DVR_GET_THERMOMETRY_PRESETINFO = 3624;
    int NET_DVR_SET_THERMOMETRY_PRESETINFO = 3625;
    int NET_DVR_GET_FACECONTRAST_TRIGGER = 3965;
    int NET_DVR_SET_FACECONTRAST_TRIGGER = 3966;
    int NET_DVR_GET_FACECONTRAST_SCHEDULE = 3968;
    int NET_DVR_SET_FACECONTRAST_SCHEDULE = 3969;
    int NET_DVR_GET_PICCFG_V40 = 6179;
    int NET_DVR_SET_PICCFG_V40 = 6180;
    int NET_DVR_GET_THERMAL_PIP = 6768;
    int NET_DVR_SET_THERMAL_PIP = 6769;
    int NET_DVR_GET_CARD_CFG_V50 = 2178;
    int NET_DVR_SET_CARD_CFG_V50 = 2179;
    int NET_DVR_SET_FACE_PARAM_CFG = 2508;
    int NET_DVR_GET_FACE_PARAM_CFG = 2507;
    int NET_DVR_GET_ACS_EVENT = 2514;
    int NET_DVR_CAPTURE_FACE_INFO = 2510;
    int NET_DVR_DEL_FACE_PARAM_CFG = 2509;
    int NET_DVR_GET_NETCFG_V50 = 1015;
    int NET_DVR_SET_NETCFG_V50 = 1016;
    int NET_DVR_GET_EVENT_CARD_LINKAGE_CFG_V51 = 2518;
    int NET_DVR_SET_EVENT_CARD_LINKAGE_CFG_V51 = 2519;
    int COMM_ALARM_RULE = 4354;
    int COMM_ALARM_PDC = 4355;
    int COMM_UPLOAD_FACESNAP_RESULT = 4370;
    int COMM_UPLOAD_PLATE_RESULT = 10240;
    int COMM_SNAP_MATCH_ALARM = 10498;
    int COMM_ITS_PLATE_RESULT = 12368;
    int COMM_VEHICLE_CONTROL_ALARM = 12377;
    int COMM_ALARM_V30 = 16384;
    int COMM_ALARM_V40 = 16391;
    int COMM_ALARM_FACE_DETECTION = 16400;
    int COMM_ALARM_TFS = 4371;
    int COMM_ID_INFO_ALARM = 20992;
    int COMM_ALARM_ACS = 20482;
    int COMM_PASSNUM_INFO_ALARM = 20993;
    int NET_DVR_DEV_ADDRESS_MAX_LEN = 129;
    int NET_DVR_LOGIN_USERNAME_MAX_LEN = 64;
    int NET_DVR_LOGIN_PASSWD_MAX_LEN = 64;
    int NET_SDK_CALLBACK_TYPE_STATUS = 0;
    int NET_SDK_CALLBACK_TYPE_PROGRESS = 1;
    int NET_SDK_CALLBACK_TYPE_DATA = 2;
    int NET_SDK_CALLBACK_STATUS_SUCCESS = 1000;
    int NET_SDK_CALLBACK_STATUS_PROCESSING = 1001;
    int NET_SDK_CALLBACK_STATUS_FAILED = 1002;
    int NET_SDK_CALLBACK_STATUS_EXCEPTION = 1003;
    int NET_SDK_CALLBACK_STATUS_LANGUAGE_MISMATCH = 1004;
    int NET_SDK_CALLBACK_STATUS_DEV_TYPE_MISMATCH = 1005;
    int NET_DVR_CALLBACK_STATUS_SEND_WAIT = 1006;
    int ACS_CARD_NO_LEN = 32;
    int CARD_PASSWORD_LEN = 8;
    int MAX_DOOR_NUM = 32;
    int MAX_CARD_RIGHT_PLAN_NUM = 4;
    int STREAM_ID_LEN = 32;
    int SERIALNO_LEN = 48;
    int NAME_LEN = 32;
    int MACADDR_LEN = 6;
    int MAX_DISKNUM_V30 = 33;
    int MAX_DISKNUM = 16;
    int MAX_LICENSE_LEN = 16;
    int MAX_HUMAN_BIRTHDATE_LEN = 10;
    int MAX_CHANNUM = 16;
    int MAX_ALARMIN = 16;
    int MAX_ALARMOUT = 4;
    int MAX_ANALOG_CHANNUM = 32;
    int MAX_ANALOG_ALARMOUT = 32;
    int MAX_ANALOG_ALARMIN = 32;
    int MAX_IP_DEVICE = 32;
    int MAX_IP_CHANNEL = 32;
    int MAX_IP_ALARMIN = 128;
    int MAX_IP_ALARMOUT = 64;
    int ALARMHOST_DETECTOR_SERIAL_LEN_V50 = 16;
    int MAX_CHANNUM_V30 = 64;
    int MAX_ALARMOUT_V30 = 96;
    int MAX_ALARMIN_V30 = 160;
    int VCA_MAX_POLYGON_POINT_NUM = 10;
    int MAX_REGION_NUM = 8;
    int MAX_NUM_OUTPUT_CHANNEL = 512;
    int MAX_DISPLAY_NUM = 512;
    int ALARMHOST_MAX_SIREN_NUM = 8;
    int MAX_DETECTOR_NUM = 128;
    int MAX_DETECTOR_NUM_V51 = 256;
    int MAX_REPEATER_NUM = 16;
    int MAX_OUTPUT_MODULE_NUM = 64;
    int ENUM_VCA_EVENT_TRAVERSE_PLANE = 1;
    int ENUM_VCA_EVENT_ENTER_AREA = 2;
    int ENUM_VCA_EVENT_EXIT_AREA = 3;
    int ENUM_VCA_EVENT_INTRUSION = 4;
    int ENUM_VCA_EVENT_LOITER = 5;
    int ENUM_VCA_EVENT_LEFT_TAKE = 6;
    int ENUM_VCA_EVENT_PARKING = 7;
    int ENUM_VCA_EVENT_RUN = 8;
    int ENUM_VCA_EVENT_HIGH_DENSITY = 9;
    int ENUM_VCA_EVENT_VIOLENT_MOTION = 10;
    int ENUM_VCA_EVENT_REACH_HIGHT = 11;
    int ENUM_VCA_EVENT_GET_UP = 12;
    int ENUM_VCA_EVENT_LEFT = 13;
    int ENUM_VCA_EVENT_TAKE = 14;
    int ENUM_VCA_EVENT_LEAVE_POSITION = 15;
    int ENUM_VCA_EVENT_TRAIL = 16;
    int ENUM_VCA_EVENT_KEY_PERSON_GET_UP = 17;
    int ENUM_VCA_EVENT_STANDUP = 18;
    int ENUM_VCA_EVENT_FALL_DOWN = 20;
    int ENUM_VCA_EVENT_AUDIO_ABNORMAL = 21;
    int ENUM_VCA_EVENT_ADV_REACH_HEIGHT = 22;
    int ENUM_VCA_EVENT_TOILET_TARRY = 23;
    int ENUM_VCA_EVENT_YARD_TARRY = 24;
    int ENUM_VCA_EVENT_ADV_TRAVERSE_PLANE = 25;
    int ENUM_VCA_EVENT_HUMAN_ENTER = 29;
    int ENUM_VCA_EVENT_OVER_TIME = 30;
    int ENUM_VCA_EVENT_STICK_UP = 31;
    int ENUM_VCA_EVENT_INSTALL_SCANNER = 32;
    int ENUM_VCA_EVENT_PEOPLENUM_CHANGE = 35;
    int ENUM_VCA_EVENT_SPACING_CHANGE = 36;
    int ENUM_VCA_EVENT_COMBINED_RULE = 37;
    int ENUM_VCA_EVENT_SIT_QUIETLY = 38;
    int ENUM_VCA_EVENT_HIGH_DENSITY_STATUS = 39;
    int MAX_SUBSYSTEM_ID_LEN = 16;
    int ACCOUNTNUM_LEN = 6;
    int ACCOUNTNUM_LEN_32 = 32;
    int EZVIZ_DEVICEID_LEN = 32;
    int EZVIZ_REQURL_LEN = 64;
    int EZVIZ_ACCESSTOKEN_LEN = 128;
    int EZVIZ_CLIENTTYPE_LEN = 32;
    int EZVIZ_FEATURECODE_LEN = 64;
    int EZVIZ_OSVERSION_LEN = 32;
    int EZVIZ_NETTYPE_LEN = 32;
    int EZVIZ_SDKVERSION_LEN = 32;
    int EZVIZ_APPID_LEN = 64;
    int MAX_DOMAIN_NAME = 64;
    int PASSWD_LEN = 16;
    int MAX_CARDNO_LEN = 48;
    int NET_SDK_MAX_VERIFICATION_CODE_LEN = 32;
    int NET_SDK_MAX_FDID_LEN = 256;
    int MAX_UPLOADFILE_URL_LEN = 260;
    int NET_SDK_MAX_PICID_LEN = 256;
    int ENUM_DVR_VEHICLE_CHECK = 1;
    int ENUM_MSC_SEND_DATA = 2;
    int ENUM_ACS_SEND_DATA = 3;
    int ENUM_TME_CARD_SEND_DATA = 4;
    int ENUM_TME_VEHICLE_SEND_DATA = 5;
    int ENUM_DVR_DEBUG_CMD = 6;
    int ENUM_DVR_SCREEN_CTRL_CMD = 7;
    int ENUM_CVR_PASSBACK_SEND_DATA = 8;
    int ISAPI_DATA_LEN = 10485760;
    int ISAPI_STATUS_LEN = 16384;
    int BYTE_ARRAY_LEN = 1024;
    int MAX_MAX_ALARMIN_NUM = 64;
    int MAX_DAYS = 7;
    int MAX_TIMESEGMENT_V30 = 8;
    int MAX_TIMESEGMENT = 4;
    int HOLIDAY_GROUP_NAME_LEN = 32;
    int MAX_HOLIDAY_PLAN_NUM = 16;
    int TEMPLATE_NAME_LEN = 32;
    int MAX_HOLIDAY_GROUP_NUM = 16;
    int DEV_TYPE_NAME_LEN = 64;
    int DOOR_NAME_LEN = 32;
    int STRESS_PASSWORD_LEN = 8;
    int SUPER_PASSWORD_LEN = 8;
    int UNLOCK_PASSWORD_LEN = 8;
    int CARD_READER_DESCRIPTION = 32;
    int MAX_DOOR_NUM_256 = 256;
    int MAX_CASE_SENSOR_NUM = 8;
    int MAX_CARD_READER_NUM = 64;
    int MAX_CARD_READER_NUM_512 = 512;
    int MAX_ALARMHOST_ALARMOUT_NUM = 512;
    int MAX_ALARMHOST_ALARMIN_NUM = 512;
    int ALARMHOST_DETECTOR_SERIAL_LEN = 9;
    int PICTURE_NAME_LEN = 64;
    int MAX_FACE_PIC_NUM = 30;
    int CARDNUM_LEN_V30 = 40;
    int MAX_ITC_LANE_NUM = 6;
    int MAX_CHJC_NUM = 3;
    int MAX_IOOUT_NUM = 4;
    int MAX_LANEAREA_NUM = 2;
    int MAX_INTERVAL_NUM = 4;
    int ITC_MAX_POLYGON_POINT_NUM = 20;
    int MAX_OVERLAP_ITEM_NUM = 50;
    int MAX_STRINGNUM_V30 = 8;
    int NET_DVR_FILE_SUCCESS = 1000;
    int NET_DVR_FILE_NOFIND = 1001;
    int NET_DVR_ISFINDING = 1002;
    int NET_DVR_NOMOREFILE = 1003;
    int NET_DVR_FILE_EXCEPTION = 1004;
    int MAX_SUBSYSTEM_NUM_V40 = 120;
    int MONITORSITE_ID_LEN = 48;
    int DEVICE_ID_LEN = 48;
    int MAX_SHELTERNUM = 4;
    int MAX_ALARMOUT_V40 = 4128;
    int MAX_CHANNUM_V40 = 512;
    int MAX_MULTI_AREA_NUM = 24;
    int MAX_THERMOMETRY_REGION_NUM = 40;
    int MAX_CATEGORY_LEN = 8;
    int MAX_LICENSE_LEN_EX = 32;
    int NET_DVR_PLAYSTART = 1;
    int NET_DVR_PLAYSTOP = 2;
    int NET_DVR_PLAYPAUSE = 3;
    int NET_DVR_PLAYRESTART = 4;
    int NET_DVR_PLAYFAST = 5;
    int NET_DVR_PLAYSLOW = 6;
    int NET_DVR_PLAYNORMAL = 7;
    int NET_DVR_PLAYSTARTAUDIO = 9;
    int NET_DVR_PLAYSTOPAUDIO = 10;
    int NET_DVR_PLAYSETPOS = 12;
    int NET_DVR_RESETBUFFER = 37;

    int NET_DVR_GetSDKVersion();

    int NET_DVR_GetLastError();

    int NET_DVR_Login_V40(Pointer var1, Pointer var2);

    boolean NET_DVR_RestoreConfig(int var1);

    boolean NET_DVR_ControlGateway(int var1, int var2, int var3);

    boolean NET_DVR_RemoteControl(int var1, int var2, Pointer var3, int var4);

    boolean NET_DVR_Logout(int var1);

    boolean NET_DVR_STDXMLConfig(int var1, HCNetSDKByJNA.NET_DVR_XML_CONFIG_INPUT var2, HCNetSDKByJNA.NET_DVR_XML_CONFIG_OUTPUT var3);

    boolean NET_DVR_GetDVRConfig(int var1, int var2, int var3, Pointer var4, int var5, IntByReference var6);

    boolean NET_DVR_SetDVRConfig(int var1, int var2, int var3, Pointer var4, int var5);

    boolean NET_DVR_GetDeviceConfig(int var1, int var2, int var3, Pointer var4, int var5, Pointer var6, Pointer var7, int var8);

    boolean NET_DVR_SetDeviceConfig(int var1, int var2, int var3, Pointer var4, int var5, Pointer var6, Pointer var7, int var8);

    boolean NET_DVR_PTZControlWithSpeed_Other(int var1, int var2, int var3, int var4, int var5);

    boolean NET_DVR_ClientSetVideoEffect(int var1, int var2, int var3, int var4, int var5);

    boolean NET_DVR_ClientGetVideoEffect(int var1, Pointer var2, Pointer var3, Pointer var4, Pointer var5);

    boolean NET_DVR_GetSTDConfig(int var1, int var2, Pointer var3);

    boolean NET_DVR_SetSTDConfig(int var1, int var2, Pointer var3);

    boolean NET_DVR_GetSTDAbility(int var1, int var2, Pointer var3);

    boolean NET_DVR_SetDVRMessageCallBack_V30(HCNetSDKByJNA.FMSGCallBack var1, Pointer var2);

    boolean NET_DVR_SetDVRMessageCallBack_V50(int var1, HCNetSDKByJNA.FMSGCallBack var2, Pointer var3);

    int NET_DVR_SetupAlarmChan_V30(int var1);

    int NET_DVR_SetupAlarmChan_V41(int var1, Pointer var2);

    boolean NET_DVR_CloseAlarmChan_V30(int var1);

    int NET_DVR_StartRemoteConfig(int var1, int var2, Pointer var3, int var4, HCNetSDKByJNA.fRemoteConfigCallback var5, Pointer var6);

    boolean NET_DVR_SendRemoteConfig(int var1, int var2, Pointer var3, int var4);

    boolean NET_DVR_StopRemoteConfig(int var1);

    int NET_DVR_MatrixStartPassiveDecode(int var1, int var2, Pointer var3);

    boolean NET_DVR_MatrixSendData(int var1, Pointer var2, int var3);

    boolean NET_DVR_MatrixStopPassiveDecode(int var1);

    int NET_DVR_MatrixGetPassiveDecodeStatus(int var1);

    boolean NET_DVR_MatrixGetDecChanCfg(int var1, int var2, Pointer var3);

    boolean NET_DVR_MatrixSetDecChanCfg(int var1, int var2, Pointer var3);

    int NET_DVR_UploadFile_V40(int var1, int var2, Pointer var3, int var4, String var5, Pointer var6, int var7);

    int NET_DVR_UploadSend(int var1, Pointer var2, Pointer var3);

    int NET_DVR_GetUploadState(int var1, IntByReference var2);

    boolean NET_DVR_GetUploadResult(int var1, Pointer var2, int var3);

    boolean NET_DVR_UploadClose(int var1);

    boolean NET_DVR_SetAlarmHostOut(int var1, int var2, int var3);

    int NET_DVR_CreateOpenEzvizUser(Pointer var1, Pointer var2);

    boolean NET_DVR_DeleteOpenEzvizUser(int var1);

    boolean NET_DVR_GetInputSignalList_V40(int var1, int var2, HCNetSDKByJNA.NET_DVR_INPUT_SIGNAL_LIST var3);

    boolean NET_DVR_OpenSound(int var1);

    boolean NET_DVR_CloseSound();

    boolean NET_DVR_Volume(int var1, short var2);

    int NET_DVR_FindPicture(int var1, Pointer var2);

    int NET_DVR_FindNextPicture_V40(int var1, Pointer var2);

    boolean NET_DVR_CloseFindPicture(int var1);

    boolean NET_DVR_GetPicture_V30(int var1, Pointer var2, Pointer var3, int var4, IntByReference var5);

    int NET_DVR_GetRealPlayerIndex(int var1);

    int NET_DVR_GetPlayBackPlayerIndex(int var1);

    boolean NET_DVR_SetCapturePictureMode(int var1);

    boolean NET_DVR_CapturePictureBlock(int var1, String var2, int var3);

    boolean NET_DVR_CapturePicture(int var1, String var2);

    boolean NET_DVR_PlayBackCaptureFile(int var1, String var2);

    boolean NET_DVR_SaveRealData_V30(int var1, int var2, String var3);

    boolean NET_DVR_GetDeviceStatus(int var1, int var2, int var3, Pointer var4, int var5, Pointer var6, Pointer var7, int var8);

    int NET_DVR_StartDownload(int var1, int var2, Pointer var3, int var4, String var5);

    int NET_DVR_GetDownloadState(int var1, IntByReference var2);

    boolean NET_DVR_StopDownload(int var1);

    boolean NET_DVR_SetSDKLocalCfg(int var1, Pointer var2);
    boolean NET_DVR_GetSDKLocalCfg(int var1, Pointer var2);

    //修改
    boolean NET_DVR_SetSDKLocalCfg(int type,NET_DVR_LOCAL_GENERAL_CFG  cfg);

    boolean NET_DVR_SetLogToFile(int var1, String var2, boolean var3);

    boolean NET_DVR_MatrixGetSubSystemInfo_V40(int var1, HCNetSDKByJNA.NET_DVR_ALLSUBSYSTEMINFO_V40 var2);

    boolean NET_DVR_ManualSnap(int var1, Pointer var2, Pointer var3);

    boolean NET_DVR_PlayBackControl_V40(int var1, int var2, Pointer var3, int var4, Pointer var5, Pointer var6);

    boolean NET_DVR_GetPicture_V50(int var1, Pointer var2);

    int NET_DVR_StartListen_V30(Pointer var1, short var2, HCNetSDKByJNA.FMSGCallBack var3, Pointer var4);

    boolean NET_DVR_StopListen_V30(int var1);

    public static class BYTE_ARRAY extends Structure {
        public byte[] byValue;

        public BYTE_ARRAY(int iLen) {
            this.byValue = new byte[iLen];
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byValue");
        }
    }

    public interface CHAR_ENCODE_CONVERT extends Callback {
        void invoke(Pointer var1, int var2, int var3, String var4, int var5, int var6);
    }

    public interface FLoginResultCallBack extends Callback {
        int invoke(int var1, int var2, HCNetSDKByJNA.NET_DVR_DEVICEINFO_V30 var3, Pointer var4);
    }

    public interface FMSGCallBack extends Callback {
        void invoke(int var1, HCNetSDKByJNA.NET_DVR_ALARMER var2, Pointer var3, int var4, Pointer var5);
    }

    public static class INT_ARRAY extends Structure {
        public int[] iValue;

        public INT_ARRAY(int iLen) {
            this.iValue = new int[iLen];
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("iValue");
        }
    }

    public static class NET_DVR_ACS_ALARM_INFO extends Structure {
        public int dwSize;
        public int dwMajor;
        public int dwMinor;
        public HCNetSDKByJNA.NET_DVR_TIME struTime = new HCNetSDKByJNA.NET_DVR_TIME();
        public byte[] sNetUser = new byte[16];
        public HCNetSDKByJNA.NET_DVR_IPADDR struRemoteHostAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public HCNetSDKByJNA.NET_DVR_ACS_EVENT_INFO struAcsEventInfo = new HCNetSDKByJNA.NET_DVR_ACS_EVENT_INFO();
        public int dwPicDataLen;
        public ByteByReference pPicData;
        public short wInductiveEventType;
        public byte byPicTransType;
        public byte byRes1;
        public int dwIOTChannelNo;
        public ByteByReference pAcsEventInfoExtend;
        public byte byAcsEventInfoExtend;
        public byte byTimeType;
        public byte[] byRes = new byte[10];

        public NET_DVR_ACS_ALARM_INFO(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwMajor", "dwMinor", "struTime", "sNetUser", "struRemoteHostAddr", "struAcsEventInfo", "dwPicDataLen", "pPicData", "wInductiveEventType", "byPicTransType", "byRes1", "dwIOTChannelNo", "pAcsEventInfoExtend", "byAcsEventInfoExtend", "byTimeType", "byRes");
        }
    }

    public static class NET_DVR_ACS_EVENT_CFG extends Structure {
        public int dwSize;
        public int dwMajor;
        public int dwMinor;
        public HCNetSDKByJNA.NET_DVR_TIME struTime;
        public byte[] sNetUser = new byte[16];
        public HCNetSDKByJNA.NET_DVR_IPADDR struRemoteHostAddr;
        public HCNetSDKByJNA.NET_DVR_ACS_EVENT_DETAIL struAcsEventInfo;
        public int dwPicDataLen;
        public String pPicData;
        public byte[] byRes = new byte[64];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwMajor", "dwMinor", "struTime", "sNetUser", "struRemoteHostAddr", "struAcsEventInfo", "dwPicDataLen", "pPicData", "byRes");
        }

        public NET_DVR_ACS_EVENT_CFG(Pointer p) {
            super(p);
        }
    }

    public static class NET_DVR_ACS_EVENT_COND extends Structure {
        public int dwSize;
        public int dwMajor;
        public int dwMinor;
        public HCNetSDKByJNA.NET_DVR_TIME struStartTime;
        public HCNetSDKByJNA.NET_DVR_TIME struEndTime;
        public byte[] byCardNo = new byte[32];
        public byte[] byName = new byte[32];
        public byte byPicEnable;
        public byte[] byRes2 = new byte[3];
        public int dwBeginSerialNo;
        public int dwEndSerialNo;
        public byte[] byRes = new byte[244];

        public NET_DVR_ACS_EVENT_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwMajor", "dwMinor", "struStartTime", "struEndTime", "byCardNo", "byName", "byPicEnable", "byRes2", "dwBeginSerialNo", "dwEndSerialNo", "byRes");
        }
    }

    public static class NET_DVR_ACS_EVENT_DETAIL extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public byte byCardType;
        public byte byWhiteListNo;
        public byte byReportChannel;
        public byte byCardReaderKind;
        public int dwCardReaderNo;
        public int dwDoorNo;
        public int dwVerifyNo;
        public int dwAlarmInNo;
        public int dwAlarmOutNo;
        public int dwCaseSensorNo;
        public int dwRs485No;
        public int dwMultiCardGroupNo;
        public short wAccessChannel;
        public byte byDeviceNo;
        public byte byDistractControlNo;
        public int dwEmployeeNo;
        public short wLocalControllerID;
        public byte byInternetAccess;
        public byte byType;
        public byte[] byMACAddr = new byte[6];
        public byte bySwipeCardType;
        public byte byRes2;
        public int dwSerialNo;
        public byte byChannelControllerID;
        public byte byChannelControllerLampID;
        public byte byChannelControllerIRAdaptorID;
        public byte byChannelControllerIREmitterID;
        public byte[] byRes = new byte[108];

        public NET_DVR_ACS_EVENT_DETAIL() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "byCardType", "byWhiteListNo", "byReportChannel", "byCardReaderKind", "dwCardReaderNo", "dwDoorNo", "dwVerifyNo", "dwAlarmInNo", "dwAlarmOutNo", "dwCaseSensorNo", "dwRs485No", "dwMultiCardGroupNo", "wAccessChannel", "byDeviceNo", "byDistractControlNo", "dwEmployeeNo", "wLocalControllerID", "byInternetAccess", "byType", "byMACAddr", "bySwipeCardType", "byRes2", "dwSerialNo", "byChannelControllerID", "byChannelControllerLampID", "byChannelControllerIRAdaptorID", "byChannelControllerIREmitterID", "byRes");
        }
    }

    public static class NET_DVR_ACS_EVENT_INFO extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public byte byCardType;
        public byte byWhiteListNo;
        public byte byReportChannel;
        public byte byCardReaderKind;
        public int dwCardReaderNo;
        public int dwDoorNo;
        public int dwVerifyNo;
        public int dwAlarmInNo;
        public int dwAlarmOutNo;
        public int dwCaseSensorNo;
        public int dwRs485No;
        public int dwMultiCardGroupNo;
        public short wAccessChannel;
        public byte byDeviceNo;
        public byte byDistractControlNo;
        public int dwEmployeeNo;
        public short wLocalControllerID;
        public byte byInternetAccess;
        public byte byType;
        public byte[] byMACAddr = new byte[6];
        public byte bySwipeCardType;
        public byte byRes2;
        public int dwSerialNo;
        public byte byChannelControllerID;
        public byte byChannelControllerLampID;
        public byte byChannelControllerIRAdaptorID;
        public byte byChannelControllerIREmitterID;
        public byte[] byRes = new byte[4];

        public NET_DVR_ACS_EVENT_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "byCardType", "byWhiteListNo", "byReportChannel", "byCardReaderKind", "dwCardReaderNo", "dwDoorNo", "dwVerifyNo", "dwAlarmInNo", "dwAlarmOutNo", "dwCaseSensorNo", "dwRs485No", "dwMultiCardGroupNo", "wAccessChannel", "byDeviceNo", "byDistractControlNo", "dwEmployeeNo", "wLocalControllerID", "byInternetAccess", "byType", "byMACAddr", "bySwipeCardType", "byRes2", "dwSerialNo", "byChannelControllerID", "byChannelControllerLampID", "byChannelControllerIRAdaptorID", "byChannelControllerIREmitterID", "byRes");
        }
    }

    public static class NET_DVR_ACS_PARAM_TYPE extends Structure {
        public int dwSize;
        public int dwParamType;
        public byte[] byRes = new byte[32];

        public NET_DVR_ACS_PARAM_TYPE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwParamType", "byRes");
        }
    }

    public static class NET_DVR_ACS_WORK_STATUS extends Structure {
        public int dwSize;
        public byte[] byDoorLockStatus = new byte[32];
        public byte[] byDoorStatus = new byte[32];
        public byte[] byMagneticStatus = new byte[32];
        public byte[] byCaseStatus = new byte[8];
        public short wBatteryVoltage;
        public byte byBatteryLowVoltage;
        public byte byPowerSupplyStatus;
        public byte byMultiDoorInterlockStatus;
        public byte byAntiSneakStatus;
        public byte byHostAntiDismantleStatus;
        public byte byIndicatorLightStatus;
        public byte[] byCardReaderOnlineStatus = new byte[64];
        public byte[] byCardReaderAntiDismantleStatus = new byte[64];
        public byte[] byCardReaderVerifyMode = new byte[64];
        public byte[] bySetupAlarmStatus = new byte[512];
        public byte[] byAlarmInStatus = new byte[512];
        public byte[] byAlarmOutStatus = new byte[512];
        public int dwCardNum;
        public byte[] byRes2 = new byte[32];

        public NET_DVR_ACS_WORK_STATUS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byDoorLockStatus", "byDoorStatus", "byMagneticStatus", "byCaseStatus", "wBatteryVoltage", "byBatteryLowVoltage", "byPowerSupplyStatus", "byMultiDoorInterlockStatus", "byAntiSneakStatus", "byHostAntiDismantleStatus", "byIndicatorLightStatus", "byCardReaderOnlineStatus", "byCardReaderAntiDismantleStatus", "byCardReaderVerifyMode", "bySetupAlarmStatus", "byAlarmInStatus", "byAlarmOutStatus", "dwCardNum", "byRes2");
        }
    }

    public static class NET_DVR_ACS_WORK_STATUS_V50 extends Structure {
        public int dwSize;
        public byte[] byDoorLockStatus = new byte[256];
        public byte[] byDoorStatus = new byte[256];
        public byte[] byMagneticStatus = new byte[256];
        public byte[] byCaseStatus = new byte[8];
        public short wBatteryVoltage;
        public byte byBatteryLowVoltage;
        public byte byPowerSupplyStatus;
        public byte byMultiDoorInterlockStatus;
        public byte byAntiSneakStatus;
        public byte byHostAntiDismantleStatus;
        public byte byIndicatorLightStatus;
        public byte[] byCardReaderOnlineStatus = new byte[512];
        public byte[] byCardReaderAntiDismantleStatus = new byte[512];
        public byte[] byCardReaderVerifyMode = new byte[512];
        public byte[] bySetupAlarmStatus = new byte[512];
        public byte[] byAlarmInStatus = new byte[512];
        public byte[] byAlarmOutStatus = new byte[512];
        public int dwCardNum;
        public byte byFireAlarmStatus;
        public byte[] byRes2 = new byte[123];

        public NET_DVR_ACS_WORK_STATUS_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byDoorLockStatus", "byDoorStatus", "byMagneticStatus", "byCaseStatus", "wBatteryVoltage", "byBatteryLowVoltage", "byPowerSupplyStatus", "byMultiDoorInterlockStatus", "byAntiSneakStatus", "byHostAntiDismantleStatus", "byIndicatorLightStatus", "byCardReaderOnlineStatus", "byCardReaderAntiDismantleStatus", "byCardReaderVerifyMode", "bySetupAlarmStatus", "byAlarmInStatus", "byAlarmOutStatus", "dwCardNum", "byFireAlarmStatus", "byRes2");
        }
    }

    public static class NET_DVR_ADDRESS extends Structure {
        public HCNetSDKByJNA.NET_DVR_IPADDR struIP = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public short wPort;
        public byte[] byRes = new byte[2];

        public NET_DVR_ADDRESS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struIP", "wPort", "byRes");
        }
    }

    public static class NET_DVR_AGC_PARAM extends Structure {
        public byte bySceneType;
        public byte byLightLevel;
        public byte byGainLevel;
        public byte[] byRes = new byte[5];

        public NET_DVR_AGC_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("bySceneType", "byLightLevel", "byGainLevel", "byRes");
        }
    }

    public static class NET_DVR_AID_INFO extends Structure {
        public byte byRuleID;
        public byte[] byRes1 = new byte[3];
        public byte[] byRuleName = new byte[32];
        public int dwAIDType;
        public HCNetSDKByJNA.NET_DVR_DIRECTION struDirect = new HCNetSDKByJNA.NET_DVR_DIRECTION();
        public byte bySpeedLimit;
        public byte byCurrentSpeed;
        public byte byVehicleEnterState;
        public byte[] byRes2 = new byte[37];

        public NET_DVR_AID_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRuleID", "byRes1", "byRuleName", "dwAIDType", "struDirect", "bySpeedLimit", "byCurrentSpeed", "byVehicleEnterState", "byRes2");
        }
    }

    public static class NET_DVR_ALARMER extends Structure {
        public byte byUserIDValid;
        public byte bySerialValid;
        public byte byVersionValid;
        public byte byDeviceNameValid;
        public byte byMacAddrValid;
        public byte byLinkPortValid;
        public byte byDeviceIPValid;
        public byte bySocketIPValid;
        public int lUserID;
        public byte[] sSerialNumber = new byte[48];
        public int dwDeviceVersion;
        public byte[] sDeviceName = new byte[32];
        public byte[] byMacAddr = new byte[6];
        public short wLinkPort;
        public byte[] sDeviceIP = new byte[128];
        public byte[] sSocketIP = new byte[128];
        public byte byIpProtocol;
        public byte[] byRes2 = new byte[11];

        public NET_DVR_ALARMER() {
        }

        protected List getFieldOrder() {
            return Arrays.asList("byUserIDValid", "bySerialValid", "byVersionValid", "byDeviceNameValid", "byMacAddrValid", "byLinkPortValid", "byDeviceIPValid", "bySocketIPValid", "lUserID", "sSerialNumber", "dwDeviceVersion", "sDeviceName", "byMacAddr", "wLinkPort", "sDeviceIP", "sSocketIP", "byIpProtocol", "byRes2");
        }
    }

    public static class NET_DVR_ALARMHOST_OTHER_STATUS_V50 extends Structure {
        public int dwSize;
        public byte[] bySirenStatus = new byte[8];
        public byte[] byDetetorPower = new byte[128];
        public byte[] byDetetorConnection = new byte[128];
        public byte[] byRes = new byte[1024];

        public NET_DVR_ALARMHOST_OTHER_STATUS_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "bySirenStatus", "byDetetorPower", "byDetetorConnection", "byRes");
        }
    }

    public static class NET_DVR_ALARMHOST_OTHER_STATUS_V51 extends Structure {
        public int dwSize;
        public byte[] bySirenStatus = new byte[8];
        public byte[] byDetetorPower = new byte[256];
        public byte[] byDetetorConnection = new byte[256];
        public byte[] bySirenPower = new byte[8];
        public byte[] bySirenTamperStatus = new byte[8];
        public byte[] byPowerStausEnabled = new byte[32];
        public byte[] byDetetorPowerStatus = new byte[32];
        public byte byDetetorPowerType;
        public byte[] byRes2 = new byte[3];
        public byte[] byRepeaterStatus = new byte[16];
        public byte[] byRepeaterTamperStatus = new byte[2];
        public byte[] byAlarmOutTamperStatus = new byte[64];
        public byte[] byOutputModuleTamperStatus = new byte[8];
        public byte[] byRes = new byte[338];

        public NET_DVR_ALARMHOST_OTHER_STATUS_V51() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "bySirenStatus", "byDetetorPower", "byDetetorConnection", "bySirenPower", "bySirenTamperStatus", "byPowerStausEnabled", "byDetetorPowerStatus", "byDetetorPowerType", "byRes2", "byRepeaterStatus", "byRepeaterTamperStatus", "byAlarmOutTamperStatus", "byOutputModuleTamperStatus", "byRes");
        }
    }

    public static class NET_DVR_ALARMINFO extends Structure {
        public int dwAlarmType;
        public int dwAlarmInputNumber;
        public int[] dwAlarmOutputNumber = new int[4];
        public int[] dwAlarmRelateChannel = new int[16];
        public int[] dwChannel = new int[16];
        public int[] dwDiskNumber = new int[16];

        public NET_DVR_ALARMINFO() {
        }

        protected List getFieldOrder() {
            return Arrays.asList("dwAlarmType", "dwAlarmInputNumber", "dwAlarmOutputNumber", "dwAlarmRelateChannel", "dwChannel", "dwDiskNumber");
        }
    }

    public static class NET_DVR_ALARMINFO_V30 extends Structure {
        public int dwAlarmType;
        public int dwAlarmInputNumber;
        public byte[] byAlarmOutputNumber = new byte[96];
        public byte[] byAlarmRelateChannel = new byte[64];
        public byte[] byChannel = new byte[64];
        public byte[] byDiskNumber = new byte[33];

        public NET_DVR_ALARMINFO_V30(Pointer p) {
            super(p);
        }

        protected List getFieldOrder() {
            return Arrays.asList("dwAlarmType", "dwAlarmInputNumber", "byAlarmOutputNumber", "byAlarmRelateChannel", "byChannel", "byDiskNumber");
        }
    }

    public static class NET_DVR_ALARMINFO_V40 extends Structure {
        public HCNetSDKByJNA.NET_DVR_ALRAM_FIXED_HEADER struAlarmFixedHeader = new HCNetSDKByJNA.NET_DVR_ALRAM_FIXED_HEADER();
        public Pointer pAlarmData;

        public NET_DVR_ALARMINFO_V40(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struAlarmFixedHeader", "pAlarmData");
        }
    }

    public static class NET_DVR_ALARMIN_PARAM_LIST extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_SINGLE_ALARMIN_PARAM[] struSingleAlarmInParam = (HCNetSDKByJNA.NET_DVR_SINGLE_ALARMIN_PARAM[])(new HCNetSDKByJNA.NET_DVR_SINGLE_ALARMIN_PARAM()).toArray(64);
        public byte[] byRes = new byte[128];

        public NET_DVR_ALARMIN_PARAM_LIST() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struSingleAlarmInParam", "byRes");
        }
    }

    public static class NET_DVR_ALARMIN_PARAM_LIST_V50 extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_SINGLE_ALARMIN_PARAM_V50[] struSingleAlarmInParam = (HCNetSDKByJNA.NET_DVR_SINGLE_ALARMIN_PARAM_V50[])(new HCNetSDKByJNA.NET_DVR_SINGLE_ALARMIN_PARAM_V50()).toArray(64);
        public byte[] byRes = new byte[128];

        public NET_DVR_ALARMIN_PARAM_LIST_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struSingleAlarmInParam", "byRes");
        }
    }

    public static class NET_DVR_ALARMIN_PARAM_V50 extends Structure {
        public int dwSize;
        public byte[] byName = new byte[32];
        public short wDetectorType;
        public byte byType;
        public byte byUploadAlarmRecoveryReport;
        public int dwParam;
        public HCNetSDKByJNA.NET_DVR_SCHEDTIME_DAYS[] struAlarmTime = (HCNetSDKByJNA.NET_DVR_SCHEDTIME_DAYS[])(new HCNetSDKByJNA.NET_DVR_SCHEDTIME_DAYS()).toArray(4);
        public byte[] byAssociateAlarmOut = new byte[512];
        public byte[] byAssociateSirenOut = new byte[8];
        public byte bySensitivityParam;
        public byte byArrayBypass;
        public byte byJointSubSystem;
        public byte byModuleStatus;
        public short wModuleAddress;
        public byte byModuleChan;
        public byte byModuleType;
        public short wZoneIndex;
        public short wInDelay;
        public short wOutDelay;
        public byte byAlarmType;
        public byte byZoneResistor;
        public float fZoneResistorManual;
        public byte[] byDetectorSerialNo = new byte[16];
        public byte byZoneSignalType;
        public byte byDisableDetectorTypeCfg;
        public short wTimeOut;
        public byte[] byAssociateLampOut = new byte[8];
        public byte[] byVoiceFileName = new byte[32];
        public byte byTimeOutRange;
        public byte byDetectorSignalIntensity;
        public byte byTimeOutMethod;
        public byte byAssociateFlashLamp;
        public byte byStayAwayEnabled;
        public byte bySilentModeEnabled;
        public byte[] byRes3 = new byte[506];

        public NET_DVR_ALARMIN_PARAM_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byName", "wDetectorType", "byType", "byUploadAlarmRecoveryReport", "dwParam", "struAlarmTime", "byAssociateAlarmOut", "byAssociateSirenOut", "bySensitivityParam", "byArrayBypass", "byJointSubSystem", "byModuleStatus", "wModuleAddress", "byModuleChan", "byModuleType", "wZoneIndex", "wInDelay", "wOutDelay", "byAlarmType", "byZoneResistor", "fZoneResistorManual", "byDetectorSerialNo", "byZoneSignalType", "byDisableDetectorTypeCfg", "wTimeOut", "byAssociateLampOut", "byVoiceFileName", "byTimeOutRange", "byDetectorSignalIntensity", "byTimeOutMethod", "byAssociateFlashLamp", "byStayAwayEnabled", "bySilentModeEnabled", "byRes3");
        }
    }

    public static class NET_DVR_ALARMSUBSYSTEMPARAM extends Structure {
        public int dwSize;
        public short wEnterDelay;
        public short wExitDelay;
        public byte byHostageReport;
        public byte bySubsystemEnable;
        public byte byKeyToneOfArmOrDisarm;
        public byte byKeyToneOfManualTestReport;
        public short wDelayTime;
        public byte byEnableAlarmInDelay;
        public byte byPublicAttributeEnable;
        public HCNetSDKByJNA.NET_DVR_JOINT_SUB_SYSTEM struJointSubSystem = new HCNetSDKByJNA.NET_DVR_JOINT_SUB_SYSTEM();
        public byte byKeyZoneArm;
        public byte byKeyZoneArmReport;
        public byte byKeyZoneDisarm;
        public byte byKeyZoneDisarmReport;
        public byte[] bySubSystemID = new byte[16];
        public byte byKeyZoneArmReportEnable;
        public byte byKeyZoneArmEnable;
        public byte byOneKeySetupAlarmEnable;
        public byte bySingleZoneSetupAlarmEnable;
        public byte byCenterType;
        public byte[] sCenterAccount = new byte[6];
        public byte[] sCenterAccountV40 = new byte[32];
        public byte[] byRes2 = new byte[565];

        public NET_DVR_ALARMSUBSYSTEMPARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "wEnterDelay", "wExitDelay", "byHostageReport", "bySubsystemEnable", "byKeyToneOfArmOrDisarm", "byKeyToneOfManualTestReport", "wDelayTime", "byEnableAlarmInDelay", "byPublicAttributeEnable", "struJointSubSystem", "byKeyZoneArm", "byKeyZoneArmReport", "byKeyZoneDisarm", "byKeyZoneDisarmReport", "bySubSystemID", "byKeyZoneArmReportEnable", "byKeyZoneArmEnable", "byOneKeySetupAlarmEnable", "bySingleZoneSetupAlarmEnable", "byCenterType", "sCenterAccount", "sCenterAccountV40", "byRes2");
        }
    }

    public static class NET_DVR_ALLSUBSYSTEMINFO_V40 extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_SUBSYSTEMINFO_V40[] struSubSystemInfo = new HCNetSDKByJNA.NET_DVR_SUBSYSTEMINFO_V40[120];
        public byte[] byRes = new byte[8];

        public NET_DVR_ALLSUBSYSTEMINFO_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struSubSystemInfo", "byRes");
        }
    }

    public static class NET_DVR_ALRAM_FIXED_HEADER extends Structure {
        public int dwAlarmType;
        public HCNetSDKByJNA.NET_DVR_TIME_EX struAlarmTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public HCNetSDKByJNA.uStruAlarm ustruAlarm = new HCNetSDKByJNA.uStruAlarm();

        public NET_DVR_ALRAM_FIXED_HEADER() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwAlarmType", "struAlarmTime", "ustruAlarm");
        }
    }

    public static class NET_DVR_AREAINFOCFG extends Structure {
        public short wNationalityID;
        public short wProvinceID;
        public short wCityID;
        public short wCountyID;
        public int dwCode;

        public NET_DVR_AREAINFOCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wNationalityID", "wProvinceID", "wCityID", "wCountyID", "dwCode");
        }
    }

    public static class NET_DVR_AUDIO_INFO extends Structure {
        public int dwSize;
        public byte byAudioChanType;
        public byte[] byRes1 = new byte[3];
        public int dwAudioNo;
        public byte[] byRes2 = new byte[16];

        public NET_DVR_AUDIO_INFO(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byAudioChanType", "byRes1", "dwAudioNo", "byRes2");
        }
    }

    public static class NET_DVR_BACKLIGHT extends Structure {
        public byte byBacklightMode;
        public byte byBacklightLevel;
        public byte[] byRes1 = new byte[2];
        public int dwPositionX1;
        public int dwPositionY1;
        public int dwPositionX2;
        public int dwPositionY2;
        public byte[] byRes2 = new byte[4];

        public NET_DVR_BACKLIGHT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byBacklightMode", "byBacklightLevel", "byRes1", "dwPositionX1", "dwPositionY1", "dwPositionX2", "dwPositionY2", "byRes2");
        }
    }

    public static class NET_DVR_CALL_ROOM_CFG extends Structure {
        public int dwSize;
        public short nFloorNumber;
        public short wRoomNumber;
        public byte byManageCenter;
        public byte[] byRes = new byte[127];

        public NET_DVR_CALL_ROOM_CFG() {
        }

        protected List getFieldOrder() {
            return Arrays.asList("dwSize", "nFloorNumber", "wRoomNumber", "byManageCenter", "byRes");
        }
    }

    public static class NET_DVR_CAMERAPARAMCFG_EX extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_VIDEOEFFECT struVideoEffect = new HCNetSDKByJNA.NET_DVR_VIDEOEFFECT();
        public HCNetSDKByJNA.NET_DVR_GAIN struGain = new HCNetSDKByJNA.NET_DVR_GAIN();
        public HCNetSDKByJNA.NET_DVR_WHITEBALANCE struWhiteBalance = new HCNetSDKByJNA.NET_DVR_WHITEBALANCE();
        public HCNetSDKByJNA.NET_DVR_EXPOSURE struExposure = new HCNetSDKByJNA.NET_DVR_EXPOSURE();
        public HCNetSDKByJNA.NET_DVR_GAMMACORRECT struGammaCorrect = new HCNetSDKByJNA.NET_DVR_GAMMACORRECT();
        public HCNetSDKByJNA.NET_DVR_WDR struWdr = new HCNetSDKByJNA.NET_DVR_WDR();
        public HCNetSDKByJNA.NET_DVR_DAYNIGHT struDayNight = new HCNetSDKByJNA.NET_DVR_DAYNIGHT();
        public HCNetSDKByJNA.NET_DVR_BACKLIGHT struBackLight = new HCNetSDKByJNA.NET_DVR_BACKLIGHT();
        public HCNetSDKByJNA.NET_DVR_NOISEREMOVE struNoiseRemove = new HCNetSDKByJNA.NET_DVR_NOISEREMOVE();
        public byte byPowerLineFrequencyMode;
        public byte byIrisMode;
        public byte byMirror;
        public byte byDigitalZoom;
        public byte byDeadPixelDetect;
        public byte byBlackPwl;
        public byte byEptzGate;
        public byte byLocalOutputGate;
        public byte byCoderOutputMode;
        public byte byLineCoding;
        public byte byDimmerMode;
        public byte byPaletteMode;
        public byte byEnhancedMode;
        public byte byDynamicContrastEN;
        public byte byDynamicContrast;
        public byte byJPEGQuality;
        public HCNetSDKByJNA.NET_DVR_CMOSMODECFG struCmosModeCfg = new HCNetSDKByJNA.NET_DVR_CMOSMODECFG();
        public byte byFilterSwitch;
        public byte byFocusSpeed;
        public byte byAutoCompensationInterval;
        public byte bySceneMode;
        public HCNetSDKByJNA.NET_DVR_DEFOGCFG struDefogCfg = new HCNetSDKByJNA.NET_DVR_DEFOGCFG();
        public HCNetSDKByJNA.NET_DVR_ELECTRONICSTABILIZATION struElectronicStabilization = new HCNetSDKByJNA.NET_DVR_ELECTRONICSTABILIZATION();
        public HCNetSDKByJNA.NET_DVR_CORRIDOR_MODE_CCD struCorridorMode = new HCNetSDKByJNA.NET_DVR_CORRIDOR_MODE_CCD();
        public byte byExposureSegmentEnable;
        public byte byBrightCompensate;
        public byte byCaptureModeN;
        public byte byCaptureModeP;
        public HCNetSDKByJNA.NET_DVR_SMARTIR_PARAM struSmartIRParam = new HCNetSDKByJNA.NET_DVR_SMARTIR_PARAM();
        public HCNetSDKByJNA.NET_DVR_PIRIS_PARAM struPIrisParam = new HCNetSDKByJNA.NET_DVR_PIRIS_PARAM();
        public HCNetSDKByJNA.NET_DVR_LASER_PARAM_CFG struLaserParam = new HCNetSDKByJNA.NET_DVR_LASER_PARAM_CFG();
        public HCNetSDKByJNA.NET_DVR_FFC_PARAM struFFCParam = new HCNetSDKByJNA.NET_DVR_FFC_PARAM();
        public HCNetSDKByJNA.NET_DVR_DDE_PARAM struDDEParam = new HCNetSDKByJNA.NET_DVR_DDE_PARAM();
        public HCNetSDKByJNA.NET_DVR_AGC_PARAM struAGCParam = new HCNetSDKByJNA.NET_DVR_AGC_PARAM();
        public byte byLensDistortionCorrection;
        public byte byDistortionCorrectionLevel;
        public byte byCalibrationAccurateLevel;
        public byte byZoomedInDistantViewLevel;
        public HCNetSDKByJNA.NET_DVR_SNAP_CAMERAPARAMCFG struSnapCCD = new HCNetSDKByJNA.NET_DVR_SNAP_CAMERAPARAMCFG();
        public HCNetSDKByJNA.NET_DVR_OPTICAL_DEHAZE struOpticalDehaze = new HCNetSDKByJNA.NET_DVR_OPTICAL_DEHAZE();
        public HCNetSDKByJNA.NET_DVR_THERMOMETRY_AGC struThermAGC = new HCNetSDKByJNA.NET_DVR_THERMOMETRY_AGC();
        public byte byFusionMode;
        public byte byHorizontalFOV;
        public byte byVerticalFOV;
        public byte byBrightnessSuddenChangeSuppression;
        public byte byGPSEnabled;
        public byte[] byRes2 = new byte[155];

        public NET_DVR_CAMERAPARAMCFG_EX() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struVideoEffect", "struGain", "struWhiteBalance", "struExposure", "struGammaCorrect", "struWdr", "struDayNight", "struBackLight", "struNoiseRemove", "byPowerLineFrequencyMode", "byIrisMode", "byMirror", "byDigitalZoom", "byDeadPixelDetect", "byBlackPwl", "byEptzGate", "byLocalOutputGate", "byCoderOutputMode", "byLineCoding", "byDimmerMode", "byPaletteMode", "byEnhancedMode", "byDynamicContrastEN", "byDynamicContrast", "byJPEGQuality", "struCmosModeCfg", "byFilterSwitch", "byFocusSpeed", "byAutoCompensationInterval", "bySceneMode", "struDefogCfg", "struElectronicStabilization", "struCorridorMode", "byExposureSegmentEnable", "byBrightCompensate", "byCaptureModeN", "byCaptureModeP", "struSmartIRParam", "struPIrisParam", "struLaserParam", "struFFCParam", "struDDEParam", "struAGCParam", "byLensDistortionCorrection", "byDistortionCorrectionLevel", "byCalibrationAccurateLevel", "byZoomedInDistantViewLevel", "struSnapCCD", "struOpticalDehaze", "struThermAGC", "byFusionMode", "byHorizontalFOV", "byVerticalFOV", "byBrightnessSuddenChangeSuppression", "byGPSEnabled", "byRes2");
        }
    }

    public static class NET_DVR_CAPTURE_FACE_CFG extends Structure {
        public int dwSize;
        public int dwFaceTemplate1Size;
        public ByteByReference pFaceTemplate1Buffer;
        public int dwFaceTemplate2Size;
        public ByteByReference pFaceTemplate2Buffer;
        public int dwFacePicSize;
        public ByteByReference pFacePicBuffer;
        public byte byFaceQuality1;
        public byte byFaceQuality2;
        public byte byCaptureProgress;
        public byte byRes1;
        public int dwInfraredFacePicSize;
        public ByteByReference pInfraredFacePicBuffer;
        public byte[] byRes = new byte[116];

        public NET_DVR_CAPTURE_FACE_CFG(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwFaceTemplate1Size", "pFaceTemplate1Buffer", "dwFaceTemplate2Size", "pFaceTemplate2Buffer", "dwFacePicSize", "pFacePicBuffer", "byFaceQuality1", "byFaceQuality2", "byCaptureProgress", "byRes1", "dwInfraredFacePicSize", "pInfraredFacePicBuffer", "byRes");
        }
    }

    public static class NET_DVR_CAPTURE_FACE_COND extends Structure {
        public int dwSize;
        public byte[] byRes = new byte[128];

        public NET_DVR_CAPTURE_FACE_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byRes");
        }
    }

    public static class NET_DVR_CARD_CFG extends Structure {
        public int dwSize;
        public int dwModifyParamType;
        public byte[] byCardNo = new byte[32];
        public byte byCardValid;
        public byte byCardType;
        public byte byLeaderCard;
        public byte byRes1;
        public int dwDoorRight;
        public HCNetSDKByJNA.NET_DVR_VALID_PERIOD_CFG struValid = new HCNetSDKByJNA.NET_DVR_VALID_PERIOD_CFG();
        public int dwBelongGroup;
        public byte[] byCardPassword = new byte[8];
        public HCNetSDKByJNA.arrayCardRightPlan[] byCardRightPlan = new HCNetSDKByJNA.arrayCardRightPlan[32];
        public int dwMaxSwipeTime;
        public int dwSwipeTime;
        public short wRoomNumber;
        public short wFloorNumber;
        public byte[] byRes2 = new byte[20];

        public NET_DVR_CARD_CFG(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwModifyParamType", "byCardNo", "byCardValid", "byCardType", "byLeaderCard", "byRes1", "dwDoorRight", "struValid", "dwBelongGroup", "byCardPassword", "byCardRightPlan", "dwMaxSwipeTime", "dwSwipeTime", "wRoomNumber", "wFloorNumber", "byRes2");
        }
    }

    public static class NET_DVR_CARD_CFG_COND extends Structure {
        public int dwSize;
        public int dwCardNum;
        public byte byCheckCardNo;
        public byte[] byRes1 = new byte[3];
        public short wLocalControllerID;
        public byte[] byRes2 = new byte[2];
        public int dwLockID;
        public byte[] byRes3 = new byte[20];

        public NET_DVR_CARD_CFG_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwCardNum", "byCheckCardNo", "byRes1", "wLocalControllerID", "byRes2", "dwLockID", "byRes3");
        }
    }

    public static class NET_DVR_CARD_CFG_SEND_DATA extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public int dwCardUserId;
        public byte[] byRes = new byte[12];

        public NET_DVR_CARD_CFG_SEND_DATA() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "dwCardUserId", "byRes");
        }
    }

    public static class NET_DVR_CARD_CFG_V50 extends Structure {
        public int dwSize;
        public int dwModifyParamType;
        public byte[] byCardNo = new byte[32];
        public byte byCardValid;
        public byte byCardType;
        public byte byLeaderCard;
        public byte byRes1;
        public byte[] byDoorRight = new byte[256];
        public HCNetSDKByJNA.tagNET_DVR_VALID_PERIOD_CFG struValid;
        public byte[] byBelongGroup = new byte[128];
        public byte[] byCardPassword = new byte[8];
        public HCNetSDKByJNA.NET_DVR_CARD_CFG_V50.short_arr_1[] wCardRightPlan = (HCNetSDKByJNA.NET_DVR_CARD_CFG_V50.short_arr_1[])(new HCNetSDKByJNA.NET_DVR_CARD_CFG_V50.short_arr_1()).toArray(4);
        public int dwMaxSwipeTime;
        public int dwSwipeTime;
        public short wRoomNumber;
        public short wFloorNumber;
        public int dwEmployeeNo;
        public byte[] byName = new byte[32];
        public short wDepartmentNo;
        public short wSchedulePlanNo;
        public byte bySchedulePlanType;
        public byte byRightType;
        public byte[] byRes2 = new byte[2];
        public int dwLockID;
        public byte[] byLockCode = new byte[8];
        public byte[] byRoomCode = new byte[8];
        public int dwCardRight;
        public int dwPlanTemplate;
        public int dwCardUserId;
        public byte byCardModelType;
        public byte[] byRes3 = new byte[51];
        public byte[] bySIMNum = new byte[32];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwModifyParamType", "byCardNo", "byCardValid", "byCardType", "byLeaderCard", "byRes1", "byDoorRight", "struValid", "byBelongGroup", "byCardPassword", "wCardRightPlan", "dwMaxSwipeTime", "dwSwipeTime", "wRoomNumber", "wFloorNumber", "dwEmployeeNo", "byName", "wDepartmentNo", "wSchedulePlanNo", "bySchedulePlanType", "byRightType", "byRes2", "dwLockID", "byLockCode", "byRoomCode", "dwCardRight", "dwPlanTemplate", "dwCardUserId", "byCardModelType", "byRes3", "bySIMNum");
        }

        public NET_DVR_CARD_CFG_V50(Pointer p) {
            super(p);
        }

        public static class short_arr_1 extends Structure {
            public short[] byKeyInfo = new short[256];

            public short_arr_1() {
            }

            protected List<String> getFieldOrder() {
                return Arrays.asList("byKeyInfo");
            }
        }
    }

    public static class NET_DVR_CARD_PASSWD_CFG extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public byte[] byCardPassword = new byte[8];
        public int dwErrorCode;
        public byte byCardValid;
        public byte[] byRes2 = new byte[23];

        public NET_DVR_CARD_PASSWD_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "byCardPassword", "dwErrorCode", "byCardValid", "byRes2");
        }
    }

    public static class NET_DVR_CARD_READER_CFG_V50 extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte byCardReaderType;
        public byte byOkLedPolarity;
        public byte byErrorLedPolarity;
        public byte byBuzzerPolarity;
        public byte bySwipeInterval;
        public byte byPressTimeout;
        public byte byEnableFailAlarm;
        public byte byMaxReadCardFailNum;
        public byte byEnableTamperCheck;
        public byte byOfflineCheckTime;
        public byte byFingerPrintCheckLevel;
        public byte byUseLocalController;
        public byte byRes1;
        public short wLocalControllerID;
        public short wLocalControllerReaderID;
        public short wCardReaderChannel;
        public byte byFingerPrintImageQuality;
        public byte byFingerPrintContrastTimeOut;
        public byte byFingerPrintRecogizeInterval;
        public byte byFingerPrintMatchFastMode;
        public byte byFingerPrintModuleSensitive;
        public byte byFingerPrintModuleLightCondition;
        public byte byFaceMatchThresholdN;
        public byte byFaceQuality;
        public byte byFaceRecogizeTimeOut;
        public byte byFaceRecogizeInterval;
        public short wCardReaderFunction;
        public byte[] byCardReaderDescription = new byte[32];
        public short wFaceImageSensitometry;
        public byte byLivingBodyDetect;
        public byte byFaceMatchThreshold1;
        public byte[] byRes = new byte[256];

        public NET_DVR_CARD_READER_CFG_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byCardReaderType", "byOkLedPolarity", "byErrorLedPolarity", "byBuzzerPolarity", "bySwipeInterval", "byPressTimeout", "byEnableFailAlarm", "byMaxReadCardFailNum", "byEnableTamperCheck", "byOfflineCheckTime", "byFingerPrintCheckLevel", "byUseLocalController", "byRes1", "wLocalControllerID", "wLocalControllerReaderID", "wCardReaderChannel", "byFingerPrintImageQuality", "byFingerPrintContrastTimeOut", "byFingerPrintRecogizeInterval", "byFingerPrintMatchFastMode", "byFingerPrintModuleSensitive", "byFingerPrintModuleLightCondition", "byFaceMatchThresholdN", "byFaceQuality", "byFaceRecogizeTimeOut", "byFaceRecogizeInterval", "wCardReaderFunction", "byCardReaderDescription", "wFaceImageSensitometry", "byLivingBodyDetect", "byFaceMatchThreshold1", "byRes");
        }
    }

    public static class NET_DVR_CMOSMODECFG extends Structure {
        public byte byCaptureMod;
        public byte byBrightnessGate;
        public byte byCaptureGain1;
        public byte byCaptureGain2;
        public int dwCaptureShutterSpeed1;
        public int dwCaptureShutterSpeed2;
        public byte[] byRes = new byte[4];

        public NET_DVR_CMOSMODECFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byCaptureMod", "byBrightnessGate", "byCaptureGain1", "byCaptureGain2", "dwCaptureShutterSpeed1", "dwCaptureShutterSpeed2", "byRes");
        }
    }

    public static class NET_DVR_COLOR extends Structure {
        public byte byBrightness;
        public byte byContrast;
        public byte bySaturation;
        public byte byHue;

        public NET_DVR_COLOR() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byBrightness", "byContrast", "bySaturation", "byHue");
        }
    }

    public static class NET_DVR_COMPLETE_RESTORE_INFO extends Structure {
        public int dwSize;
        public int dwChannel;
        public byte[] byRes = new byte[64];

        public NET_DVR_COMPLETE_RESTORE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannel", "byRes");
        }
    }

    public static class NET_DVR_COMPRESSION_INFO_V30 extends Structure {
        public byte byStreamType;
        public byte byResolution;
        public byte byBitrateType;
        public byte byPicQuality;
        public int dwVideoBitrate;
        public int dwVideoFrameRate;
        public short wIntervalFrameI;
        public byte byIntervalBPFrame;
        public byte byENumber;
        public byte byVideoEncType;
        public byte byAudioEncType;
        public byte[] byres = new byte[10];

        public NET_DVR_COMPRESSION_INFO_V30() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byStreamType", "byResolution", "byBitrateType", "byPicQuality", "dwVideoBitrate", "dwVideoFrameRate", "wIntervalFrameI", "byIntervalBPFrame", "byENumber", "byVideoEncType", "byAudioEncType", "byres");
        }
    }

    public static class NET_DVR_CORRIDOR_MODE_CCD extends Structure {
        public byte byEnableCorridorMode;
        public byte[] byRes = new byte[11];

        public NET_DVR_CORRIDOR_MODE_CCD() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnableCorridorMode", "byRes");
        }
    }

    public static class NET_DVR_CURTRIGGERMODE extends Structure {
        public int dwSize;
        public int dwTriggerType;
        public byte[] byRes = new byte[24];

        public NET_DVR_CURTRIGGERMODE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwTriggerType", "byRes");
        }
    }

    public static class NET_DVR_DATE extends Structure {
        public short wYear;
        public byte byMonth;
        public byte byDay;

        public NET_DVR_DATE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wYear", "byMonth", "byDay");
        }
    }

    public static class NET_DVR_DATE_ extends Structure {
        public short wYear;
        public byte byMonth;
        public byte byDay;

        public NET_DVR_DATE_() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wYear", "byMonth", "byDay");
        }
    }

    public static class NET_DVR_DAYNIGHT extends Structure {
        public byte byDayNightFilterType;
        public byte bySwitchScheduleEnabled;
        public byte byBeginTime;
        public byte byEndTime;
        public byte byDayToNightFilterLevel;
        public byte byNightToDayFilterLevel;
        public byte byDayNightFilterTime;
        public byte byBeginTimeMin;
        public byte byBeginTimeSec;
        public byte byEndTimeMin;
        public byte byEndTimeSec;
        public byte byAlarmTrigState;

        public NET_DVR_DAYNIGHT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byDayNightFilterType", "bySwitchScheduleEnabled", "byBeginTime", "byEndTime", "byDayToNightFilterLevel", "byNightToDayFilterLevel", "byDayNightFilterTime", "byBeginTimeMin", "byBeginTimeSec", "byEndTimeMin", "byEndTimeSec", "byAlarmTrigState");
        }
    }

    public static class NET_DVR_DAYTIME extends Structure {
        public byte byHour;
        public byte byMinute;
        public byte bySecond;
        public byte byRes;
        public short wMilliSecond;
        public byte[] byRes1 = new byte[2];

        public NET_DVR_DAYTIME() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byHour", "byMinute", "bySecond", "byRes", "wMilliSecond", "byRes1");
        }
    }

    public static class NET_DVR_DDE_PARAM extends Structure {
        public byte byMode;
        public byte byNormalLevel;
        public byte byExpertLevel;
        public byte[] byRes = new byte[5];

        public NET_DVR_DDE_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byNormalLevel", "byExpertLevel", "byRes");
        }
    }

    public static class NET_DVR_DEFOGCFG extends Structure {
        public byte byMode;
        public byte byLevel;
        public byte[] byRes = new byte[6];

        public NET_DVR_DEFOGCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byLevel", "byRes");
        }
    }

    public static class NET_DVR_DEL_FACE_PARAM_MODE extends Union {
        public byte[] uLen = new byte[588];
        public HCNetSDKByJNA.NET_DVR_FACE_PARAM_BYCARD struByCard = new HCNetSDKByJNA.NET_DVR_FACE_PARAM_BYCARD();
        public HCNetSDKByJNA.NET_DVR_FACE_PARAM_BYREADER struByReader = new HCNetSDKByJNA.NET_DVR_FACE_PARAM_BYREADER();

        public NET_DVR_DEL_FACE_PARAM_MODE() {
        }
    }

    public static class NET_DVR_DEVICECFG_V40 extends Structure {
        public int dwSize;
        public byte[] sDVRName = new byte[32];
        public int dwDVRID;
        public int dwRecycleRecord;
        public byte[] sSerialNumber = new byte[48];
        public int dwSoftwareVersion;
        public int dwSoftwareBuildDate;
        public int dwDSPSoftwareVersion;
        public int dwDSPSoftwareBuildDate;
        public int dwPanelVersion;
        public int dwHardwareVersion;
        public byte byAlarmInPortNum;
        public byte byAlarmOutPortNum;
        public byte byRS232Num;
        public byte byRS485Num;
        public byte byNetworkPortNum;
        public byte byDiskCtrlNum;
        public byte byDiskNum;
        public byte byDVRType;
        public byte byChanNum;
        public byte byStartChan;
        public byte byDecordChans;
        public byte byVGANum;
        public byte byUSBNum;
        public byte byAuxoutNum;
        public byte byAudioNum;
        public byte byIPChanNum;
        public byte byZeroChanNum;
        public byte bySupport;
        public byte byEsataUseage;
        public byte byIPCPlug;
        public byte byStorageMode;
        public byte bySupport1;
        public short wDevType;
        public byte[] byDevTypeName = new byte[64];
        public byte bySupport2;
        public byte byAnalogAlarmInPortNum;
        public byte byStartAlarmInNo;
        public byte byStartAlarmOutNo;
        public byte byStartIPAlarmInNo;
        public byte byStartIPAlarmOutNo;
        public byte byHighIPChanNum;
        public byte[] byRes2 = new byte[9];

        public NET_DVR_DEVICECFG_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "sDVRName", "dwDVRID", "dwRecycleRecord", "sSerialNumber", "dwSoftwareVersion", "dwSoftwareBuildDate", "dwDSPSoftwareVersion", "dwDSPSoftwareBuildDate", "dwPanelVersion", "dwHardwareVersion", "byAlarmInPortNum", "byAlarmOutPortNum", "byRS232Num", "byRS485Num", "byNetworkPortNum", "byDiskCtrlNum", "byDiskNum", "byDVRType", "byChanNum", "byStartChan", "byDecordChans", "byVGANum", "byUSBNum", "byAuxoutNum", "byAudioNum", "byIPChanNum", "byZeroChanNum", "bySupport", "byEsataUseage", "byIPCPlug", "byStorageMode", "bySupport1", "wDevType", "byDevTypeName", "bySupport2", "byAnalogAlarmInPortNum", "byStartAlarmInNo", "byStartAlarmOutNo", "byStartIPAlarmInNo", "byStartIPAlarmOutNo", "byHighIPChanNum", "byRes2");
        }
    }

    public static class NET_DVR_DEVICEINFO_V30 extends Structure {
        public byte[] sSerialNumber = new byte[48];
        public byte byAlarmInPortNum;
        public byte byAlarmOutPortNum;
        public byte byDiskNum;
        public byte byDVRType;
        public byte byChanNum;
        public byte byStartChan;
        public byte byAudioChanNum;
        public byte byIPChanNum;
        public byte byZeroChanNum;
        public byte byMainProto;
        public byte bySubProto;
        public byte bySupport;
        public byte bySupport1;
        public byte bySupport2;
        public short wDevType;
        public byte bySupport3;
        public byte byMultiStreamProto;
        public byte byStartDChan;
        public byte byStartDTalkChan;
        public byte byHighDChanNum;
        public byte bySupport4;
        public byte byLanguageType;
        public byte byVoiceInChanNum;
        public byte byStartVoiceInChanNo;
        public byte[] byRes3 = new byte[2];
        public byte byMirrorChanNum;
        public short wStartMirrorChanNo;
        public byte[] byRes2 = new byte[2];

        public NET_DVR_DEVICEINFO_V30() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("sSerialNumber", "byAlarmInPortNum", "byAlarmOutPortNum", "byDiskNum", "byDVRType", "byChanNum", "byStartChan", "byAudioChanNum", "byIPChanNum", "byZeroChanNum", "byMainProto", "bySubProto", "bySupport", "bySupport1", "bySupport2", "wDevType", "bySupport3", "byMultiStreamProto", "byStartDChan", "byStartDTalkChan", "byHighDChanNum", "bySupport4", "byLanguageType", "byVoiceInChanNum", "byStartVoiceInChanNo", "byRes3", "byMirrorChanNum", "wStartMirrorChanNo", "byRes2");
        }
    }

    public static class NET_DVR_DEVICEINFO_V40 extends Structure {
        public HCNetSDKByJNA.NET_DVR_DEVICEINFO_V30 struDeviceV30 = new HCNetSDKByJNA.NET_DVR_DEVICEINFO_V30();
        public byte bySupportLock;
        public byte byRetryLoginTime;
        public byte byPasswordLevel;
        public byte byProxyType;
        public int dwSurplusLockTime;
        public byte byCharEncodeType;
        public byte bySupportDev5;
        public byte byLoginMode;
        public byte[] byRes2 = new byte[253];

        public NET_DVR_DEVICEINFO_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struDeviceV30", "bySupportLock", "byRetryLoginTime", "byPasswordLevel", "byProxyType", "dwSurplusLockTime", "byCharEncodeType", "bySupportDev5", "byLoginMode", "byRes2");
        }
    }

    public static class NET_DVR_DEVICE_RUN_STATUS extends Structure {
        public int dwSize;
        public int dwMemoryTotal;
        public int dwMemoryUsage;
        public byte byCPUUsage;
        public byte byMainFrameTemp;
        public byte byBackPanelTemp;
        public byte byRes1;
        public byte[] byLeftDecResource = new byte[32];
        public float fNetworkFlow;
        public byte[] byRes2 = new byte[88];

        public NET_DVR_DEVICE_RUN_STATUS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwMemoryTotal", "dwMemoryUsage", "byCPUUsage", "byMainFrameTemp", "byBackPanelTemp", "byRes1", "byLeftDecResource", "fNetworkFlow", "byRes2");
        }
    }

    public static class NET_DVR_DEV_CHAN_INFO extends Structure {
        public HCNetSDKByJNA.NET_DVR_IPADDR struIP = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public short wDVRPort;
        public byte byChannel;
        public byte byTransProtocol;
        public byte byTransMode;
        public byte byFactoryType;
        public byte byDeviceType;
        public byte byDispChan;
        public byte bySubDispChan;
        public byte byResolution;
        public byte[] byRes = new byte[2];
        public byte[] sDomain = new byte[64];
        public byte[] sUserName = new byte[32];
        public byte[] sPassword = new byte[16];

        public NET_DVR_DEV_CHAN_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struIP", "wDVRPort", "byChannel", "byTransProtocol", "byTransMode", "byFactoryType", "byDeviceType", "byDispChan", "bySubDispChan", "byResolution", "byRes", "sDomain", "sUserName", "sPassword");
        }
    }

    public static class NET_DVR_DIRECTION extends Structure {
        public HCNetSDKByJNA.NET_DVR_PTZPOS struStartPoint = new HCNetSDKByJNA.NET_DVR_PTZPOS();
        public HCNetSDKByJNA.NET_DVR_PTZPOS struEndPoint = new HCNetSDKByJNA.NET_DVR_PTZPOS();

        public NET_DVR_DIRECTION() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struStartPoint", "struEndPoint");
        }
    }

    public static class NET_DVR_DISPLAYCFG extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_DISPLAYPARAM[] struDisplayParam = new HCNetSDKByJNA.NET_DVR_DISPLAYPARAM[512];
        public byte[] byRes = new byte[128];

        public NET_DVR_DISPLAYCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struDisplayParam", "byRes");
        }
    }

    public static class NET_DVR_DISPLAYPARAM extends Structure {
        public int dwDisplayNo;
        public byte byDispChanType;
        public byte[] byRes = new byte[11];

        public NET_DVR_DISPLAYPARAM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwDisplayNo", "byDispChanType", "byRes");
        }
    }

    public static class NET_DVR_DNMODE extends Structure {
        public byte byObjectSize;
        public byte byMotionSensitive;
        public byte[] byRes = new byte[6];

        public NET_DVR_DNMODE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byObjectSize", "byMotionSensitive", "byRes");
        }
    }

    public static class NET_DVR_DOOR_CFG extends Structure {
        public int dwSize;
        public byte[] byDoorName = new byte[32];
        public byte byMagneticType;
        public byte byOpenButtonType;
        public byte byOpenDuration;
        public byte byDisabledOpenDuration;
        public byte byMagneticAlarmTimeout;
        public byte byEnableDoorLock;
        public byte byEnableLeaderCard;
        public byte byLeaderCardMode;
        public int dwLeaderCardOpenDuration;
        public byte[] byStressPassword = new byte[8];
        public byte[] bySuperPassword = new byte[8];
        public byte[] byUnlockPassword = new byte[8];
        public byte byUseLocalController;
        public byte byRes1;
        public short wLocalControllerID;
        public short wLocalControllerDoorNumber;
        public short wLocalControllerStatus;
        public byte byLockInputCheck;
        public byte byLockInputType;
        public byte byDoorTerminalMode;
        public byte byOpenButton;
        public byte byLadderControlDelayTime;
        public byte[] byRes2 = new byte[43];

        public NET_DVR_DOOR_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byDoorName", "byMagneticType", "byOpenButtonType", "byOpenDuration", "byDisabledOpenDuration", "byMagneticAlarmTimeout", "byEnableDoorLock", "byEnableLeaderCard", "byLeaderCardMode", "dwLeaderCardOpenDuration", "byStressPassword", "bySuperPassword", "byUnlockPassword", "byUseLocalController", "byRes1", "wLocalControllerID", "wLocalControllerDoorNumber", "wLocalControllerStatus", "byLockInputCheck", "byLockInputType", "byDoorTerminalMode", "byOpenButton", "byLadderControlDelayTime", "byRes2");
        }
    }

    public static class NET_DVR_ELECTRONICSTABILIZATION extends Structure {
        public byte byEnable;
        public byte byLevel;
        public byte[] byRes = new byte[6];

        public NET_DVR_ELECTRONICSTABILIZATION() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byLevel", "byRes");
        }
    }

    public static class NET_DVR_ETHERNET_V30 extends Structure {
        public HCNetSDKByJNA.NET_DVR_IPADDR[] struDVRIP = new HCNetSDKByJNA.NET_DVR_IPADDR[2];
        public HCNetSDKByJNA.NET_DVR_IPADDR struDVRIPMask = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public int dwNetInterface;
        public short wDVRPort;
        public short wMTU;
        public byte[] byMACAddr = new byte[6];
        public byte byEthernetPortNo;
        public byte[] byRes = new byte[1];

        public NET_DVR_ETHERNET_V30() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struDVRIP", "struDVRIPMask", "dwNetInterface", "wDVRPort", "wMTU", "byMACAddr", "byEthernetPortNo", "byRes");
        }
    }

    public static class NET_DVR_EVENT_CARD_LINKAGE_CFG_V51 extends Structure {
        public int dwSize;
        public byte byProMode;
        public byte[] byRes1 = new byte[3];
        public int dwEventSourceID;
        public HCNetSDKByJNA.NET_DVR_EVETN_CARD_LINKAGE_UNION uLinkageInfo = new HCNetSDKByJNA.NET_DVR_EVETN_CARD_LINKAGE_UNION();
        public byte[] byAlarmout = new byte[512];
        public byte[] byRes2 = new byte[32];
        public byte[] byOpenDoor = new byte[256];
        public byte[] byCloseDoor = new byte[256];
        public byte[] byNormalOpen = new byte[256];
        public byte[] byNormalClose = new byte[256];
        public byte byMainDevBuzzer;
        public byte byCapturePic;
        public byte byRecordVideo;
        public byte byMainDevStopBuzzer;
        public byte[] byRes3 = new byte[28];
        public byte[] byReaderBuzzer = new byte[512];
        public byte[] byAlarmOutClose = new byte[512];
        public byte[] byAlarmInSetup = new byte[512];
        public byte[] byAlarmInClose = new byte[512];
        public byte[] byReaderStopBuzzer = new byte[512];
        public byte[] byRes = new byte[512];

        public NET_DVR_EVENT_CARD_LINKAGE_CFG_V51() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byProMode", "byRes1", "dwEventSourceID", "uLinkageInfo", "byAlarmout", "byRes2", "byOpenDoor", "byCloseDoor", "byNormalOpen", "byNormalClose", "byMainDevBuzzer", "byCapturePic", "byRecordVideo", "byMainDevStopBuzzer", "byRes3", "byReaderBuzzer", "byAlarmOutClose", "byAlarmInSetup", "byAlarmInClose", "byReaderStopBuzzer", "byRes");
        }
    }

    public static class NET_DVR_EVENT_CARD_LINKAGE_COND extends Structure {
        public int dwSize;
        public int dwEventID;
        public short wLocalControllerID;
        public byte[] byRes = new byte[106];

        public NET_DVR_EVENT_CARD_LINKAGE_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwEventID", "wLocalControllerID", "byRes");
        }
    }

    public static class NET_DVR_EVENT_LINKAGE_INFO extends Structure {
        public short wMainEventType;
        public short wSubEventType;
        public byte[] byRes = new byte[28];

        public NET_DVR_EVENT_LINKAGE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wMainEventType", "wSubEventType", "byRes");
        }
    }

    public static class NET_DVR_EVENT_SCHEDULE extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_SCHEDTIME_DAYS[] struAlarmTime = (HCNetSDKByJNA.NET_DVR_SCHEDTIME_DAYS[])(new HCNetSDKByJNA.NET_DVR_SCHEDTIME_DAYS()).toArray(8);
        public byte[] byRes = new byte[160];

        public NET_DVR_EVENT_SCHEDULE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struHandleException", "byRes");
        }
    }

    public static class NET_DVR_EVENT_TRIGGER extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_HANDLEEXCEPTION_V41 struHandleException = new HCNetSDKByJNA.NET_DVR_HANDLEEXCEPTION_V41();
        public byte[] byRes = new byte[14592];

        public NET_DVR_EVENT_TRIGGER() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struHandleException", "byRes");
        }
    }

    public static class NET_DVR_EVETN_CARD_LINKAGE_UNION extends Union {
        public byte[] byCardNo = new byte[32];
        public HCNetSDKByJNA.NET_DVR_EVENT_LINKAGE_INFO struEventLinkage = new HCNetSDKByJNA.NET_DVR_EVENT_LINKAGE_INFO();
        public byte[] byMACAddr = new byte[6];
        public byte[] byEmployeeNo = new byte[32];

        public NET_DVR_EVETN_CARD_LINKAGE_UNION() {
        }
    }

    public static class NET_DVR_EXPOSURE extends Structure {
        public byte byExposureMode;
        public byte byAutoApertureLevel;
        public byte[] byRes = new byte[2];
        public int dwVideoExposureSet;
        public int dwExposureUserSet;
        public int dwRes;

        public NET_DVR_EXPOSURE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byExposureMode", "byAutoApertureLevel", "byRes", "dwVideoExposureSet", "dwExposureUserSet", "dwRes");
        }
    }

    public static class NET_DVR_EZVIZ_ACCESS_CFG extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte byDeviceStatus;
        public byte byAllowRedirect;
        public byte[] byDomainName = new byte[64];
        public byte byRes1;
        public byte[] byVerificationCode = new byte[32];
        public byte byNetMode;
        public byte[] byRes = new byte[411];

        public NET_DVR_EZVIZ_ACCESS_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byDeviceStatus", "byAllowRedirect", "byDomainName", "byRes1", "byVerificationCode", "byNetMode", "byRes");
        }
    }

    public static class NET_DVR_FACELIB_COND extends Structure {
        public int dwSize;
        public byte[] szFDID = new byte[256];
        public byte byConcurrent;
        public byte[] byRes = new byte[127];

        public NET_DVR_FACELIB_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "szFDID", "byConcurrent", "byRes");
        }
    }

    public static class NET_DVR_FACE_DETECTION extends Structure {
        public int dwSize;
        public int dwRelativeTime;
        public int dwAbsTime;
        public int dwBackgroundPicLen;
        public HCNetSDKByJNA.NET_VCA_DEV_INFO struDevInfo = new HCNetSDKByJNA.NET_VCA_DEV_INFO();
        public HCNetSDKByJNA.NET_VCA_RECT[] struFacePic = (HCNetSDKByJNA.NET_VCA_RECT[])(new HCNetSDKByJNA.NET_VCA_RECT()).toArray(30);
        public byte byFacePicNum;
        public byte byRes1;
        public short wDevInfoIvmsChannelEx;
        public byte[] byRes = new byte[252];
        public ByteByReference pBackgroundPicpBuffer;

        public NET_DVR_FACE_DETECTION(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwRelativeTime", "dwAbsTime", "dwBackgroundPicLen", "struDevInfo", "struFacePic", "byFacePicNum", "byRes1", "wDevInfoIvmsChannelEx", "byRes", "pBackgroundPicpBuffer");
        }
    }

    public static class NET_DVR_FACE_EXTRA_INFO extends Structure {
        public HCNetSDKByJNA.NET_VCA_RECT[] struVcaRect = (HCNetSDKByJNA.NET_VCA_RECT[])(new HCNetSDKByJNA.NET_VCA_RECT()).toArray(30);
        public byte[] byRes = new byte[64];

        public NET_DVR_FACE_EXTRA_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struVcaRect", "byRes");
        }
    }

    public static class NET_DVR_FACE_PARAM_BYCARD extends Structure {
        public byte[] byCardNo = new byte[32];
        public byte[] byEnableCardReader = new byte[512];
        public byte[] byFaceID = new byte[2];
        public byte[] byRes1 = new byte[42];

        public NET_DVR_FACE_PARAM_BYCARD() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byCardNo", "byEnableCardReader", "byFaceID", "byRes1");
        }
    }

    public static class NET_DVR_FACE_PARAM_BYREADER extends Structure {
        public int dwCardReaderNo;
        public byte byClearAllCard;
        public byte[] byRes1 = new byte[3];
        public byte[] byCardNo = new byte[32];
        public byte[] byRes = new byte[548];

        public NET_DVR_FACE_PARAM_BYREADER() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwCardReaderNo", "byClearAllCard", "byRes1", "byCardNo", "byRes");
        }
    }

    public static class NET_DVR_FACE_PARAM_CFG extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public int dwFaceLen;
        public Pointer pFaceBuffer;
        public byte[] byEnableCardReader = new byte[512];
        public byte byFaceID;
        public byte byFaceDataType;
        public byte[] byRes = new byte[126];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "dwFaceLen", "pFaceBuffer", "byEnableCardReader", "byFaceID", "byFaceDataType", "byRes");
        }

        public NET_DVR_FACE_PARAM_CFG(Pointer p) {
            super(p);
        }
    }

    public static class NET_DVR_FACE_PARAM_COND extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public byte[] byEnableCardReader = new byte[512];
        public int dwFaceNum;
        public byte byFaceID;
        public byte[] byRes = new byte[127];

        public NET_DVR_FACE_PARAM_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "byEnableCardReader", "dwFaceNum", "byFaceID", "byRes");
        }
    }

    public static class NET_DVR_FACE_PARAM_CTRL extends Structure {
        public int dwSize;
        public byte byMode;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_DEL_FACE_PARAM_MODE struProcessMode = new HCNetSDKByJNA.NET_DVR_DEL_FACE_PARAM_MODE();
        public byte[] byRes = new byte[64];

        public NET_DVR_FACE_PARAM_CTRL() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byMode", "byRes1", "struProcessMode", "byRes");
        }
    }

    public static class NET_DVR_FACE_PARAM_STATUS extends Structure {
        public int dwSize;
        public byte[] byCardNo = new byte[32];
        public byte[] byCardReaderRecvStatus = new byte[512];
        public byte[] byErrorMsg = new byte[32];
        public int dwCardReaderNo;
        public byte byTotalStatus;
        public byte byFaceID;
        public byte[] byRes = new byte[130];

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byCardNo", "byCardReaderRecvStatus", "byErrorMsg", "dwCardReaderNo", "byTotalStatus", "byFaceID", "byRes");
        }

        public NET_DVR_FACE_PARAM_STATUS(Pointer p) {
            super(p);
        }
    }

    public static class NET_DVR_FFC_MANUAL_INFO extends Structure {
        public int dwSize;
        public int dwChannel;
        public byte[] byRes = new byte[64];

        public NET_DVR_FFC_MANUAL_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannel", "byRes");
        }
    }

    public static class NET_DVR_FFC_PARAM extends Structure {
        public byte byMode;
        public byte byRes1;
        public short wCompensateTime;
        public byte[] byRes2 = new byte[4];

        public NET_DVR_FFC_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byRes1", "wCompensateTime", "byRes2");
        }
    }

    public static class NET_DVR_FIND_PICTURE_PARAM extends Structure {
        public int dwSize;
        public int lChannel;
        public byte byFileType;
        public byte byNeedCard;
        public byte byProvince;
        public byte byRes1;
        public byte[] sCardNum = new byte[40];
        public HCNetSDKByJNA.NET_DVR_TIME struStartTime = new HCNetSDKByJNA.NET_DVR_TIME();
        public HCNetSDKByJNA.NET_DVR_TIME struStopTime = new HCNetSDKByJNA.NET_DVR_TIME();
        public int dwTrafficType;
        public int dwVehicleType;
        public int dwIllegalType;
        public byte byLaneNo;
        public byte bySubHvtType;
        public byte[] byRes2 = new byte[2];
        public byte[] sLicense = new byte[16];
        public byte byRegion;
        public byte byCountry;
        public byte[] byRes3 = new byte[6];

        public NET_DVR_FIND_PICTURE_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "lChannel", "byFileType", "byNeedCard", "byProvince", "byRes1", "sCardNum", "struStartTime", "struStopTime", "dwTrafficType", "dwVehicleType", "dwIllegalType", "byLaneNo", "bySubHvtType", "byRes2", "sLicense", "byRegion", "byCountry", "byRes3");
        }
    }

    public static class NET_DVR_FIND_PICTURE_V40 extends Structure {
        public byte[] sFileName = new byte[64];
        public HCNetSDKByJNA.NET_DVR_TIME struTime = new HCNetSDKByJNA.NET_DVR_TIME();
        public int dwFileSize;
        public byte[] sCardNum = new byte[40];
        public byte byPlateColor;
        public byte byVehicleLogo;
        public byte byFileType;
        public byte byRecogResult;
        public byte[] sLicense = new byte[16];
        public byte byEventSearchStatus;
        public byte[] byRes = new byte[75];
        public HCNetSDKByJNA.NET_DVR_PIC_EXTRA_INFO_UNION uPicExtraInfo = new HCNetSDKByJNA.NET_DVR_PIC_EXTRA_INFO_UNION();

        public NET_DVR_FIND_PICTURE_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("sFileName", "struTime", "dwFileSize", "sCardNum", "byPlateColor", "byVehicleLogo", "byFileType", "byRecogResult", "sLicense", "byEventSearchStatus", "byRes", "uPicExtraInfo");
        }
    }

    public static class NET_DVR_GAIN extends Structure {
        public byte byGainLevel;
        public byte byGainUserSet;
        public byte[] byRes = new byte[2];
        public int dwMaxGainValue;

        public NET_DVR_GAIN() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byGainLevel", "byGainUserSet", "byRes", "dwMaxGainValue");
        }
    }

    public static class NET_DVR_GAMMACORRECT extends Structure {
        public byte byGammaCorrectionEnabled;
        public byte byGammaCorrectionLevel;
        public byte[] byRes = new byte[6];

        public NET_DVR_GAMMACORRECT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byGammaCorrectionEnabled", "byGammaCorrectionLevel", "byRes");
        }
    }

    public static class NET_DVR_HANDLEEXCEPTION_V41 extends Structure {
        public int dwHandleType;
        public byte[] byRes = new byte[16580];

        public NET_DVR_HANDLEEXCEPTION_V41() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwHandleType", "byRes");
        }
    }

    public static class NET_DVR_HIDEALARM_V40 extends Structure {
        public int dwEnableHideAlarm;
        public short wHideAlarmAreaTopLeftX;
        public short wHideAlarmAreaTopLeftY;
        public short wHideAlarmAreaWidth;
        public short wHideAlarmAreaHeight;
        public int dwHandleType;
        public int dwMaxRelAlarmOutChanNum;
        public int[] dwRelAlarmOut = new int[4128];
        public HCNetSDKByJNA.NET_DVR_SCHEDTIMEWEEK[] struAlarmTime = new HCNetSDKByJNA.NET_DVR_SCHEDTIMEWEEK[7];
        public byte[] byRes = new byte[64];

        public NET_DVR_HIDEALARM_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwEnableHideAlarm", "wHideAlarmAreaTopLeftX", "wHideAlarmAreaTopLeftY", "wHideAlarmAreaWidth", "wHideAlarmAreaHeight", "dwHandleType", "dwMaxRelAlarmOutChanNum", "dwRelAlarmOut", "struAlarmTime", "byRes");
        }
    }

    public static class NET_DVR_HOLIDAY_GROUP_CFG extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public byte[] byGroupName = new byte[32];
        public int[] dwHolidayPlanNo = new int[16];
        public byte[] byRes2 = new byte[32];

        public NET_DVR_HOLIDAY_GROUP_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byRes1", "byGroupName", "dwHolidayPlanNo", "byRes2");
        }
    }

    public static class NET_DVR_HOLIDAY_GROUP_COND extends Structure {
        public int dwSize;
        public int dwHolidayGroupNumber;
        public short wLocalControllerID;
        public byte[] byRes = new byte[106];

        public NET_DVR_HOLIDAY_GROUP_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwHolidayGroupNumber", "wLocalControllerID", "byRes");
        }
    }

    public static class NET_DVR_HOLIDAY_PLAN_CFG extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_DATE struBeginDate = new HCNetSDKByJNA.NET_DVR_DATE();
        public HCNetSDKByJNA.NET_DVR_DATE struEndDate = new HCNetSDKByJNA.NET_DVR_DATE();
        public HCNetSDKByJNA.NET_DVR_SINGLE_PLAN_SEGMENT[] struPlanCfg = (HCNetSDKByJNA.NET_DVR_SINGLE_PLAN_SEGMENT[])(new HCNetSDKByJNA.NET_DVR_SINGLE_PLAN_SEGMENT()).toArray(8);
        public byte[] byRes2 = new byte[16];

        public NET_DVR_HOLIDAY_PLAN_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byRes1", "struBeginDate", "struEndDate", "struPlanCfg", "byRes2");
        }
    }

    public static class NET_DVR_HOLIDAY_PLAN_COND extends Structure {
        public int dwSize;
        public int dwHolidayPlanNumber;
        public short wLocalControllerID;
        public byte[] byRes = new byte[106];

        public NET_DVR_HOLIDAY_PLAN_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwHolidayPlanNumber", "wLocalControllerID", "byRes");
        }
    }

    public static class NET_DVR_ID_CARD_INFO extends Structure {
        public int dwSize;
        public byte[] byName = new byte[128];
        public HCNetSDKByJNA.NET_DVR_DATE_ struBirth;
        public byte[] byAddr = new byte[280];
        public byte[] byIDNum = new byte[32];
        public byte[] byIssuingAuthority = new byte[128];
        public HCNetSDKByJNA.NET_DVR_DATE_ struStartDate;
        public HCNetSDKByJNA.NET_DVR_DATE_ struEndDate;
        public byte byTermOfValidity;
        public byte bySex;
        public byte byNation;
        public byte[] byRes = new byte[101];

        public NET_DVR_ID_CARD_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byName", "struBirth", "byAddr", "byIDNum", "byIssuingAuthority", "struStartDate", "struEndDate", "byTermOfValidity", "bySex", "byNation", "byRes");
        }
    }

    public static class NET_DVR_ID_CARD_INFO_ALARM extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_ID_CARD_INFO struIDCardCfg;
        public int dwMajor;
        public int dwMinor;
        public HCNetSDKByJNA.NET_DVR_TIME_V30 struSwipeTime;
        public byte[] byNetUser = new byte[16];
        public HCNetSDKByJNA.NET_DVR_IPADDR struRemoteHostAddr;
        public int dwCardReaderNo;
        public int dwDoorNo;
        public int dwPicDataLen;
        public String pPicData;
        public byte byCardType;
        public byte byDeviceNo;
        public byte[] byRes2 = new byte[2];
        public int dwFingerPrintDataLen;
        public String pFingerPrintData;
        public int dwCapturePicDataLen;
        public String pCapturePicData;
        public byte[] byRes = new byte[188];

        public NET_DVR_ID_CARD_INFO_ALARM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struIDCardCfg", "dwMajor", "dwMinor", "struSwipeTime", "byNetUser", "struRemoteHostAddr", "dwCardReaderNo", "dwDoorNo", "dwPicDataLen", "pPicData", "byCardType", "byDeviceNo", "byRes2", "dwFingerPrintDataLen", "pFingerPrintData", "dwCapturePicDataLen", "pCapturePicData", "byRes");
        }
    }

    public static class NET_DVR_INPUTSTREAMCFG_V40 extends Structure {
        public int dwSize;
        public byte byValid;
        public byte byCamMode;
        public short wInputNo;
        public byte[] sCamName = new byte[32];
        public HCNetSDKByJNA.NET_DVR_VIDEOEFFECT struVideoEffect = new HCNetSDKByJNA.NET_DVR_VIDEOEFFECT();
        public HCNetSDKByJNA.NET_DVR_PU_STREAM_CFG struPuStream = new HCNetSDKByJNA.NET_DVR_PU_STREAM_CFG();
        public short wBoardNum;
        public short wInputIdxOnBoard;
        public int dwResolution;
        public byte byVideoFormat;
        public byte byStatus;
        public byte[] sGroupName = new byte[32];
        public byte byJointMatrix;
        public byte byJointNo;
        public byte byColorMode;
        public byte byScreenServer;
        public byte[] byRes1 = new byte[2];
        public int dwInputSignalNo;
        public byte[] byRes = new byte[120];

        public NET_DVR_INPUTSTREAMCFG_V40() {
        }

        public NET_DVR_INPUTSTREAMCFG_V40(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byValid", "byCamMode", "wInputNo", "sCamName", "struVideoEffect", "struPuStream", "wBoardNum", "wInputIdxOnBoard", "dwResolution", "byVideoFormat", "byStatus", "sGroupName", "byJointMatrix", "byJointNo", "byColorMode", "byScreenServer", "byRes1", "dwInputSignalNo", "byRes");
        }
    }

    public static class NET_DVR_INPUTVOLUME extends Structure {
        public int dwSize;
        public byte byAudioInputChan;
        public byte[] byRes = new byte[63];

        public NET_DVR_INPUTVOLUME() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byAudioInputChan", "byRes");
        }
    }

    public static class NET_DVR_INPUT_SIGNAL_LIST extends Structure {
        public int dwSize;
        public int dwInputSignalNums;
        public Pointer pBuffer;
        public byte[] byRes1 = new byte[3];
        public int dwBufLen;
        public byte[] byRes2 = new byte[64];

        public NET_DVR_INPUT_SIGNAL_LIST() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwInputSignalNums", "pBuffer", "byRes1", "dwBufLen", "byRes2");
        }
    }

    public static class NET_DVR_IPADDR extends Structure {
        public byte[] sIpV4 = new byte[16];
        public byte[] byRes = new byte[128];

        public NET_DVR_IPADDR() {
        }

        public String toString() {
            return "NET_DVR_IPADDR.sIpV4: " + new String(this.sIpV4) + "\n" + "NET_DVR_IPADDR.byRes: " + new String(this.byRes) + "\n";
        }

        protected List getFieldOrder() {
            return Arrays.asList("sIpV4", "byRes");
        }
    }

    public static class NET_DVR_JOINT_SUB_SYSTEM extends Union {
        public HCNetSDKByJNA.NET_DVR_NOAMAL_SUB_SYSTEM struNormalSubSystem = new HCNetSDKByJNA.NET_DVR_NOAMAL_SUB_SYSTEM();
        public HCNetSDKByJNA.NET_DVR_PUBLIC_SUB_SYSTEM struPublicSubSystem = new HCNetSDKByJNA.NET_DVR_PUBLIC_SUB_SYSTEM();
        public byte[] byRes = new byte[20];

        public NET_DVR_JOINT_SUB_SYSTEM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struNormalSubSystem", "struPublicSubSystem", "byRes");
        }
    }

    public static class NET_DVR_LASER_PARAM_CFG extends Structure {
        public byte byControlMode;
        public byte bySensitivity;
        public byte byTriggerMode;
        public byte byBrightness;
        public byte byAngle;
        public byte byLimitBrightness;
        public byte byEnabled;
        public byte byIllumination;
        public byte byLightAngle;
        public byte[] byRes = new byte[7];

        public NET_DVR_LASER_PARAM_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byControlMode", "bySensitivity", "byTriggerMode", "byBrightness", "byAngle", "byLimitBrightness", "byEnabled", "byIllumination", "byLightAngle", "byRes");
        }
    }

    public static class NET_DVR_LED_AREA_COND extends Structure {
        public int dwSize;
        public int dwVideoWallNo;
        public int dwLEDAreaNo;
        public byte[] byRes = new byte[32];

        public NET_DVR_LED_AREA_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwVideoWallNo", "dwLEDAreaNo", "byRes");
        }
    }

    public static class NET_DVR_LED_AREA_INFO extends Structure {
        public int dwSize;
        public int dwLEDAreaNo;
        public HCNetSDKByJNA.NET_DVR_RECTCFG_EX struRect = new HCNetSDKByJNA.NET_DVR_RECTCFG_EX();
        public int[] dwaOutputNo = new int[512];
        public byte byAreaType;
        public byte[] byRes = new byte[31];

        public NET_DVR_LED_AREA_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwLEDAreaNo", "struRect", "dwaOutputNo", "byAreaType", "byRes");
        }
    }

    public static class NET_DVR_LED_AREA_INFO_LIST extends Structure {
        public int dwSize;
        public int dwLEDAreaNum;
        public Pointer lpstruBuffer;
        public int dwBufferSize;
        public byte[] byRes = new byte[32];

        public NET_DVR_LED_AREA_INFO_LIST() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwLEDAreaNum", "lpstruBuffer", "dwBufferSize", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_ABILITY_PARSE_CFG extends Structure {
        public byte byEnableAbilityParse;
        public byte[] byRes = new byte[127];

        public NET_DVR_LOCAL_ABILITY_PARSE_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnableAbilityParse", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_BYTE_ENCODE_CONVERT extends Structure {
        public HCNetSDKByJNA.CHAR_ENCODE_CONVERT fnCharConvertCallBack;
        public byte[] byRes = new byte[256];

        public NET_DVR_LOCAL_BYTE_ENCODE_CONVERT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("fnCharConvertCallBack", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_CFG_VERSION extends Structure {
        public byte byVersion;
        public byte[] byRes = new byte[63];

        public NET_DVR_LOCAL_CFG_VERSION() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byVersion", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_CHECK_DEV extends Structure {
        public int dwCheckOnlineTimeout;
        public int dwCheckOnlineNetFailMax;
        public byte[] byRes = new byte[256];

        public NET_DVR_LOCAL_CHECK_DEV() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwCheckOnlineTimeout", "dwCheckOnlineNetFailMax", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_GENERAL_CFG extends Structure {
        public byte byExceptionCbDirectly;
        public byte byNotSplitRecordFile;
        public byte byResumeUpgradeEnable;
        public byte[] byRes = new byte[5];
        public long i64FileSize;
        public int dwResumeUpgradeTimeout;
        public byte[] byRes1 = new byte[236];

        public NET_DVR_LOCAL_GENERAL_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byExceptionCbDirectly", "byNotSplitRecordFile", "byResumeUpgradeEnable", "byRes", "i64FileSize", "dwResumeUpgradeTimeout", "byRes1");
        }
    }

    public static class NET_DVR_LOCAL_LOG_CFG extends Structure {
        public short wSDKLogNum;
        public byte[] byRes = new byte[254];

        public NET_DVR_LOCAL_LOG_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wSDKLogNum", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_MEM_POOL_CFG extends Structure {
        public int dwAlarmMaxBlockNum;
        public int dwAlarmReleaseInterval;
        public int dwObjectReleaseInterval;
        public byte[] byRes = new byte[508];

        public NET_DVR_LOCAL_MEM_POOL_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwAlarmMaxBlockNum", "dwAlarmReleaseInterval", "dwObjectReleaseInterval", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_MODULE_RECV_TIMEOUT_CFG extends Structure {
        public int dwPreviewTime;
        public int dwAlarmTime;
        public int dwVodTime;
        public int dwElse;
        public byte[] byRes = new byte[512];

        public NET_DVR_LOCAL_MODULE_RECV_TIMEOUT_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwPreviewTime", "dwAlarmTime", "dwVodTime", "dwElse", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_PROTECT_KEY_CFG extends Structure {
        public byte[] byProtectKey = new byte[128];
        public byte[] byRes = new byte[128];

        public NET_DVR_LOCAL_PROTECT_KEY_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byProtectKey", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_PTZ_CFG extends Structure {
        public byte byWithoutRecv;
        public byte[] byRes = new byte[63];

        public NET_DVR_LOCAL_PTZ_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byWithoutRecv", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_SECURITY extends Structure {
        public byte bySecurityLevel;
        public byte[] byRes = new byte[255];

        public NET_DVR_LOCAL_SECURITY() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("bySecurityLevel", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_STREAM_CALLBACK_CFG extends Structure {
        public byte byPlayBackEndFlag;
        public byte[] byRes = new byte[255];

        public NET_DVR_LOCAL_STREAM_CALLBACK_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byPlayBackEndFlag", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_TALK_MODE_CFG extends Structure {
        public byte byTalkMode;
        public byte[] byRes = new byte[127];

        public NET_DVR_LOCAL_TALK_MODE_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byTalkMode", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_TCP_PORT_BIND_CFG extends Structure {
        public short wLocalBindTcpMinPort;
        public short wLocalBindTcpMaxPort;
        public byte[] byRes = new byte[60];

        public NET_DVR_LOCAL_TCP_PORT_BIND_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wLocalBindTcpMinPort", "wLocalBindTcpMaxPort", "byRes");
        }
    }

    public static class NET_DVR_LOCAL_UDP_PORT_BIND_CFG extends Structure {
        public short wLocalBindUdpMinPort;
        public short wLocalBindUdpMaxPort;
        public byte[] byRes = new byte[60];

        public NET_DVR_LOCAL_UDP_PORT_BIND_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wLocalBindUdpMinPort", "wLocalBindUdpMaxPort", "byRes");
        }
    }

    public static class NET_DVR_LOITERING_DETECTION extends Structure {
        public int dwSize;
        public byte byEnabled;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_LOITERING_REGION[] struRegion = (HCNetSDKByJNA.NET_DVR_LOITERING_REGION[])(new HCNetSDKByJNA.NET_DVR_LOITERING_REGION()).toArray(8);
        public byte[] byRes2 = new byte[128];

        public NET_DVR_LOITERING_DETECTION() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnabled", "byRes1", "struRegion", "byRes2");
        }
    }

    public static class NET_DVR_LOITERING_REGION extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public byte bySensitivity;
        public byte byTimeThreshold;
        public byte[] byRes = new byte[62];

        public NET_DVR_LOITERING_REGION() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "bySensitivity", "byTimeThreshold", "byRes");
        }
    }

    public static class NET_DVR_MANUALSNAP extends Structure {
        public byte byOSDEnable;
        public byte byLaneNo;
        public byte byChannel;
        public byte[] byRes = new byte[21];

        public NET_DVR_MANUALSNAP() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byOSDEnable", "byLaneNo", "byChannel", "byRes");
        }
    }

    public static class NET_DVR_MATRIX_ABILITY extends Structure {
        public int dwSize;
        public byte byDecNums;
        public byte byStartChan;
        public byte byVGANums;
        public byte byBNCNums;
        public byte[][] byVGAWindowMode = new byte[8][12];
        public byte[] byBNCWindowMode = new byte[4];
        public byte byDspNums;
        public byte byHDMINums;
        public byte byDVINums;
        public byte[] byRes1 = new byte[13];
        public byte[] bySupportResolution = new byte[64];
        public byte[][] byHDMIWindowMode = new byte[4][8];
        public byte[][] byDVIWindowMode = new byte[4][8];
        public byte[] byRes2 = new byte[24];

        public NET_DVR_MATRIX_ABILITY() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byDecNums", "byStartChan", "byVGANums", "byBNCNums", "byVGAWindowMode", "byBNCWindowMode", "byDspNums", "byHDMINums", "byDVINums", "byRes1", "bySupportResolution", "byHDMIWindowMode", "byDVIWindowMode", "byRes2");
        }
    }

    public static class NET_DVR_MATRIX_DECCHAN_CONTROL extends Structure {
        public int dwSize;
        public byte byDecChanScaleStatus;
        public byte byDecodeDelay;
        public byte byEnableSpartan;
        public byte byLowLight;
        public byte byNoiseReduction;
        public byte byDefog;
        public byte byEnableVcaDec;
        public byte byEnableAudio;
        public int dwAllCtrlType;
        public byte[] byRes = new byte[56];

        public NET_DVR_MATRIX_DECCHAN_CONTROL() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byDecChanScaleStatus", "byDecodeDelay", "byEnableSpartan", "byLowLight", "byNoiseReduction", "byDefog", "byEnableVcaDec", "byEnableAudio", "dwAllCtrlType", "byRes");
        }
    }

    public static class NET_DVR_MATRIX_PASSIVEMODE extends Structure {
        public short wTransProtol;
        public short wPassivePort;
        public HCNetSDKByJNA.NET_DVR_IPADDR struMcastIP = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public byte byStreamType;
        public byte[] byRes = new byte[7];

        public NET_DVR_MATRIX_PASSIVEMODE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wTransProtol", "wPassivePort", "struMcastIP", "byStreamType", "byRes");
        }
    }

    public static class NET_DVR_MOTIONSCOPE extends Structure {
        public byte[] byMotionScope = new byte[96];

        public NET_DVR_MOTIONSCOPE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMotionScope");
        }
    }

    public static class NET_DVR_MOTION_MODE_PARAM extends Structure {
        public HCNetSDKByJNA.NET_DVR_MOTION_SINGLE_AREA struMotionSingleArea;
        public HCNetSDKByJNA.NET_DVR_MOTION_MULTI_AREA struMotionMultiArea;

        public NET_DVR_MOTION_MODE_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struMotionSingleArea", "struMotionMultiArea");
        }
    }

    public static class NET_DVR_MOTION_MULTI_AREA extends Structure {
        public byte byDayNightCtrl;
        public byte byAllMotionSensitive;
        public byte[] byRes = new byte[2];
        public HCNetSDKByJNA.NET_DVR_SCHEDULE_DAYTIME struScheduleTime;
        public HCNetSDKByJNA.NET_DVR_MOTION_MULTI_AREAPARAM[] struMotionMultiAreaParam = new HCNetSDKByJNA.NET_DVR_MOTION_MULTI_AREAPARAM[24];
        public byte[] byRes1 = new byte[60];

        public NET_DVR_MOTION_MULTI_AREA() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byDayNightCtrl", "byAllMotionSensitive", "byRes", "struScheduleTime", "struMotionMultiAreaParam", "byRes1");
        }
    }

    public static class NET_DVR_MOTION_MULTI_AREAPARAM extends Structure {
        public byte byAreaNo;
        public byte[] byRes = new byte[3];
        public HCNetSDKByJNA.NET_VCA_RECT struRect;
        public HCNetSDKByJNA.NET_DVR_DNMODE struDayNightDisable;
        public HCNetSDKByJNA.NET_DVR_DNMODE struDayModeParam;
        public HCNetSDKByJNA.NET_DVR_DNMODE struNightModeParam;
        public byte[] byRes1 = new byte[8];

        public NET_DVR_MOTION_MULTI_AREAPARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byAreaNo", "byRes", "struRect", "struDayNightDisable", "struDayModeParam", "struNightModeParam", "byRes1");
        }
    }

    public static class NET_DVR_MOTION_SINGLE_AREA extends Structure {
        public HCNetSDKByJNA.NET_DVR_MOTIONSCOPE[] byMotionScope = new HCNetSDKByJNA.NET_DVR_MOTIONSCOPE[64];
        public byte byMotionSensitive;
        public byte[] byRes = new byte[3];

        public NET_DVR_MOTION_SINGLE_AREA() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMotionScope", "byMotionSensitive", "byRes");
        }
    }

    public static class NET_DVR_MOTION_V40 extends Structure {
        public HCNetSDKByJNA.NET_DVR_MOTION_MODE_PARAM struMotionMode;
        public byte byEnableHandleMotion;
        public byte byEnableDisplay;
        public byte byConfigurationMode;
        public byte byKeyingEnable;
        public int dwHandleType;
        public int dwMaxRelAlarmOutChanNum;
        public int[] dwRelAlarmOut = new int[4128];
        public HCNetSDKByJNA.NET_DVR_SCHEDTIMEWEEK[] struAlarmTime = new HCNetSDKByJNA.NET_DVR_SCHEDTIMEWEEK[7];
        public int dwMaxRecordChanNum;
        public int[] dwRelRecordChan = new int[512];
        public byte byDiscardFalseAlarm;
        public byte[] byRes = new byte[127];

        public NET_DVR_MOTION_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struMotionMode", "byEnableHandleMotion", "byEnableDisplay", "byConfigurationMode", "byKeyingEnable", "dwHandleType", "dwMaxRelAlarmOutChanNum", "dwRelAlarmOut", "struAlarmTime", "dwMaxRecordChanNum", "dwRelRecordChan", "byDiscardFalseAlarm", "byRes");
        }
    }

    public static class NET_DVR_MULTI_ALARMIN_COND extends Structure {
        public int dwSize;
        public int[] iZoneNo = new int[64];
        public byte[] byRes = new byte[256];

        public NET_DVR_MULTI_ALARMIN_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "iZoneNo", "byRes");
        }
    }

    public static class NET_DVR_MULTI_STREAM_COMPRESSIONCFG extends Structure {
        public int dwSize;
        public int dwStreamType;
        public HCNetSDKByJNA.NET_DVR_COMPRESSION_INFO_V30 struStreamPara = new HCNetSDKByJNA.NET_DVR_COMPRESSION_INFO_V30();
        public byte[] byRes = new byte[80];

        public NET_DVR_MULTI_STREAM_COMPRESSIONCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwStreamType", "struStreamPara", "byRes");
        }
    }

    public static class NET_DVR_MULTI_STREAM_COMPRESSIONCFG_COND extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_STREAM_INFO struStreamInfo = new HCNetSDKByJNA.NET_DVR_STREAM_INFO();
        public int dwStreamType;
        public byte[] byRes = new byte[32];

        public NET_DVR_MULTI_STREAM_COMPRESSIONCFG_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struStreamInfo", "dwStreamType", "byRes");
        }
    }

    public static class NET_DVR_NETCFG_V50 extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_ETHERNET_V30[] struEtherNet = new HCNetSDKByJNA.NET_DVR_ETHERNET_V30[2];
        public HCNetSDKByJNA.NET_DVR_IPADDR[] struRes1 = new HCNetSDKByJNA.NET_DVR_IPADDR[2];
        public HCNetSDKByJNA.NET_DVR_IPADDR struAlarmHostIpAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public byte[] byRes2 = new byte[4];
        public short wAlarmHostIpPort;
        public byte byUseDhcp;
        public byte byIPv6Mode;
        public HCNetSDKByJNA.NET_DVR_IPADDR struDnsServer1IpAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public HCNetSDKByJNA.NET_DVR_IPADDR struDnsServer2IpAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public byte[] byIpResolver = new byte[64];
        public short wIpResolverPort;
        public short wHttpPortNo;
        public HCNetSDKByJNA.NET_DVR_IPADDR struMulticastIpAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public HCNetSDKByJNA.NET_DVR_IPADDR struGatewayIpAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public HCNetSDKByJNA.NET_DVR_PPPOECFG struPPPoE = new HCNetSDKByJNA.NET_DVR_PPPOECFG();
        public byte byEnablePrivateMulticastDiscovery;
        public byte byEnableOnvifMulticastDiscovery;
        public short wAlarmHost2IpPort;
        public HCNetSDKByJNA.NET_DVR_IPADDR struAlarmHost2IpAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public byte byEnableDNS;
        public byte byAlarmOverTLS;
        public byte[] byRes = new byte[598];

        public NET_DVR_NETCFG_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struEtherNet", "struRes1", "struAlarmHostIpAddr", "byRes2", "wAlarmHostIpPort", "byUseDhcp", "byIPv6Mode", "struDnsServer1IpAddr", "struDnsServer2IpAddr", "byIpResolver", "wIpResolverPort", "wHttpPortNo", "struMulticastIpAddr", "struGatewayIpAddr", "struPPPoE", "byEnablePrivateMulticastDiscovery", "byEnableOnvifMulticastDiscovery", "wAlarmHost2IpPort", "struAlarmHost2IpAddr", "byEnableDNS", "byAlarmOverTLS", "byRes");
        }
    }

    public static class NET_DVR_NOAMAL_SUB_SYSTEM extends Structure {
        public int dwBeJoinedSubSystem;
        public byte[] byRes = new byte[16];

        public NET_DVR_NOAMAL_SUB_SYSTEM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwBeJoinedSubSystem", "byRes");
        }
    }

    public static class NET_DVR_NOISEREMOVE extends Structure {
        public byte byDigitalNoiseRemoveEnable;
        public byte byDigitalNoiseRemoveLevel;
        public byte bySpectralLevel;
        public byte byTemporalLevel;
        public byte byDigitalNoiseRemove2DEnable;
        public byte byDigitalNoiseRemove2DLevel;
        public byte[] byRes = new byte[2];

        public NET_DVR_NOISEREMOVE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byDigitalNoiseRemoveEnable", "byDigitalNoiseRemoveLevel", "bySpectralLevel", "byTemporalLevel", "byDigitalNoiseRemove2DEnable", "byDigitalNoiseRemove2DLevel", "byRes");
        }
    }

    public static class NET_DVR_OPEN_EZVIZ_USER_LOGIN_INFO extends Structure {
        public byte[] sEzvizServerAddress = new byte[129];
        public byte[] byRes1 = new byte[3];
        public short wPort;
        public byte[] byRes2 = new byte[2];
        public byte[] sUrl = new byte[64];
        public byte[] sAccessToken = new byte[128];
        public byte[] sDeviceID = new byte[32];
        public byte[] sClientType = new byte[32];
        public byte[] sFeatureCode = new byte[64];
        public byte[] sOsVersion = new byte[32];
        public byte[] sNetType = new byte[32];
        public byte[] sSdkVersion = new byte[32];
        public byte[] sAppID = new byte[64];
        public byte[] byRes3 = new byte[512];

        public NET_DVR_OPEN_EZVIZ_USER_LOGIN_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("sEzvizServerAddress", "byRes1", "wPort", "byRes2", "sUrl", "sAccessToken", "sDeviceID", "sClientType", "sFeatureCode", "sOsVersion", "sNetType", "sSdkVersion", "sAppID", "byRes3");
        }
    }

    public static class NET_DVR_OPTICAL_DEHAZE extends Structure {
        public byte byEnable;
        public byte[] byRes = new byte[7];

        public NET_DVR_OPTICAL_DEHAZE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byRes");
        }
    }

    public static class NET_DVR_PASSNUM_INFO_ALARM extends Structure {
        public int dwSize;
        public int dwAccessChannel;
        public HCNetSDKByJNA.NET_DVR_TIME_V30 struSwipeTime = new HCNetSDKByJNA.NET_DVR_TIME_V30();
        public byte[] byNetUser = new byte[16];
        public HCNetSDKByJNA.NET_DVR_IPADDR struRemoteHostAddr = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public int dwEntryTimes;
        public int dwExitTimes;
        public int dwTotalTimes;
        public byte[] byRes = new byte[300];

        public NET_DVR_PASSNUM_INFO_ALARM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwAccessChannel", "struSwipeTime", "byNetUser", "struRemoteHostAddr", "dwEntryTimes", "dwExitTimes", "dwTotalTimes", "byRes");
        }
    }

    public static class NET_DVR_PDC_ALRAM_INFO extends Structure {
        public int dwSize;
        public byte byMode;
        public byte byChannel;
        public byte bySmart;
        public byte byRes1;
        public HCNetSDKByJNA.NET_VCA_DEV_INFO struDevInfo = new HCNetSDKByJNA.NET_VCA_DEV_INFO();
        public HCNetSDKByJNA.unionStartModeParam unionStartModeParam = new HCNetSDKByJNA.unionStartModeParam();
        public int dwLeaveNum;
        public int dwEnterNum;
        public byte byBrokenNetHttp;
        public byte byRes3;
        public short wDevInfoIvmsChannelEx;
        public int dwPassingNum;
        public byte[] byRes = new byte[32];

        public NET_DVR_PDC_ALRAM_INFO(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byMode", "byChannel", "bySmart", "byRes1", "struDevInfo", "unionStartModeParam", "dwLeaveNum", "dwEnterNum", "byBrokenNetHttp", "byRes3", "wDevInfoIvmsChannelEx", "dwPassingNum", "byRes");
        }
    }

    public static class NET_DVR_PICCFG_V40 extends Structure {
        public int dwSize;
        public byte[] sChanName = new byte[32];
        public int dwVideoFormat;
        public HCNetSDKByJNA.NET_DVR_VICOLOR struViColor;
        public int dwShowChanName;
        public short wShowNameTopLeftX;
        public short wShowNameTopLeftY;
        public int dwEnableHide;
        public HCNetSDKByJNA.NET_DVR_SHELTER[] struShelter = new HCNetSDKByJNA.NET_DVR_SHELTER[4];
        public int dwShowOsd;
        public short wOSDTopLeftX;
        public short wOSDTopLeftY;
        public byte byOSDType;
        public byte byDispWeek;
        public byte byOSDAttrib;
        public byte byHourOSDType;
        public byte byFontSize;
        public byte byOSDColorType;
        public byte byAlignment;
        public byte byOSDMilliSecondEnable;
        public HCNetSDKByJNA.NET_DVR_VILOST_V40 struVILost;
        public HCNetSDKByJNA.NET_DVR_VILOST_V40 struAULost;
        public HCNetSDKByJNA.NET_DVR_MOTION_V40 struMotion;
        public HCNetSDKByJNA.NET_DVR_HIDEALARM_V40 struHideAlarm;
        public HCNetSDKByJNA.NET_DVR_RGB_COLOR struOsdColor;
        public int dwBoundary;
        public HCNetSDKByJNA.NET_DVR_RGB_COLOR struOsdBkColor;
        public byte byOSDBkColorMode;
        public byte[] byRes = new byte[115];

        public NET_DVR_PICCFG_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "sChanName", "dwVideoFormat", "struViColor", "dwShowChanName", "wShowNameTopLeftX", "wShowNameTopLeftY", "dwEnableHide", "struShelter", "dwShowOsd", "wOSDTopLeftX", "wOSDTopLeftY", "byOSDType", "byDispWeek", "byOSDAttrib", "byHourOSDType", "byFontSize", "byOSDColorType", "byAlignment", "byOSDMilliSecondEnable", "struVILost", "struAULost", "struMotion", "struHideAlarm", "struOsdColor", "dwBoundary", "struOsdBkColor", "byOSDBkColorMode", "byRes");
        }
    }

    public static class NET_DVR_PIC_EXTRA_INFO_UNION extends Union {
        public byte[] byUnionLen = new byte[544];
        public HCNetSDKByJNA.NET_DVR_FACE_EXTRA_INFO struFaceExtraInfo = new HCNetSDKByJNA.NET_DVR_FACE_EXTRA_INFO();

        public NET_DVR_PIC_EXTRA_INFO_UNION() {
        }
    }

    public static class NET_DVR_PIC_PARAM extends Structure {
        public Pointer pDVRFileName;
        public Pointer pSavedFileBuf;
        public int dwBufLen;
        public Pointer lpdwRetLen;
        public HCNetSDKByJNA.NET_DVR_ADDRESS struAddr = new HCNetSDKByJNA.NET_DVR_ADDRESS();
        public byte[] byRes = new byte[256];

        public NET_DVR_PIC_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("pDVRFileName", "pSavedFileBuf", "dwBufLen", "lpdwRetLen", "struAddr", "byRes");
        }
    }

    public static class NET_DVR_PIRIS_PARAM extends Structure {
        public byte byMode;
        public byte byPIrisAperture;
        public byte[] byRes = new byte[6];

        public NET_DVR_PIRIS_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byPIrisAperture", "byRes");
        }
    }

    public static class NET_DVR_PLAN_TEMPLATE extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public byte[] byTemplateName = new byte[32];
        public int dwWeekPlanNo;
        public int[] dwHolidayGroupNo = new int[16];
        public byte[] byRes2 = new byte[32];

        public NET_DVR_PLAN_TEMPLATE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byRes1", "byTemplateName", "dwWeekPlanNo", "dwHolidayGroupNo", "byRes2");
        }
    }

    public static class NET_DVR_PLAN_TEMPLATE_COND extends Structure {
        public int dwSize;
        public int dwPlanTemplateNumber;
        public short wLocalControllerID;
        public byte[] byRes = new byte[106];

        public NET_DVR_PLAN_TEMPLATE_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwPlanTemplateNumber", "wLocalControllerID", "byRes");
        }
    }

    public static class NET_DVR_PLATE_INFO extends Structure {
        public byte byPlateType;
        public byte byColor;
        public byte byBright;
        public byte byLicenseLen;
        public byte byEntireBelieve;
        public byte byRegion;
        public byte byCountry;
        public byte[] byRes = new byte[33];
        public HCNetSDKByJNA.NET_VCA_RECT struPlateRect = new HCNetSDKByJNA.NET_VCA_RECT();
        public byte[] sLicense = new byte[16];
        public byte[] byBelieve = new byte[16];

        public NET_DVR_PLATE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byPlateType", "byColor", "byBright", "byLicenseLen", "byEntireBelieve", "byRegion", "byCountry", "byRes", "struPlateRect", "sLicense", "byBelieve");
        }
    }

    public static class NET_DVR_PLATE_RESULT extends Structure {
        public int dwSize;
        public byte byResultType;
        public byte byChanIndex;
        public short wAlarmRecordID;
        public int dwRelativeTime;
        public byte[] byAbsTime = new byte[32];
        public int dwPicLen;
        public int dwPicPlateLen;
        public int dwVideoLen;
        public byte byTrafficLight;
        public byte byPicNum;
        public byte byDriveChan;
        public byte byVehicleType;
        public int dwBinPicLen;
        public int dwCarPicLen;
        public int dwFarCarPicLen;
        public ByteByReference pBuffer3;
        public ByteByReference pBuffer4;
        public ByteByReference pBuffer5;
        public byte byRelaLaneDirectionType;
        public byte byCarDirectionType;
        public byte[] byRes3 = new byte[6];
        public HCNetSDKByJNA.NET_DVR_PLATE_INFO struPlateInfo = new HCNetSDKByJNA.NET_DVR_PLATE_INFO();
        public HCNetSDKByJNA.NET_DVR_VEHICLE_INFO struVehicleInfo = new HCNetSDKByJNA.NET_DVR_VEHICLE_INFO();
        public ByteByReference pBuffer1;
        public ByteByReference pBuffer2;

        public NET_DVR_PLATE_RESULT() {
        }

        public NET_DVR_PLATE_RESULT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byResultType", "byChanIndex", "wAlarmRecordID", "dwRelativeTime", "byAbsTime", "dwPicLen", "dwPicPlateLen", "dwVideoLen", "byTrafficLight", "byPicNum", "byDriveChan", "byVehicleType", "dwBinPicLen", "dwCarPicLen", "dwFarCarPicLen", "pBuffer3", "pBuffer4", "pBuffer5", "byRelaLaneDirectionType", "byCarDirectionType", "byRes3", "struPlateInfo", "struVehicleInfo", "pBuffer1", "pBuffer2");
        }
    }

    public static class NET_DVR_PPPOECFG extends Structure {
        public int dwPPPOE;
        public byte[] sPPPoEUser = new byte[32];
        public byte[] sPPPoEPassword = new byte[16];
        public HCNetSDKByJNA.NET_DVR_IPADDR struDVRIP = new HCNetSDKByJNA.NET_DVR_IPADDR();

        public NET_DVR_PPPOECFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwPPPOE", "sPPPoEUser", "sPPPoEPassword", "struDVRIP");
        }
    }

    public static class NET_DVR_PTZPOS extends Structure {
        public short wAction;
        public short wPanPos;
        public short wTiltPos;
        public short wZoomPos;

        public NET_DVR_PTZPOS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wAction", "wPanPos", "wTiltPos", "wZoomPos");
        }
    }

    public static class NET_DVR_PUBLIC_SUB_SYSTEM extends Structure {
        public int dwJointSubSystem;
        public byte[] byRes = new byte[16];

        public NET_DVR_PUBLIC_SUB_SYSTEM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwJointSubSystem", "byRes");
        }
    }

    public static class NET_DVR_PU_STREAM_CFG extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_STREAM_MEDIA_SERVER_CFG struStreamMediaSvrCfg = new HCNetSDKByJNA.NET_DVR_STREAM_MEDIA_SERVER_CFG();
        public HCNetSDKByJNA.NET_DVR_DEV_CHAN_INFO struDevChanInfo = new HCNetSDKByJNA.NET_DVR_DEV_CHAN_INFO();

        public NET_DVR_PU_STREAM_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struStreamMediaSvrCfg", "struDevChanInfo");
        }
    }

    public static class NET_DVR_RECTCFG_EX extends Structure {
        public int dwXCoordinate;
        public int dwYCoordinate;
        public int dwWidth;
        public int dwHeight;
        public byte[] byRes = new byte[4];

        public NET_DVR_RECTCFG_EX() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwXCoordinate", "dwYCoordinate", "dwWidth", "dwHeight", "byRes");
        }
    }

    public static class NET_DVR_RGB_COLOR extends Structure {
        public byte byRed;
        public byte byGreen;
        public byte byBlue;
        public byte byRes;

        public NET_DVR_RGB_COLOR() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRed", "byGreen", "byBlue", "byRes");
        }
    }

    public static class NET_DVR_RTSP_PARAMS_CFG extends Structure {
        public int dwMaxBuffRoomNum;
        public byte byUseSort;
        public byte[] byRes = new byte[123];

        public NET_DVR_RTSP_PARAMS_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwMaxBuffRoomNum", "byUseSort", "byRes");
        }
    }

    public static class NET_DVR_SCENE_INFO extends Structure {
        public int dwSceneID;
        public byte[] bySceneName = new byte[32];
        public byte byDirection;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_PTZPOS struPtzPos = new HCNetSDKByJNA.NET_DVR_PTZPOS();
        public byte[] byRes2 = new byte[64];

        public NET_DVR_SCENE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSceneID", "bySceneName", "byDirection", "byRes1", "struPtzPos", "byRes2");
        }
    }

    public static class NET_DVR_SCHEDTIME extends Structure {
        public byte byStartHour;
        public byte byStartMin;
        public byte byStopHour;
        public byte byStopMin;

        public NET_DVR_SCHEDTIME() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byStartHour", "byStartMin", "byStopHour", "byStopMin");
        }
    }

    public static class NET_DVR_SCHEDTIMEWEEK extends Structure {
        public HCNetSDKByJNA.NET_DVR_SCHEDTIME[] struAlarmTime = new HCNetSDKByJNA.NET_DVR_SCHEDTIME[8];

        public NET_DVR_SCHEDTIMEWEEK() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struAlarmTime");
        }
    }

    public static class NET_DVR_SCHEDTIME_DAYS extends Structure {
        public HCNetSDKByJNA.NET_DVR_SCHEDTIME[] struSchedTimeDays = (HCNetSDKByJNA.NET_DVR_SCHEDTIME[])(new HCNetSDKByJNA.NET_DVR_SCHEDTIME()).toArray(7);

        public NET_DVR_SCHEDTIME_DAYS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struSchedTimeDays");
        }
    }

    public static class NET_DVR_SCHEDULE_DAYTIME extends Structure {
        public HCNetSDKByJNA.NET_DVR_DAYTIME struStartTime;
        public HCNetSDKByJNA.NET_DVR_DAYTIME struStopTime;

        public NET_DVR_SCHEDULE_DAYTIME() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struStartTime", "struStopTime");
        }
    }

    public static class NET_DVR_SEND_PARAM_IN extends Structure {
        public Pointer pSendData;
        public int dwSendDataLen;
        public HCNetSDKByJNA.NET_DVR_TIME_V30 struTime;
        public byte byPicType;
        public byte[] byRes1 = new byte[3];
        public int dwPicManageNo;
        public byte[] sPicName = new byte[32];
        public int dwPicDisplayTime;
        public Pointer pSendAppendData;
        public int dwSendAppendDataLen;
        public byte[] byRes = new byte[192];

        public NET_DVR_SEND_PARAM_IN() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("pSendData", "dwSendDataLen", "struTime", "byPicType", "byRes1", "dwPicManageNo", "sPicName", "dwPicDisplayTime", "pSendAppendData", "dwSendAppendDataLen", "byRes");
        }
    }

    public static class NET_DVR_SETUPALARM_PARAM extends Structure {
        public int dwSize;
        public byte byLevel;
        public byte byAlarmInfoType;
        public byte byRetAlarmTypeV40;
        public byte byRetDevInfoVersion;
        public byte byRetVQDAlarmType;
        public byte byFaceAlarmDetection;
        public byte bySupport;
        public byte byBrokenNetHttp;
        public short wTaskNo;
        public byte[] byRes1 = new byte[6];

        public NET_DVR_SETUPALARM_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byLevel", "byAlarmInfoType", "byRetAlarmTypeV40", "byRetDevInfoVersion", "byRetVQDAlarmType", "byFaceAlarmDetection", "bySupport", "byBrokenNetHttp", "wTaskNo", "byRes1");
        }
    }

    public static class NET_DVR_SHELTER extends Structure {
        public short wHideAreaTopLeftX;
        public short wHideAreaTopLeftY;
        public short wHideAreaWidth;
        public short wHideAreaHeight;

        public NET_DVR_SHELTER() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wHideAreaTopLeftX", "wHideAreaTopLeftY", "wHideAreaWidth", "wHideAreaHeight");
        }
    }

    public static class NET_DVR_SHOWSTRINGINFO extends Structure {
        public short wShowString;
        public short wStringSize;
        public short wShowStringTopLeftX;
        public short wShowStringTopLeftY;
        public byte[] sString = new byte[44];

        public NET_DVR_SHOWSTRINGINFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wShowString", "wStringSize", "wShowStringTopLeftX", "wShowStringTopLeftY", "sString");
        }
    }

    public static class NET_DVR_SHOWSTRING_V30 extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_DVR_SHOWSTRINGINFO[] struStringInfo = (HCNetSDKByJNA.NET_DVR_SHOWSTRINGINFO[])(new HCNetSDKByJNA.NET_DVR_SHOWSTRINGINFO()).toArray(8);

        public NET_DVR_SHOWSTRING_V30() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struStringInfo");
        }
    }

    public static class NET_DVR_SIMPLE_DAYTIME extends Structure {
        public byte byHour;
        public byte byMinute;
        public byte bySecond;
        public byte byRes;

        public NET_DVR_SIMPLE_DAYTIME() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byHour", "byMinute", "bySecond", "byRes");
        }
    }

    public static class NET_DVR_SIMXML_LOGIN extends Structure {
        public byte byLoginWithSimXml;
        public byte[] byRes = new byte[127];

        public NET_DVR_SIMXML_LOGIN() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byLoginWithSimXml", "byRes");
        }
    }

    public static class NET_DVR_SINGLE_ALARMIN_PARAM extends Structure {
        public int dwSize;
        public short wZoneNo;
        public byte byJointSubSystem;
        public byte byType;
        public byte[] byName = new byte[32];
        public short wDetectorType;
        public short wInDelay;
        public short wOutDelay;
        public byte byAlarmType;
        public byte byZoneSignalType;
        public byte[] byDetectorSerialNo = new byte[9];
        public byte byDisableDetectorTypeCfg;
        public byte[] byRes2 = new byte[118];

        public NET_DVR_SINGLE_ALARMIN_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "wZoneNo", "byJointSubSystem", "byType", "byName", "wDetectorType", "wInDelay", "wOutDelay", "byAlarmType", "byZoneSignalType", "byDetectorSerialNo", "byDisableDetectorTypeCfg", "byRes2");
        }
    }

    public static class NET_DVR_SINGLE_ALARMIN_PARAM_V50 extends Structure {
        public int dwSize;
        public short wZoneNo;
        public byte byJointSubSystem;
        public byte byType;
        public byte[] byName = new byte[32];
        public short wDetectorType;
        public short wInDelay;
        public short wOutDelay;
        public byte byAlarmType;
        public byte byZoneSignalType;
        public byte[] byDetectorSerialNo = new byte[9];
        public byte byDisableDetectorTypeCfg;
        public byte byTimeOutRange;
        public byte byDetectorSignalIntensity;
        public short wTimeOut;
        public byte byTimeOutMethod;
        public byte byAssociateFlashLamp;
        public byte byStayAwayEnabled;
        public byte bySilentModeEnabled;
        public byte[] byRes3 = new byte[2];
        public byte[] byAssociateAlarmOut = new byte[512];
        public byte[] byRes2 = new byte[128];

        public NET_DVR_SINGLE_ALARMIN_PARAM_V50() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "wZoneNo", "byJointSubSystem", "byType", "byName", "wDetectorType", "wInDelay", "wOutDelay", "byAlarmType", "byZoneSignalType", "byDetectorSerialNo", "byDisableDetectorTypeCfg", "byTimeOutRange", "byDetectorSignalIntensity", "wTimeOut", "byTimeOutMethod", "byAssociateFlashLamp", "byStayAwayEnabled", "bySilentModeEnabled", "byRes3", "byAssociateAlarmOut", "byRes2");
        }
    }

    public static class NET_DVR_SINGLE_PLAN_SEGMENT extends Structure {
        public byte byEnable;
        public byte byDoorStatus;
        public byte byVerifyMode;
        public byte[] byRes = new byte[5];
        public HCNetSDKByJNA.NET_DVR_TIME_SEGMENT struTimeSegment = new HCNetSDKByJNA.NET_DVR_TIME_SEGMENT();

        public NET_DVR_SINGLE_PLAN_SEGMENT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byDoorStatus", "byVerifyMode", "byRes", "struTimeSegment");
        }
    }

    public static class NET_DVR_SMARTIR_PARAM extends Structure {
        public byte byMode;
        public byte byIRDistance;
        public byte byShortIRDistance;
        public byte byLongIRDistance;

        public NET_DVR_SMARTIR_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byIRDistance", "byShortIRDistance", "byLongIRDistance");
        }
    }

    public static class NET_DVR_SMART_REGION_COND extends Structure {
        public int dwSize;
        public int dwChannel;
        public int dwRegion;

        public NET_DVR_SMART_REGION_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannel", "dwRegion");
        }
    }

    public static class NET_DVR_SNAP_CAMERAPARAMCFG extends Structure {
        public byte byWDRMode;
        public byte byWDRType;
        public byte byWDRLevel;
        public byte byRes1;
        public HCNetSDKByJNA.NET_DVR_TIME_EX struStartTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public HCNetSDKByJNA.NET_DVR_TIME_EX struEndTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public byte byDayNightBrightness;
        public byte byMCEEnabled;
        public byte byMCELevel;
        public byte byAutoContrastEnabled;
        public byte byAutoContrastLevel;
        public byte byLSEDetailEnabled;
        public byte byLSEDetailLevel;
        public byte byLPDEEnabled;
        public byte byLPDELevel;
        public byte byLseEnabled;
        public byte byLseLevel;
        public byte byLSEHaloLevel;
        public byte byLseType;
        public byte[] byRes2 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_TIME_EX struLSEStartTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public HCNetSDKByJNA.NET_DVR_TIME_EX struLSEEndTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public byte byLightLevel;
        public byte byPlateContrastLevel;
        public byte byPlateSaturationLevel;
        public byte[] byRes = new byte[9];

        public NET_DVR_SNAP_CAMERAPARAMCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byWDRMode", "byWDRType", "byWDRLevel", "byRes1", "struStartTime", "struEndTime", "byDayNightBrightness", "byMCEEnabled", "byMCELevel", "byAutoContrastEnabled", "byAutoContrastLevel", "byLSEDetailEnabled", "byLSEDetailLevel", "byLPDEEnabled", "byLPDELevel", "byLseEnabled", "byLseLevel", "byLSEHaloLevel", "byLseType", "byRes2", "struLSEStartTime", "struLSEEndTime", "byLightLevel", "byPlateContrastLevel", "byPlateSaturationLevel", "byRes");
        }
    }

    public static class NET_DVR_SOCKS_PROXYS extends Structure {
        public HCNetSDKByJNA.NET_DVR_SOCKS_PROXY_PARA[] struProxy = new HCNetSDKByJNA.NET_DVR_SOCKS_PROXY_PARA[32];

        public NET_DVR_SOCKS_PROXYS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struProxy");
        }
    }

    public static class NET_DVR_SOCKS_PROXY_PARA extends Structure {
        public byte[] byIP = new byte[129];
        public byte byAuthType;
        public short wPort;
        public byte[] byRes2 = new byte[64];

        public NET_DVR_SOCKS_PROXY_PARA() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byIP", "byAuthType", "wPort", "byRes2");
        }
    }

    public static class NET_DVR_STD_ABILITY extends Structure {
        public Pointer lpCondBuffer;
        public int dwCondSize;
        public Pointer lpOutBuffer;
        public int dwOutSize;
        public Pointer lpStatusBuffer;
        public int dwStatusSize;
        public int dwRetSize;
        public byte[] byRes = new byte[32];

        public NET_DVR_STD_ABILITY() {
        }

        protected List getFieldOrder() {
            return Arrays.asList("lpCondBuffer", "dwCondSize", "lpOutBuffer", "dwOutSize", "lpStatusBuffer", "dwStatusSize", "dwRetSize", "byRes");
        }
    }

    public static class NET_DVR_STD_CONFIG extends Structure {
        public Pointer lpCondBuffer;
        public int dwCondSize;
        public Pointer lpInBuffer;
        public int dwInSize;
        public Pointer lpOutBuffer;
        public int dwOutSize;
        public Pointer lpStatusBuffer;
        public int dwStatusSize;
        public Pointer lpXmlBuffer;
        public int dwXmlSize;
        public byte byDataType;
        public byte[] byRes = new byte[23];

        public NET_DVR_STD_CONFIG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("lpCondBuffer", "dwCondSize", "lpInBuffer", "dwInSize", "lpOutBuffer", "dwOutSize", "lpStatusBuffer", "dwStatusSize", "lpXmlBuffer", "dwXmlSize", "byDataType", "byRes");
        }
    }

    public static class NET_DVR_STREAM_INFO extends Structure {
        public int dwSize;
        public byte[] byID = new byte[32];
        public int dwChannel;
        public byte[] byRes = new byte[32];

        public NET_DVR_STREAM_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byID", "dwChannel", "byRes");
        }
    }

    public static class NET_DVR_STREAM_MEDIA_SERVER_CFG extends Structure {
        public byte byValid;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_IPADDR struDevIP = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public short wDevPort;
        public byte byTransmitType;
        public byte[] byRes2 = new byte[69];

        public NET_DVR_STREAM_MEDIA_SERVER_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byValid", "byRes1", "struDevIP", "wDevPort", "byTransmitType", "byRes2");
        }
    }

    public static class NET_DVR_SUBSYSTEMINFO_V40 extends Structure {
        public byte bySubSystemType;
        public byte byChan;
        public byte byLoginType;
        public byte bySlotNum;
        public byte[] byRes1 = new byte[4];
        public HCNetSDKByJNA.NET_DVR_IPADDR struSubSystemIP;
        public HCNetSDKByJNA.NET_DVR_IPADDR struSubSystemIPMask;
        public HCNetSDKByJNA.NET_DVR_IPADDR struGatewayIpAddr;
        public short wSubSystemPort;
        public byte[] byRes2 = new byte[6];
        public byte[] sUserName = new byte[32];
        public byte[] sPassword = new byte[16];
        public byte[] sDomainName = new byte[64];
        public byte[] sDnsAddress = new byte[64];
        public byte[] sSerialNumber = new byte[48];
        public byte byBelongBoard;
        public byte byInterfaceType;
        public byte byInterfaceNums;
        public byte byInterfaceStartNum;
        public byte[] byDeviceName = new byte[20];
        public byte byAudioChanNums;
        public byte byAudioChanStartNum;
        public byte byAudioChanType;
        public byte[] byRes3 = new byte[33];

        public NET_DVR_SUBSYSTEMINFO_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("bySubSystemType", "byChan", "byLoginType", "bySlotNum", "byRes1", "struSubSystemIP", "struSubSystemIPMask", "struGatewayIpAddr", "wSubSystemPort", "byRes2", "sUserName", "sPassword", "sDomainName", "sDnsAddress", "sSerialNumber", "byBelongBoard", "byInterfaceType", "byInterfaceNums", "byInterfaceStartNum", "byDeviceName", "byAudioChanNums", "byAudioChanStartNum", "byAudioChanType", "byRes3");
        }
    }

    public static class NET_DVR_SUBSYSTEM_BASIC_INFO extends Structure {
        public int dwSize;
        public byte bySubSystemType;
        public byte bySubSystemNo;
        public byte[] byRes1 = new byte[2];
        public int dwChan;
        public HCNetSDKByJNA.NET_DVR_IPADDR struSubSystemIP;
        public HCNetSDKByJNA.NET_DVR_IPADDR struSubSystemIPMask;
        public HCNetSDKByJNA.NET_DVR_IPADDR struGatewayIpAddr;
        public short wSubSystemPort;
        public byte[] byRes2 = new byte[6];
        public byte[] sSerialNumber = new byte[48];
        public byte byBelongBoard;
        public byte[] byRes3 = new byte[3];
        public byte[] byDeviceName = new byte[20];
        public int dwStartChanNo;
        public byte byDevNo;
        public byte[] byRes4 = new byte[63];

        public NET_DVR_SUBSYSTEM_BASIC_INFO(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "bySubSystemType", "bySubSystemNo", "byRes1", "dwChan", "struSubSystemIP", "struSubSystemIPMask", "struGatewayIpAddr", "wSubSystemPort", "byRes2", "sSerialNumber", "byBelongBoard", "byRes3", "byDeviceName", "dwStartChanNo", "byDevNo", "byRes4");
        }
    }

    public static class NET_DVR_SUBSYSTEM_BASIC_INFO_RESPONSE extends Structure {
        public int dwSize;
        public int dwErrorCode;
        public byte byDevNo;
        public byte bySubSystemNo;
        public byte[] byRes = new byte[30];

        public NET_DVR_SUBSYSTEM_BASIC_INFO_RESPONSE(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwErrorCode", "byDevNo", "bySubSystemNo", "byRes");
        }
    }

    public static class NET_DVR_TEMPERATURE_COLOR extends Structure {
        public byte byType;
        public byte[] byRes1 = new byte[3];
        public int iHighTemperature;
        public int iLowTemperature;
        public byte[] byRes = new byte[8];

        public NET_DVR_TEMPERATURE_COLOR() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byType", "byRes1", "iHighTemperature", "iLowTemperature", "byRes");
        }
    }

    public static class NET_DVR_TFS_ALARM extends Structure {
        public int dwSize;
        public int dwRelativeTime;
        public int dwAbsTime;
        public int dwIllegalType;
        public int dwIllegalDuration;
        public byte[] byMonitoringSiteID = new byte[48];
        public byte[] byDeviceID = new byte[48];
        public HCNetSDKByJNA.NET_VCA_DEV_INFO struDevInfo = new HCNetSDKByJNA.NET_VCA_DEV_INFO();
        public HCNetSDKByJNA.NET_DVR_SCENE_INFO struSceneInfo = new HCNetSDKByJNA.NET_DVR_SCENE_INFO();
        public HCNetSDKByJNA.NET_DVR_TIME_EX struBeginRecTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public HCNetSDKByJNA.NET_DVR_TIME_EX struEndRecTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public HCNetSDKByJNA.NET_DVR_AID_INFO struAIDInfo = new HCNetSDKByJNA.NET_DVR_AID_INFO();
        public HCNetSDKByJNA.NET_DVR_PLATE_INFO struPlateInfo = new HCNetSDKByJNA.NET_DVR_PLATE_INFO();
        public HCNetSDKByJNA.NET_DVR_VEHICLE_INFO struVehicleInfo = new HCNetSDKByJNA.NET_DVR_VEHICLE_INFO();
        public int dwPicNum;
        public HCNetSDKByJNA.NET_ITS_PICTURE_INFO[] struPicInfo = (HCNetSDKByJNA.NET_ITS_PICTURE_INFO[])(new HCNetSDKByJNA.NET_ITS_PICTURE_INFO()).toArray(8);
        public byte bySpecificVehicleType;
        public byte byLaneNo;
        public byte[] byRes1 = new byte[2];
        public HCNetSDKByJNA.NET_DVR_TIME_V30 struTime = new HCNetSDKByJNA.NET_DVR_TIME_V30();
        public int dwSerialNo;
        public byte byVehicleAttribute;
        public byte byPilotSafebelt;
        public byte byCopilotSafebelt;
        public byte byPilotSunVisor;
        public byte byCopilotSunVisor;
        public byte byPilotCall;
        public byte[] byRes2 = new byte[2];
        public byte[] byIllegalCode = new byte[48];
        public byte[] byRes = new byte[68];

        public NET_DVR_TFS_ALARM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwRelativeTime", "dwAbsTime", "dwIllegalType", "dwIllegalDuration", "byMonitoringSiteID", "byDeviceID", "struDevInfo", "struSceneInfo", "struBeginRecTime", "struEndRecTime", "struAIDInfo", "struPlateInfo", "struVehicleInfo", "dwPicNum", "struPicInfo", "bySpecificVehicleType", "byLaneNo", "byRes1", "struTime", "dwSerialNo", "byVehicleAttribute", "byPilotSafebelt", "byCopilotSafebelt", "byPilotSunVisor", "byCopilotSunVisor", "byPilotCall", "byRes2", "byIllegalCode", "byRes");
        }
    }

    public static class NET_DVR_THERMAL_PIP extends Structure {
        public int dwsize;
        public byte byEnable;
        public byte byPipMode;
        public byte byOverlapType;
        public byte byTransparency;
        public HCNetSDKByJNA.NET_VCA_POLYGON struPipRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public byte[] byRes = new byte[640];

        public NET_DVR_THERMAL_PIP() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwsize", "byEnable", "byPipMode", "byOverlapType", "byTransparency", "struPipRegion", "byRes");
        }
    }

    public static class NET_DVR_THERMOMETRY_AGC extends Structure {
        public byte byMode;
        public byte[] byRes1 = new byte[3];
        public int iHighTemperature;
        public int iLowTemperature;
        public byte[] byRes = new byte[8];

        public NET_DVR_THERMOMETRY_AGC() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byRes1", "iHighTemperature", "iLowTemperature", "byRes");
        }
    }

    public static class NET_DVR_THERMOMETRY_BASICPARAM extends Structure {
        public int dwsize;
        public byte byEnabled;
        public byte byStreamOverlay;
        public byte byPictureOverlay;
        public byte byThermometryRange;
        public byte byThermometryUnit;
        public byte byThermometryCurve;
        public byte byFireImageModea;
        public byte byShowTempStripEnable;
        public float fEmissivity;
        public byte byDistanceUnit;
        public byte byEnviroHumidity;
        public byte[] byRes2 = new byte[2];
        public HCNetSDKByJNA.NET_DVR_TEMPERATURE_COLOR struTempColor;
        public int iEnviroTemperature;
        public int iCorrectionVolume;
        public byte bySpecialPointThermType;
        public byte byReflectiveEnabled;
        public short wDistance;
        public float fReflectiveTemperature;
        public float fAlert;
        public float fAlarm;
        public float fThermalOpticalTransmittance;
        public float fExternalOpticsWindowCorrection;
        public byte[] byRes = new byte[64];

        public NET_DVR_THERMOMETRY_BASICPARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwsize", "byEnabled", "byStreamOverlay", "byPictureOverlay", "byThermometryRange", "byThermometryUnit", "byThermometryCurve", "byFireImageModea", "byShowTempStripEnable", "fEmissivity", "byDistanceUnit", "byEnviroHumidity", "byRes2", "struTempColor", "iEnviroTemperature", "iCorrectionVolume", "bySpecialPointThermType", "byReflectiveEnabled", "wDistance", "fReflectiveTemperature", "fAlert", "fAlarm", "fThermalOpticalTransmittance", "fExternalOpticsWindowCorrection", "byRes");
        }
    }

    public static class NET_DVR_THERMOMETRY_COND extends Structure {
        public int dwsize;
        public int dwChannel;
        public short wPresetNo;
        public byte[] byRes = new byte[62];

        public NET_DVR_THERMOMETRY_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwsize", "dwChannel", "wPresetNo", "byRes");
        }
    }

    public static class NET_DVR_THERMOMETRY_PRESETINFO extends Structure {
        public int dwsize;
        public short wPresetNo;
        public byte[] byRes = new byte[2];
        public HCNetSDKByJNA.NET_DVR_THERMOMETRY_PRESETINFO_PARAM[] struPresetInfo = new HCNetSDKByJNA.NET_DVR_THERMOMETRY_PRESETINFO_PARAM[40];

        public NET_DVR_THERMOMETRY_PRESETINFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwsize", "wPresetNo", "byRes", "struPresetInfo");
        }
    }

    public static class NET_DVR_THERMOMETRY_PRESETINFO_PARAM extends Structure {
        public byte byEnabled;
        public byte byRuleID;
        public short wDistance;
        public float fEmissivity;
        public byte[] byRes = new byte[3];
        public byte byReflectiveEnabled;
        public float fReflectiveTemperature;
        public byte[] szRuleName = new byte[32];
        public byte[] byRes1 = new byte[63];
        public byte byRuleCalibType;
        public HCNetSDKByJNA.NET_VCA_POINT struPoint;
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion;

        public NET_DVR_THERMOMETRY_PRESETINFO_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnabled", "byRuleID", "wDistance", "fEmissivity", "byRes", "byReflectiveEnabled", "fReflectiveTemperature", "szRuleName", "byRes1", "byRuleCalibType", "struPoint", "struRegion");
        }
    }

    public static class NET_DVR_TIME extends Structure {
        public int dwYear;
        public int dwMonth;
        public int dwDay;
        public int dwHour;
        public int dwMinute;
        public int dwSecond;

        public NET_DVR_TIME() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwYear", "dwMonth", "dwDay", "dwHour", "dwMinute", "dwSecond");
        }
    }

    public static class NET_DVR_TIME_EX extends Structure {
        public short wYear;
        public byte byMonth;
        public byte byDay;
        public byte byHour;
        public byte byMinute;
        public byte bySecond;
        public byte byRes;

        public NET_DVR_TIME_EX() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wYear", "byMonth", "byDay", "byHour", "byMinute", "bySecond", "byRes");
        }
    }

    public static class NET_DVR_TIME_SEGMENT extends Structure {
        public HCNetSDKByJNA.NET_DVR_SIMPLE_DAYTIME struBeginTime = new HCNetSDKByJNA.NET_DVR_SIMPLE_DAYTIME();
        public HCNetSDKByJNA.NET_DVR_SIMPLE_DAYTIME struEndTime = new HCNetSDKByJNA.NET_DVR_SIMPLE_DAYTIME();

        public NET_DVR_TIME_SEGMENT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struBeginTime", "struEndTime");
        }
    }

    public static class NET_DVR_TIME_V30 extends Structure {
        public short wYear;
        public byte byMonth;
        public byte byDay;
        public byte byHour;
        public byte byMinute;
        public byte bySecond;
        public byte byRes;
        public short wMilliSec;
        public byte cTimeDifferenceH;
        public byte cTimeDifferenceM;

        public NET_DVR_TIME_V30() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wYear", "byMonth", "byDay", "byHour", "byMinute", "bySecond", "byRes", "wMilliSec", "cTimeDifferenceH", "cTimeDifferenceM");
        }
    }

    public static class NET_DVR_UPLOAD_FACE_DATA_OUT extends Structure {
        public byte[] szPicID = new byte[256];
        public byte[] byRes = new byte[128];

        public NET_DVR_UPLOAD_FACE_DATA_OUT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("szPicID", "byRes");
        }
    }

    public static class NET_DVR_UPLOAD_FILE_RET extends Structure {
        public byte[] sUrl = new byte[260];
        public byte[] byRes = new byte[260];

        public NET_DVR_UPLOAD_FILE_RET() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("sUrl", "byRes");
        }
    }

    public static class NET_DVR_USER_LOGIN_INFO extends Structure {
        public byte[] sDeviceAddress = new byte[129];
        public byte byUseTransport;
        public short wPort;
        public byte[] sUserName = new byte[64];
        public byte[] sPassword = new byte[64];
        public HCNetSDKByJNA.FLoginResultCallBack cbLoginResult;
        public Pointer pUser;
        public boolean bUseAsynLogin;
        public byte byProxyType;
        public byte byUseUTCTime;
        public byte byLoginMode;
        public byte byHttps;
        public int iProxyID;
        public byte[] byRes3 = new byte[120];

        public NET_DVR_USER_LOGIN_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("sDeviceAddress", "byUseTransport", "wPort", "sUserName", "sPassword", "cbLoginResult", "pUser", "bUseAsynLogin", "byProxyType", "byUseUTCTime", "byLoginMode", "byHttps", "iProxyID", "byRes3");
        }
    }

    public static class NET_DVR_VALID_PERIOD_CFG extends Structure {
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_TIME_EX struBeginTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public HCNetSDKByJNA.NET_DVR_TIME_EX struEndTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public byte[] byRes2 = new byte[32];

        public NET_DVR_VALID_PERIOD_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byRes1", "struBeginTime", "struEndTime", "byRes2");
        }
    }

    public static class NET_DVR_VEHICLE_CONTROL_ALARM extends Structure {
        public int dwSize;
        public byte byListType;
        public byte byPlateType;
        public byte byPlateColor;
        public byte byRes1;
        public byte[] sLicense = new byte[16];
        public byte[] sCardNo = new byte[48];
        public HCNetSDKByJNA.NET_DVR_TIME_V30 struAlarmTime = new HCNetSDKByJNA.NET_DVR_TIME_V30();
        public int dwChannel;
        public int dwPicDataLen;
        public byte byPicType;
        public byte[] byRes3 = new byte[3];
        public ByteByReference pPicData;
        public byte[] byRes2 = new byte[48];

        public NET_DVR_VEHICLE_CONTROL_ALARM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byListType", "byPlateType", "byPlateColor", "byRes1", "sLicense", "sCardNo", "struAlarmTime", "dwChannel", "dwPicDataLen", "byPicType", "byRes3", "pPicData", "byRes2");
        }
    }

    public static class NET_DVR_VEHICLE_INFO extends Structure {
        public int dwIndex;
        public byte byVehicleType;
        public byte byColorDepth;
        public byte byColor;
        public byte byRadarState;
        public short wSpeed;
        public short wLength;
        public byte byIllegalType;
        public byte byVehicleLogoRecog;
        public byte byVehicleSubLogoRecog;
        public byte byVehicleModel;
        public byte[] byCustomInfo = new byte[16];
        public short wVehicleLogoRecog;
        public byte byIsParking;
        public byte byRes;
        public int dwParkingTime;
        public byte byBelieve;
        public byte byCurrentWorkerNumber;
        public byte byCurrentGoodsLoadingRate;
        public byte byDoorsStatus;
        public byte[] byRes3 = new byte[4];

        public NET_DVR_VEHICLE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwIndex", "byVehicleType", "byColorDepth", "byColor", "byRadarState", "wSpeed", "wLength", "byIllegalType", "byVehicleLogoRecog", "byVehicleSubLogoRecog", "byVehicleModel", "byCustomInfo", "wVehicleLogoRecog", "byIsParking", "byRes", "dwParkingTime", "byBelieve", "byCurrentWorkerNumber", "byCurrentGoodsLoadingRate", "byDoorsStatus", "byRes3");
        }
    }

    public static class NET_DVR_VICOLOR extends Structure {
        public HCNetSDKByJNA.NET_DVR_COLOR[] struShelter = new HCNetSDKByJNA.NET_DVR_COLOR[8];
        public HCNetSDKByJNA.NET_DVR_SCHEDTIME[] struHandleTime = new HCNetSDKByJNA.NET_DVR_SCHEDTIME[8];

        public NET_DVR_VICOLOR() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struShelter", "struHandleTime");
        }
    }

    public static class NET_DVR_VIDEOEFFECT extends Structure {
        public byte byBrightnessLevel;
        public byte byContrastLevel;
        public byte bySharpnessLevel;
        public byte bySaturationLevel;
        public byte byHueLevel;
        public byte byEnableFunc;
        public byte byLightInhibitLevel;
        public byte byGrayLevel;

        public NET_DVR_VIDEOEFFECT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byBrightnessLevel", "byContrastLevel", "bySharpnessLevel", "bySaturationLevel", "byHueLevel", "byEnableFunc", "byLightInhibitLevel", "byGrayLevel");
        }
    }

    public static class NET_DVR_VIDEOWALLDISPLAYPOSITION extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public int dwVideoWallNo;
        public int dwDisplayNo;
        public HCNetSDKByJNA.NET_DVR_RECTCFG_EX struRectCfg = new HCNetSDKByJNA.NET_DVR_RECTCFG_EX();
        public byte[] byRes2 = new byte[64];

        public NET_DVR_VIDEOWALLDISPLAYPOSITION() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byRes1", "dwVideoWallNo", "dwDisplayNo", "struRectCfg", "byRes2");
        }
    }

    public static class NET_DVR_VIDEO_CALL_COND extends Structure {
        public int dwSize;
        public byte[] byRes = new byte[128];

        public NET_DVR_VIDEO_CALL_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byRes");
        }
    }

    public static class NET_DVR_VIDEO_CALL_PARAM extends Structure {
        public int dwSize;
        public int dwCmdType;
        public byte[] byRes = new byte[128];

        public NET_DVR_VIDEO_CALL_PARAM() {
        }

        public NET_DVR_VIDEO_CALL_PARAM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwCmdType", "byRes");
        }
    }

    public static class NET_DVR_VILOST_V40 extends Structure {
        public int dwEnableVILostAlarm;
        public int dwHandleType;
        public int dwMaxRelAlarmOutChanNum;
        public int[] dwRelAlarmOut = new int[4128];
        public HCNetSDKByJNA.NET_DVR_SCHEDTIMEWEEK[] struAlarmTime = new HCNetSDKByJNA.NET_DVR_SCHEDTIMEWEEK[7];
        public byte byVILostAlarmThreshold;
        public byte[] byRes = new byte[63];

        public NET_DVR_VILOST_V40() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwEnableVILostAlarm", "dwHandleType", "dwMaxRelAlarmOutChanNum", "dwRelAlarmOut", "struAlarmTime", "byVILostAlarmThreshold", "byRes");
        }
    }

    public static class NET_DVR_WALLWIN_INFO extends Structure {
        public int dwSize;
        public int dwWinNum;
        public int dwSubWinNum;
        public int dwWallNo;
        public byte[] byRes = new byte[12];

        public NET_DVR_WALLWIN_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwWinNum", "dwSubWinNum", "dwWallNo", "byRes");
        }
    }

    public static class NET_DVR_WALL_WIN_STATUS extends Structure {
        public int dwSize;
        public byte byDecodeStatus;
        public byte byStreamType;
        public byte byPacketType;
        public byte byFpsDecV;
        public byte byFpsDecA;
        public byte[] byRes1 = new byte[7];
        public int dwDecodedV;
        public int dwDecodedA;
        public short wImgW;
        public short wImgH;
        public byte byStreamMode;
        public byte[] byRes2 = new byte[31];

        public NET_DVR_WALL_WIN_STATUS() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byDecodeStatus", "byStreamType", "byPacketType", "byFpsDecV", "byFpsDecA", "byRes1", "dwDecodedV", "dwDecodedA", "wImgW", "wImgH", "byStreamMode", "byRes2");
        }
    }

    public static class NET_DVR_WDR extends Structure {
        public byte byWDREnabled;
        public byte byWDRLevel1;
        public byte byWDRLevel2;
        public byte byWDRContrastLevel;
        public byte[] byRes = new byte[16];

        public NET_DVR_WDR() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byWDREnabled", "byWDRLevel1", "byWDRLevel2", "byWDRContrastLevel", "byRes");
        }
    }

    public static class NET_DVR_WEEK_PLAN_CFG extends Structure {
        public int dwSize;
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.arrayStruPlanCfg[] struPlanCfg = (HCNetSDKByJNA.arrayStruPlanCfg[])(new HCNetSDKByJNA.arrayStruPlanCfg()).toArray(7);
        public byte[] byRes2 = new byte[16];

        public NET_DVR_WEEK_PLAN_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byEnable", "byRes1", "struPlanCfg", "byRes2");
        }
    }

    public static class NET_DVR_WEEK_PLAN_COND extends Structure {
        public int dwSize;
        public int dwWeekPlanNumber;
        public short wLocalControllerID;
        public byte[] byRes = new byte[106];

        public NET_DVR_WEEK_PLAN_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwWeekPlanNumber", "wLocalControllerID", "byRes");
        }
    }

    public static class NET_DVR_WHITEBALANCE extends Structure {
        public byte byWhiteBalanceMode;
        public byte byWhiteBalanceModeRGain;
        public byte byWhiteBalanceModeBGain;
        public byte[] byRes = new byte[5];

        public NET_DVR_WHITEBALANCE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byWhiteBalanceMode", "byWhiteBalanceModeRGain", "byWhiteBalanceModeBGain", "byRes");
        }
    }

    public static class NET_DVR_WIFI_WORKMODE extends Structure {
        public int dwSize;
        public int dwNetworkInterfaceMode;

        public NET_DVR_WIFI_WORKMODE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwNetworkInterfaceMode");
        }
    }

    public static class NET_DVR_XML_CONFIG_INPUT extends Structure {
        public int dwSize;
        public Pointer lpRequestUrl;
        public int dwRequestUrlLen;
        public Pointer lpInBuffer;
        public int dwInBufferSize;
        public int dwRecvTimeOut;
        public byte[] byRes = new byte[32];

        public NET_DVR_XML_CONFIG_INPUT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "lpRequestUrl", "dwRequestUrlLen", "lpInBuffer", "dwInBufferSize", "dwRecvTimeOut", "byRes");
        }
    }

    public static class NET_DVR_XML_CONFIG_OUTPUT extends Structure {
        public int dwSize;
        public Pointer lpOutBuffer;
        public int dwOutBufferSize;
        public int dwReturnedXMLSize;
        public Pointer lpStatusBuffer;
        public int dwStatusSize;
        public byte[] byRes = new byte[32];

        public NET_DVR_XML_CONFIG_OUTPUT() {
        }

        protected List getFieldOrder() {
            return Arrays.asList("dwSize", "lpOutBuffer", "dwOutBufferSize", "dwReturnedXMLSize", "lpStatusBuffer", "dwStatusSize", "byRes");
        }
    }

    public static class NET_ITC_ACCESS_DEVINFO_PARAM_UNION extends Union {
        public byte[] uLen = new byte[128];
        public HCNetSDKByJNA.NET_ITC_RADAR_INFO_PARAM struRadarInfoParam = new HCNetSDKByJNA.NET_ITC_RADAR_INFO_PARAM();

        public NET_ITC_ACCESS_DEVINFO_PARAM_UNION() {
        }
    }

    public static class NET_ITC_INTERVAL_PARAM extends Structure {
        public byte byIntervalType;
        public byte[] byRes1 = new byte[3];
        public short[] wInterval = new short[4];
        public byte[] byRes = new byte[8];

        public NET_ITC_INTERVAL_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byIntervalType", "byRes1", "wInterval", "byRes");
        }
    }

    public static class NET_ITC_LANE_PARAM extends Structure {
        public byte byEnable;
        public byte byRelatedDriveWay;
        public short wDistance;
        public short wTrigDelayTime;
        public byte byTrigDelayDistance;
        public byte bySpeedCapEn;
        public byte bySignSpeed;
        public byte bySpeedLimit;
        public byte bySnapTimes;
        public byte byOverlayDriveWay;
        public HCNetSDKByJNA.NET_ITC_INTERVAL_PARAM struInterval = new HCNetSDKByJNA.NET_ITC_INTERVAL_PARAM();
        public byte[] byRelatedIOOut = new byte[4];
        public byte byFlashMode;
        public byte byCartSignSpeed;
        public byte byCartSpeedLimit;
        public byte byRelatedIOOutEx;
        public HCNetSDKByJNA.NET_ITC_PLATE_RECOG_REGION_PARAM[] struPlateRecog = (HCNetSDKByJNA.NET_ITC_PLATE_RECOG_REGION_PARAM[])(new HCNetSDKByJNA.NET_ITC_PLATE_RECOG_REGION_PARAM()).toArray(2);
        public byte byLaneType;
        public byte byUseageType;
        public byte byRelaLaneDirectionType;
        public byte byLowSpeedLimit;
        public byte byBigCarLowSpeedLimit;
        public byte byLowSpeedCapEn;
        public byte byEmergencyCapEn;
        public byte[] byRes = new byte[9];

        public NET_ITC_LANE_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byRelatedDriveWay", "wDistance", "wTrigDelayTime", "byTrigDelayDistance", "bySpeedCapEn", "bySignSpeed", "bySpeedLimit", "bySnapTimes", "byOverlayDriveWay", "struInterval", "byRelatedIOOut", "byFlashMode", "byCartSignSpeed", "byCartSpeedLimit", "byRelatedIOOutEx", "struPlateRecog", "byLaneType", "byUseageType", "byRelaLaneDirectionType", "byLowSpeedLimit", "byBigCarLowSpeedLimit", "byLowSpeedCapEn", "byEmergencyCapEn", "byRes");
        }
    }

    public static class NET_ITC_PLATE_RECOG_PARAM extends Structure {
        public byte[] byDefaultCHN = new byte[3];
        public byte byEnable;
        public int dwRecogMode;
        public byte byVehicleLogoRecog;
        public byte byProvince;
        public byte byRegion;
        public byte byRes1;
        public short wPlatePixelWidthMin;
        public short wPlatePixelWidthMax;
        public byte[] byRes = new byte[24];

        public NET_ITC_PLATE_RECOG_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byDefaultCHN", "byEnable", "dwRecogMode", "byVehicleLogoRecog", "byProvince", "byRegion", "byRes1", "wPlatePixelWidthMin", "wPlatePixelWidthMax", "byRes");
        }
    }

    public static class NET_ITC_PLATE_RECOG_REGION_PARAM extends Structure {
        public byte byMode;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.unionRegion uRegion = new HCNetSDKByJNA.unionRegion();
        public byte[] byRes = new byte[16];

        public NET_ITC_PLATE_RECOG_REGION_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byMode", "byRes1", "uRegion", "byRes");
        }
    }

    public static class NET_ITC_POLYGON extends Structure {
        public int dwPointNum;
        public HCNetSDKByJNA.NET_VCA_POINT[] struPos = (HCNetSDKByJNA.NET_VCA_POINT[])(new HCNetSDKByJNA.NET_VCA_POINT()).toArray(20);

        public NET_ITC_POLYGON() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwPointNum", "struPos");
        }
    }

    public static class NET_ITC_POST_RS485_RADAR_PARAM extends Structure {
        public byte byRelatedLaneNum;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_ITC_PLATE_RECOG_PARAM struPlateRecog = new HCNetSDKByJNA.NET_ITC_PLATE_RECOG_PARAM();
        public HCNetSDKByJNA.NET_ITC_LANE_PARAM[] struLane = (HCNetSDKByJNA.NET_ITC_LANE_PARAM[])(new HCNetSDKByJNA.NET_ITC_LANE_PARAM()).toArray(6);
        public HCNetSDKByJNA.NET_ITC_RADAR_PARAM struRadar = new HCNetSDKByJNA.NET_ITC_RADAR_PARAM();
        public byte[] byRes = new byte[32];

        public NET_ITC_POST_RS485_RADAR_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRelatedLaneNum", "byRes1", "struPlateRecog", "struLane", "struRadar", "byRes");
        }
    }

    public static class NET_ITC_RADAR_INFO_PARAM extends Structure {
        public HCNetSDKByJNA.NET_ITC_RADAR_PARAM struRadarParam = new HCNetSDKByJNA.NET_ITC_RADAR_PARAM();
        public byte byAssociateLaneNo;
        public byte[] byRes = new byte[103];

        public NET_ITC_RADAR_INFO_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRadarParam", "byAssociateLaneNo", "byRes");
        }
    }

    public static class NET_ITC_RADAR_PARAM extends Structure {
        public byte byRadarType;
        public byte byLevelAngle;
        public short wRadarSensitivity;
        public short wRadarSpeedValidTime;
        public byte[] byRes1 = new byte[2];
        public float fLineCorrectParam;
        public int iConstCorrectParam;
        public byte[] byRes2 = new byte[8];

        public NET_ITC_RADAR_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRadarType", "byLevelAngle", "wRadarSensitivity", "wRadarSpeedValidTime", "byRes1", "fLineCorrectParam", "iConstCorrectParam", "byRes2");
        }
    }

    public static class NET_ITC_RS485_ACCESS_CFG extends Structure {
        public int dwSize;
        public byte byModeType;
        public byte[] byRes = new byte[3];
        public HCNetSDKByJNA.NET_ITC_ACCESS_DEVINFO_PARAM_UNION uITCAccessDevinfoParam = new HCNetSDKByJNA.NET_ITC_ACCESS_DEVINFO_PARAM_UNION();
        public byte[] byRes1 = new byte[12];

        public NET_ITC_RS485_ACCESS_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "byModeType", "byRes", "uITCAccessDevinfoParam", "byRes1");
        }
    }

    public static class NET_ITC_RS485_ACCESS_INFO_COND extends Structure {
        public int dwSize;
        public int dwChannel;
        public int dwTriggerModeType;
        public byte byAssociateRS485No;
        public byte[] byRes = new byte[15];

        public NET_ITC_RS485_ACCESS_INFO_COND() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwChannel", "dwTriggerModeType", "byAssociateRS485No", "byRes");
        }
    }

    public static class NET_ITC_SINGLE_TRIGGERCFG extends Structure {
        public byte byEnable;
        public byte[] byRes1 = new byte[3];
        public int dwTriggerType;
        public HCNetSDKByJNA.NET_ITC_TRIGGER_PARAM_UNION uTriggerParam = new HCNetSDKByJNA.NET_ITC_TRIGGER_PARAM_UNION();
        public byte[] byRes = new byte[64];

        public NET_ITC_SINGLE_TRIGGERCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byRes1", "dwTriggerType", "uTriggerParam", "byRes");
        }
    }

    public static class NET_ITC_TRIGGERCFG extends Structure {
        public int dwSize;
        public HCNetSDKByJNA.NET_ITC_SINGLE_TRIGGERCFG struTriggerParam = new HCNetSDKByJNA.NET_ITC_SINGLE_TRIGGERCFG();
        public byte[] byRes = new byte[32];

        public NET_ITC_TRIGGERCFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "struTriggerParam", "byRes");
        }
    }

    public static class NET_ITC_TRIGGER_PARAM_UNION extends Union {
        public int[] uLen = new int[1070];
        public HCNetSDKByJNA.NET_ITC_POST_RS485_RADAR_PARAM struPostRadar = new HCNetSDKByJNA.NET_ITC_POST_RS485_RADAR_PARAM();

        public NET_ITC_TRIGGER_PARAM_UNION() {
        }
    }

    public static class NET_ITS_PICTURE_INFO extends Structure {
        public int dwDataLen;
        public byte byType;
        public byte byDataType;
        public byte byCloseUpType;
        public byte byPicRecogMode;
        public int dwRedLightTime;
        public byte[] byAbsTime = new byte[32];
        public HCNetSDKByJNA.NET_VCA_RECT struPlateRect;
        public HCNetSDKByJNA.NET_VCA_RECT struPlateRecgRect;
        public ByteByReference pBuffer;
        public int dwUTCTime;
        public byte byCompatibleAblity;
        public byte[] byRes2 = new byte[7];

        public NET_ITS_PICTURE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwDataLen", "byType", "byDataType", "byCloseUpType", "byPicRecogMode", "dwRedLightTime", "byAbsTime", "struPlateRect", "struPlateRecgRect", "pBuffer", "dwUTCTime", "byCompatibleAblity", "byRes2");
        }
    }

    public static class NET_ITS_PLATE_RESULT extends Structure {
        public int dwSize;
        public int dwMatchNo;
        public byte byGroupNum;
        public byte byPicNo;
        public byte bySecondCam;
        public byte byFeaturePicNo;
        public byte byDriveChan;
        public byte byVehicleType;
        public byte byDetSceneID;
        public byte byVehicleAttribute;
        public short wIllegalType;
        public byte[] byIllegalSubType = new byte[8];
        public byte byPostPicNo;
        public byte byChanIndex;
        public short wSpeedLimit;
        public byte[] byRes2 = new byte[2];
        public HCNetSDKByJNA.NET_DVR_PLATE_INFO struPlateInfo = new HCNetSDKByJNA.NET_DVR_PLATE_INFO();
        public HCNetSDKByJNA.NET_DVR_VEHICLE_INFO struVehicleInfo = new HCNetSDKByJNA.NET_DVR_VEHICLE_INFO();
        public byte[] byMonitoringSiteID = new byte[48];
        public byte[] byDeviceID = new byte[48];
        public byte byDir;
        public byte byDetectType;
        public byte byRelaLaneDirectionType;
        public byte byCarDirectionType;
        public int dwCustomIllegalType;
        public ByteByReference pIllegalInfoBuf;
        public byte byIllegalFromatType;
        public byte byPendant;
        public byte byDataAnalysis;
        public byte byYellowLabelCar;
        public byte byDangerousVehicles;
        public byte byPilotSafebelt;
        public byte byCopilotSafebelt;
        public byte byPilotSunVisor;
        public byte byCopilotSunVisor;
        public byte byPilotCall;
        public byte byBarrierGateCtrlType;
        public byte byAlarmDataType;
        public HCNetSDKByJNA.NET_DVR_TIME_V30 struSnapFirstPicTime = new HCNetSDKByJNA.NET_DVR_TIME_V30();
        public int dwIllegalTime;
        public int dwPicNum;
        public HCNetSDKByJNA.NET_ITS_PICTURE_INFO[] struPicInfo = (HCNetSDKByJNA.NET_ITS_PICTURE_INFO[])(new HCNetSDKByJNA.NET_ITS_PICTURE_INFO()).toArray(6);

        public NET_ITS_PLATE_RESULT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwMatchNo", "byGroupNum", "byPicNo", "bySecondCam", "byFeaturePicNo", "byDriveChan", "byVehicleType", "byDetSceneID", "byVehicleAttribute", "wIllegalType", "byIllegalSubType", "byPostPicNo", "byChanIndex", "wSpeedLimit", "byRes2", "struPlateInfo", "struVehicleInfo", "byMonitoringSiteID", "byDeviceID", "byDir", "byDetectType", "byRelaLaneDirectionType", "byCarDirectionType", "dwCustomIllegalType", "pIllegalInfoBuf", "byIllegalFromatType", "byPendant", "byDataAnalysis", "byYellowLabelCar", "byDangerousVehicles", "byPilotSafebelt", "byCopilotSafebelt", "byPilotSunVisor", "byCopilotSunVisor", "byPilotCall", "byBarrierGateCtrlType", "byAlarmDataType", "struSnapFirstPicTime", "dwIllegalTime", "dwPicNum", "struPicInfo");
        }
    }

    public static class NET_SDK_DOWNLOAD_TYPE {
        public static final int NET_SDK_DOWNLOAD_CERT = 0;
        public static final int NET_SDK_DOWNLOAD_IPC_CFG_FILE = 1;
        public static final int NET_SDK_DOWNLOAD_BASELINE_SCENE_PIC = 2;
        public static final int NET_SDK_DOWNLOAD_VQD_ALARM_PIC = 3;
        public static final int NET_SDK_DOWNLOAD_CONFIGURATION_FILE = 4;
        public static final int NET_SDK_DOWNLOAD_SCENE_CONFIGURATION_FILE = 5;
        public static final int NET_SDK_DOWNLOAD_FILE_FORM_DB = 6;
        public static final int NET_SDK_DOWNLOAD_TME_FILE = 7;
        public static final int NET_SDK_DOWNLOAD_VEHICLE_BLACKWHITELST_FILE = 8;
        public static final int NET_SDK_DOWNLOAD_GUID_FILE = 9;
        public static final int NET_SDK_DOWNLOAD_FILE_FORM_CLOUD = 10;
        public static final int NET_SDK_DOWNLOAD_PICTURE = 11;
        public static final int NET_SDK_DOWNLOAD_VIDEO = 12;
        public static final int NET_DVR_DOWNLOAD_SCREEN_FILE = 13;
        public static final int NET_SDK_DOWNLOAD_PUBLISH_MATERIAL = 14;
        public static final int NET_SDK_DOWNLOAD_THERMOMETRIC_FILE = 15;
        public static final int NET_SDK_DOWNLOAD_LED_CHECK_FILE = 16;
        public static final int NET_SDK_DOWNLOAD_VEHICLE_INFORMATION = 17;
        public static final int NET_SDK_DOWNLOAD_CERTIFICATE_BLACK_LIST_TEMPLET = 18;
        public static final int NET_SDK_DOWNLOAD_LOG_FILE = 19;
        public static final int NET_SDK_DOWNLOAD_FILEVOLUME_DATA = 20;
        public static final int NET_SDK_DOWNLOAD_FD_DATA = 21;
        public static final int NET_SDK_DOWNLOAD_SECURITY_CFG_FILE = 22;
        public static final int NET_SDK_DOWNLOAD_PUBLISH_SCHEDULE = 23;
        public static final int NET_SDK_DOWNLOAD_RIGHT_CONTROLLER_AUDIO = 24;
        public static final int NET_SDK_DOWNLOAD_MODBUS_CFG_FILE = 25;
        public static final int NET_SDK_DOWNLOAD_RS485_PROTOCOL_DLL_FILE = 26;
        public static final int NET_SDK_DOWNLOAD_CLUSTER_MAINTENANCE_LOG = 27;
        public static final int NET_SDK_DOWNLOAD_SQL_ARCHIVE_FILE = 28;
        public static final int NET_SDK_DOWNLOAD_SUBWIND_STREAM = 29;
        public static final int NET_SDK_DOWNLOAD_DEVTYPE_CALIBFILE = 30;
        public static final int NET_SDK_DOWNLOAD_HD_CAMERA_CORRECT_TABLE = 31;
        public static final int NET_SDK_DOWNLOAD_CLIENT_CALIBFILE = 32;
        public static final int NET_SDK_DOWNLOAD_FOUE_CAMERAS_PICTURES = 33;
        public static final int NET_SDK_DOWNLOAD_DOOR_CONTENT = 34;
        public static final int NET_SDK_DOWNLOAD_PUBLISH_MATERIAL_THUMBNAIL = 35;
        public static final int NET_SDK_DOWNLOAD_PUBLISH_PROGRAM_THUMBNAIL = 36;
        public static final int NET_SDK_DOWNLOAD_PUBLISH_TEMPLATE_THUMBNAIL = 37;

        public NET_SDK_DOWNLOAD_TYPE() {
        }
    }

    public static class NET_SDK_LOCAL_CFG_TYPE {
        public static final int NET_SDK_LOCAL_CFG_TYPE_TCP_PORT_BIND = 0;
        public static final int NET_SDK_LOCAL_CFG_TYPE_UDP_PORT_BIND = 1;
        public static final int NET_SDK_LOCAL_CFG_TYPE_MEM_POOL = 2;
        public static final int NET_SDK_LOCAL_CFG_TYPE_MODULE_RECV_TIMEOUT = 3;
        public static final int NET_SDK_LOCAL_CFG_TYPE_ABILITY_PARSE = 4;
        public static final int NET_SDK_LOCAL_CFG_TYPE_TALK_MODE = 5;
        public static final int NET_SDK_LOCAL_CFG_TYPE_PROTECT_KEY = 6;
        public static final int NET_SDK_LOCAL_CFG_TYPE_CFG_VERSION = 7;
        public static final int NET_SDK_LOCAL_CFG_TYPE_RTSP_PARAMS = 8;
        public static final int NET_SDK_LOCAL_CFG_TYPE_SIMXML_LOGIN = 9;
        public static final int NET_SDK_LOCAL_CFG_TYPE_CHECK_DEV = 10;
        public static final int NET_SDK_LOCAL_CFG_TYPE_SECURITY = 11;
        public static final int NET_SDK_LOCAL_CFG_TYPE_EZVIZLIB_PATH = 12;
        public static final int NET_SDK_LOCAL_CFG_TYPE_CHAR_ENCODE = 13;
        public static final int NET_SDK_LOCAL_CFG_TYPE_PROXYS = 14;
        public static final int NET_DVR_LOCAL_CFG_TYPE_LOG = 15;
        public static final int NET_DVR_LOCAL_CFG_TYPE_STREAM_CALLBACK = 16;
        public static final int NET_DVR_LOCAL_CFG_TYPE_GENERAL = 17;
        public static final int NET_DVR_LOCAL_CFG_TYPE_PTZ = 18;

        public NET_SDK_LOCAL_CFG_TYPE() {
        }
    }

    public static class NET_SDK_UPLOAD_TYPE {
        public static final int UPGRADE_CERT_FILE = 0;
        public static final int UPLOAD_CERT_FILE = 1;
        public static final int TRIAL_CERT_FILE = 2;
        public static final int CONFIGURATION_FILE = 3;
        public static final int UPLOAD_RECORD_FILE = 4;
        public static final int SCENE_CONFIGURATION_FILE = 5;
        public static final int UPLOAD_PICTURE_FILE = 6;
        public static final int UPLOAD_VIOLATION_FILE = 7;
        public static final int UPLOAD_TG_FILE = 8;
        public static final int UPLOAD_DATA_TO_DB = 9;
        public static final int UPLOAD_BACKGROUND_PIC = 10;
        public static final int UPLOAD_CALIBRATION_FILE = 11;
        public static final int UPLOAD_TME_FILE = 12;
        public static final int UPLOAD_VEHICLE_BLACKWHITELST_FILE = 13;
        public static final int UPLOAD_PICTURE_TO_CLOUD = 15;
        public static final int UPLOAD_VIDEO_FILE = 16;
        public static final int UPLOAD_SCREEN_FILE = 17;
        public static final int UPLOAD_PUBLISH_MATERIAL = 18;
        public static final int UPLOAD_PUBLISH_UPGRADE_FILE = 19;
        public static final int UPLOAD_RING_FILE = 20;
        public static final int UPLOAD_ENCRYPT_CERT = 21;
        public static final int UPLOAD_THERMOMETRIC_FILE = 22;
        public static final int UPLOAD_SUBBRAND_FILE = 23;
        public static final int UPLOAD_LED_CHECK_FILE = 24;
        public static final int BATCH_UPLOAD_PICTURE_FILE = 25;
        public static final int UPLOAD_EDID_CFG_FILE = 26;
        public static final int UPLOAD_PANORAMIC_STITCH = 27;
        public static final int UPLOAD_BINOCULAR_COUNTING = 28;
        public static final int UPLOAD_AUDIO_FILE = 29;
        public static final int UPLOAD_PUBLISH_THIRD_PARTY_FILE = 30;
        public static final int UPLOAD_DEEPEYES_BINOCULAR = 31;
        public static final int UPLOAD_CERTIFICATE_BLACK_LIST = 32;
        public static final int UPLOAD_HD_CAMERA_CORRECT_TABLE = 33;
        public static final int UPLOAD_FD_DATA = 35;
        public static final int UPLOAD_FACE_DATA = 36;
        public static final int UPLOAD_FACE_ANALYSIS_DATA = 37;
        public static final int UPLOAD_FILEVOLUME_DATA = 38;
        public static final int IMPORT_DATA_TO_FACELIB = 39;
        public static final int UPLOAD_LEFTEYE_4K_CALIBFILE = 40;
        public static final int UPLOAD_SECURITY_CFG_FILE = 41;
        public static final int UPLOAD_RIGHT_CONTROLLER_AUDIO = 42;
        public static final int UPLOAD_MODBUS_CFG_FILE = 43;
        public static final int UPLOAD_NOTICE_VIDEO_DATA = 44;
        public static final int UPLOAD_RS485_PROTOCOL_DLL_FILE = 45;
        public static final int UPLOAD_PIC_BY_BUF = 46;
        public static final int UPLOAD_CLIENT_CALIBFILE = 47;
        public static final int UPLOAD_HD_CAMERA_CORRECT_TABLE_3200W = 48;
        public static final int UPLOAD_DOOR_CONTENT = 49;
        public static final int UPLOAD_ASR_CONTROL_FILE = 50;
        public static final int UPLOAD_APP_FILE = 51;
        public static final int UPLOAD_AI_ALGORITHM_MODEL = 52;
        public static final int UPLOAD_PUBLISH_PROGRAM_THUMBNAIL = 53;
        public static final int UPLOAD_PUBLISH_TEMPLATE_THUMBNAIL = 54;

        public NET_SDK_UPLOAD_TYPE() {
        }
    }

    public static class NET_VCA_ADV_REACH_HEIGHT extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public int dwCrossDirection;
        public byte[] byRes = new byte[4];

        public NET_VCA_ADV_REACH_HEIGHT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "dwCrossDirection", "byRes");
        }
    }

    public static class NET_VCA_ADV_TRAVERSE_PLANE extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public int dwCrossDirection;
        public byte bySensitivity;
        public byte[] byRes = new byte[3];

        public NET_VCA_ADV_TRAVERSE_PLANE(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "dwCrossDirection", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_AREA extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public byte byDetectionTarget;
        public byte[] byRes = new byte[7];

        public NET_VCA_AREA(Pointer pointer) {
            super(pointer);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "byDetectionTarget", "byRes");
        }
    }

    public static class NET_VCA_AUDIO_ABNORMAL extends Structure {
        public short wDecibel;
        public byte bySensitivity;
        public byte byAudioMode;
        public byte byEnable;
        public byte byThreshold;
        public byte[] byRes = new byte[54];

        public NET_VCA_AUDIO_ABNORMAL(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wDecibel", "bySensitivity", "byAudioMode", "byEnable", "byThreshold", "byRes");
        }
    }

    public static class NET_VCA_BLACKLIST_INFO extends Structure {
        public int dwSize;
        public int dwRegisterID;
        public int dwGroupNo;
        public byte byType;
        public byte byLevel;
        public byte[] byRes1 = new byte[2];
        public HCNetSDKByJNA.NET_VCA_HUMAN_ATTRIBUTE struAttribute = new HCNetSDKByJNA.NET_VCA_HUMAN_ATTRIBUTE();
        public byte[] byRemark = new byte[32];
        public int dwFDDescriptionLen;
        public ByteByReference pFDDescriptionBuffer;
        public byte[] byRes2 = new byte[12];

        public NET_VCA_BLACKLIST_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwRegisterID", "dwGroupNo", "byType", "byLevel", "byRes1", "struAttribute", "byRemark", "dwFDDescriptionLen", "pFDDescriptionBuffer", "byRes2");
        }
    }

    public static class NET_VCA_BLACKLIST_INFO_ALARM extends Structure {
        public HCNetSDKByJNA.NET_VCA_BLACKLIST_INFO struBlackListInfo = new HCNetSDKByJNA.NET_VCA_BLACKLIST_INFO();
        public int dwBlackListPicLen;
        public int dwFDIDLen;
        public ByteByReference pFDID;
        public int dwPIDLen;
        public ByteByReference pPID;
        public short wThresholdValue;
        public byte[] byRes = new byte[2];
        public ByteByReference pBuffer1;

        public NET_VCA_BLACKLIST_INFO_ALARM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struBlackListInfo", "dwBlackListPicLen", "dwFDIDLen", "pFDID", "dwPIDLen", "pPID", "wThresholdValue", "byRes", "pBuffer1");
        }
    }

    public static class NET_VCA_COMBINED_RULE extends Structure {
        public byte byRuleSequence;
        public byte[] byRes = new byte[7];
        public int dwMinInterval;
        public int dwMaxInterval;
        public HCNetSDKByJNA.NET_VCA_RELATE_RULE_PARAM struRule1Raram = new HCNetSDKByJNA.NET_VCA_RELATE_RULE_PARAM();
        public HCNetSDKByJNA.NET_VCA_RELATE_RULE_PARAM struRule2Raram = new HCNetSDKByJNA.NET_VCA_RELATE_RULE_PARAM();
        public byte[] byRes1 = new byte[36];

        public NET_VCA_COMBINED_RULE(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRuleSequence", "byRes", "dwMinInterval", "dwMaxInterval", "struRule1Raram", "struRule2Raram", "byRes1");
        }
    }

    public static class NET_VCA_DEV_INFO extends Structure {
        public HCNetSDKByJNA.NET_DVR_IPADDR struDevIP = new HCNetSDKByJNA.NET_DVR_IPADDR();
        public short wPort;
        public byte byChannel;
        public byte byIvmsChannel;

        public NET_VCA_DEV_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struDevIP", "wPort", "byChannel", "byIvmsChannel");
        }
    }

    public static class NET_VCA_EVENT_UNION extends Union {
        public int[] uLen = new int[23];

        public NET_VCA_EVENT_UNION() {
        }
    }

    public static class NET_VCA_FACESNAP_INFO_ALARM extends Structure {
        public int dwRelativeTime;
        public int dwAbsTime;
        public int dwSnapFacePicID;
        public int dwSnapFacePicLen;
        public HCNetSDKByJNA.NET_VCA_DEV_INFO struDevInfo = new HCNetSDKByJNA.NET_VCA_DEV_INFO();
        public byte byFaceScore;
        public byte bySex;
        public byte byGlasses;
        public byte byAge;
        public byte byAgeDeviation;
        public byte[] byRes1 = new byte[3];
        public int dwUIDLen;
        public ByteByReference pUIDBuffer;
        public byte[] byRes = new byte[4];
        public ByteByReference pBuffer1;

        public NET_VCA_FACESNAP_INFO_ALARM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwRelativeTime", "dwAbsTime", "dwSnapFacePicID", "dwSnapFacePicLen", "struDevInfo", "byFaceScore", "bySex", "byGlasses", "byAge", "byAgeDeviation", "byRes1", "dwUIDLen", "pUIDBuffer", "byRes", "pBuffer1");
        }
    }

    public static class NET_VCA_FACESNAP_MATCH_ALARM extends Structure {
        public int dwSize;
        public float fSimilarity;
        public HCNetSDKByJNA.NET_VCA_FACESNAP_INFO_ALARM struSnapInfo = new HCNetSDKByJNA.NET_VCA_FACESNAP_INFO_ALARM();
        public HCNetSDKByJNA.NET_VCA_BLACKLIST_INFO_ALARM struBlackListInfo = new HCNetSDKByJNA.NET_VCA_BLACKLIST_INFO_ALARM();
        public byte[] sStorageIP = new byte[16];
        public short wStoragePort;
        public byte byMatchPicNum;
        public byte byPicTransType;
        public int dwSnapPicLen;
        public ByteByReference pSnapPicBuffer;
        public HCNetSDKByJNA.NET_VCA_RECT struRegion = new HCNetSDKByJNA.NET_VCA_RECT();
        public int dwModelDataLen;
        public ByteByReference pModelDataBuffer;
        public byte[] byRes = new byte[8];

        public NET_VCA_FACESNAP_MATCH_ALARM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "fSimilarity", "struSnapInfo", "struBlackListInfo", "sStorageIP", "wStoragePort", "byMatchPicNum", "byPicTransType", "dwSnapPicLen", "pSnapPicBuffer", "struRegion", "dwModelDataLen", "pModelDataBuffer", "byRes");
        }
    }

    public static class NET_VCA_FACESNAP_RESULT extends Structure {
        public int dwSize;
        public int dwRelativeTime;
        public int dwAbsTime;
        public int dwFacePicID;
        public int dwFaceScore;
        public HCNetSDKByJNA.NET_VCA_TARGET_INFO struTargetInfo = new HCNetSDKByJNA.NET_VCA_TARGET_INFO();
        public HCNetSDKByJNA.NET_VCA_RECT struRect = new HCNetSDKByJNA.NET_VCA_RECT();
        public HCNetSDKByJNA.NET_VCA_DEV_INFO struDevInfo = new HCNetSDKByJNA.NET_VCA_DEV_INFO();
        public int dwFacePicLen;
        public int dwBackgroundPicLen;
        public byte bySmart;
        public byte byAlarmEndMark;
        public byte byRepeatTimes;
        public byte byUploadEventDataType;
        public HCNetSDKByJNA.NET_VCA_HUMAN_FEATURE struFeature = new HCNetSDKByJNA.NET_VCA_HUMAN_FEATURE();
        public float fStayDuration;
        public byte[] sStorageIP = new byte[16];
        public short wStoragePort;
        public short wDevInfoIvmsChannelEx;
        public byte[] byRes1 = new byte[16];
        public ByteByReference pBuffer1;
        public ByteByReference pBuffer2;

        public NET_VCA_FACESNAP_RESULT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwRelativeTime", "dwAbsTime", "dwFacePicID", "dwFaceScore", "struTargetInfo", "struRect", "struDevInfo", "dwFacePicLen", "dwBackgroundPicLen", "bySmart", "byAlarmEndMark", "byRepeatTimes", "byUploadEventDataType", "struFeature", "fStayDuration", "sStorageIP", "wStoragePort", "wDevInfoIvmsChannelEx", "byRes1", "pBuffer1", "pBuffer2");
        }
    }

    public static class NET_VCA_FALL_DOWN extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte byHeightThreshold;
        public byte[] byRes = new byte[4];

        public NET_VCA_FALL_DOWN(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byHeightThreshold", "byRes");
        }
    }

    public static class NET_VCA_GET_UP extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte byMode;
        public byte bySensitivity;
        public byte[] byRes = new byte[4];

        public NET_VCA_GET_UP(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "byMode", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_HIGH_DENSITY extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public float fDensity;
        public byte bySensitivity;
        public byte byRes;
        public short wDuration;

        public NET_VCA_HIGH_DENSITY(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "fDensity", "bySensitivity", "byRes", "wDuration");
        }
    }

    public static class NET_VCA_HIGH_DENSITY_STATUS extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public float fDensity;
        public byte bySensitivity;
        public byte[] byRes = new byte[3];

        public NET_VCA_HIGH_DENSITY_STATUS(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "fDensity", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_HUMAN_ATTRIBUTE extends Structure {
        public byte bySex;
        public byte byCertificateType;
        public byte[] byBirthDate = new byte[10];
        public byte[] byName = new byte[32];
        public HCNetSDKByJNA.NET_DVR_AREAINFOCFG struNativePlace = new HCNetSDKByJNA.NET_DVR_AREAINFOCFG();
        public byte[] byCertificateNumber = new byte[32];

        public NET_VCA_HUMAN_ATTRIBUTE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("bySex", "byCertificateType", "byBirthDate", "byName", "struNativePlace", "byCertificateNumber");
        }
    }

    public static class NET_VCA_HUMAN_ENTER extends Structure {
        public int[] dwRes = new int[23];

        public NET_VCA_HUMAN_ENTER(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwRes");
        }
    }

    public static class NET_VCA_HUMAN_FEATURE extends Structure {
        public byte byAgeGroup;
        public byte bySex;
        public byte byEyeGlass;
        public byte byAge;
        public byte byAgeDeviation;
        public byte[] byRes = new byte[11];

        public NET_VCA_HUMAN_FEATURE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byAgeGroup", "bySex", "byEyeGlass", "byAge", "byAgeDeviation", "byRes");
        }
    }

    public static class NET_VCA_INTRUSION extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte byRate;
        public byte byDetectionTarget;
        public byte[] byRes = new byte[3];

        public NET_VCA_INTRUSION(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byRate", "byDetectionTarget", "byRes");
        }
    }

    public static class NET_VCA_LEAVE_POSITION extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wLeaveDelay;
        public short wStaticDelay;
        public byte byMode;
        public byte byPersonType;
        public byte[] byRes = new byte[2];

        public NET_VCA_LEAVE_POSITION(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wLeaveDelay", "wStaticDelay", "byMode", "byPersonType", "byRes");
        }
    }

    public static class NET_VCA_LEFT extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte[] byRes = new byte[5];

        public NET_VCA_LEFT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_LINE extends Structure {
        public HCNetSDKByJNA.NET_VCA_POINT struStart = new HCNetSDKByJNA.NET_VCA_POINT();
        public HCNetSDKByJNA.NET_VCA_POINT struEnd = new HCNetSDKByJNA.NET_VCA_POINT();

        public NET_VCA_LINE() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struStart", "struEnd");
        }
    }

    public static class NET_VCA_LOITER extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte[] byRes = new byte[6];

        public NET_VCA_LOITER(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_OVER_TIME extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte[] byRes = new byte[6];

        public NET_VCA_OVER_TIME(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_PARKING extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte[] byRes = new byte[6];

        public NET_VCA_PARKING(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_PEOPLENUM_CHANGE extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public byte bySensitivity;
        public byte byPeopleNumThreshold;
        public byte byDetectMode;
        public byte byNoneStateEffective;
        public short wDuration;
        public byte[] byRes = new byte[2];

        public NET_VCA_PEOPLENUM_CHANGE(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "bySensitivity", "byPeopleNumThreshold", "byDetectMode", "byNoneStateEffective", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_POINT extends Structure {
        public float fX;
        public float fY;

        public NET_VCA_POINT() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("fX", "fY");
        }
    }

    public static class NET_VCA_POLYGON extends Structure {
        public int dwPointNum;
        public HCNetSDKByJNA.NET_VCA_POINT[] struPos = (HCNetSDKByJNA.NET_VCA_POINT[])(new HCNetSDKByJNA.NET_VCA_POINT()).toArray(10);

        public NET_VCA_POLYGON() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwPointNum", "struPos");
        }
    }

    public static class NET_VCA_REACH_HIGHT extends Structure {
        public HCNetSDKByJNA.NET_VCA_LINE struVcaLine = new HCNetSDKByJNA.NET_VCA_LINE();
        public short wDuration;
        public byte[] byRes = new byte[6];

        public NET_VCA_REACH_HIGHT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struVcaLine", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_RECT extends Structure {
        public float fX;
        public float fY;
        public float fWidth;
        public float fHeight;

        public NET_VCA_RECT() {
        }

        protected List getFieldOrder() {
            return Arrays.asList("fX", "fY", "fWidth", "fHeight");
        }
    }

    public static class NET_VCA_RELATE_RULE_PARAM extends Structure {
        public byte byRuleID;
        public byte byRes;
        public short wEventType;

        public NET_VCA_RELATE_RULE_PARAM() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRuleID", "byRes", "wEventType");
        }
    }

    public static class NET_VCA_RULE_ALARM extends Structure {
        public int dwSize;
        public int dwRelativeTime;
        public int dwAbsTime;
        public HCNetSDKByJNA.NET_VCA_RULE_INFO struRuleInfo = new HCNetSDKByJNA.NET_VCA_RULE_INFO();
        public HCNetSDKByJNA.NET_VCA_TARGET_INFO struTargetInfo = new HCNetSDKByJNA.NET_VCA_TARGET_INFO();
        public HCNetSDKByJNA.NET_VCA_DEV_INFO struDevInfo = new HCNetSDKByJNA.NET_VCA_DEV_INFO();
        public int dwPicDataLen;
        public byte byPicType;
        public byte byRelAlarmPicNum;
        public byte bySmart;
        public byte byRes;
        public int dwAlarmID;
        public byte[] byRes2 = new byte[8];
        public ByteByReference pImage;

        public NET_VCA_RULE_ALARM(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwSize", "dwRelativeTime", "dwAbsTime", "struRuleInfo", "struTargetInfo", "struDevInfo", "dwPicDataLen", "byPicType", "byRelAlarmPicNum", "bySmart", "byRes", "dwAlarmID", "byRes2", "pImage");
        }
    }

    public static class NET_VCA_RULE_INFO extends Structure {
        public byte byRuleID;
        public byte byRes;
        public short wEventTypeEx;
        public byte[] byRuleName = new byte[32];
        public int dwEventType;
        public HCNetSDKByJNA.NET_VCA_EVENT_UNION uEventParam = new HCNetSDKByJNA.NET_VCA_EVENT_UNION();

        public NET_VCA_RULE_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byRuleID", "byRes", "wEventTypeEx", "byRuleName", "dwEventType", "uEventParam");
        }
    }

    public static class NET_VCA_RUN extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public float fRunDistance;
        public byte bySensitivity;
        public byte byMode;
        public byte byDetectionTarget;
        public byte byRes;

        public NET_VCA_RUN(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "fRunDistance", "bySensitivity", "byMode", "byDetectionTarget", "byRes");
        }
    }

    public static class NET_VCA_SCANNER extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte[] byRes = new byte[5];

        public NET_VCA_SCANNER(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_SIT_QUIETLY extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public int dwDuration;
        public byte[] byRes = new byte[4];

        public NET_VCA_SIT_QUIETLY(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "dwDuration", "byRes");
        }
    }

    public static class NET_VCA_SPACING_CHANGE extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public float fSpacingThreshold;
        public byte bySensitivity;
        public byte byDetectMode;
        public short wDuration;

        public NET_VCA_SPACING_CHANGE(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "fSpacingThreshold", "bySensitivity", "byDetectMode", "wDuration");
        }
    }

    public static class NET_VCA_STANDUP extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public byte bySensitivity;
        public byte byHeightThreshold;
        public short wDuration;
        public byte[] byRes = new byte[4];

        public NET_VCA_STANDUP(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "bySensitivity", "byHeightThreshold", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_STICK_UP extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte[] byRes = new byte[5];

        public NET_VCA_STICK_UP(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_TAKE extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte[] byRes = new byte[5];

        public NET_VCA_TAKE(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_TAKE_LEFT extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte[] byRes = new byte[6];

        public NET_VCA_TAKE_LEFT(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "byRes");
        }
    }

    public static class NET_VCA_TARGET_INFO extends Structure {
        public int dwID;
        public HCNetSDKByJNA.NET_VCA_RECT struRect = new HCNetSDKByJNA.NET_VCA_RECT();
        public byte[] byRes = new byte[4];

        public NET_VCA_TARGET_INFO() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwID", "struRect", "byRes");
        }
    }

    public static class NET_VCA_TOILET_TARRY extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDelay;
        public byte[] byRes = new byte[6];

        public NET_VCA_TOILET_TARRY(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDelay", "byRes");
        }
    }

    public static class NET_VCA_TRAIL extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wRes;
        public byte bySensitivity;
        public byte[] byRes = new byte[5];

        public NET_VCA_TRAIL(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wRes", "bySensitivity", "byRes");
        }
    }

    public static class NET_VCA_TRAVERSE_PLANE extends Structure {
        public HCNetSDKByJNA.NET_VCA_LINE struPlaneBottom = new HCNetSDKByJNA.NET_VCA_LINE();
        public int dwCrossDirection;
        public byte bySensitivity;
        public byte byPlaneHeight;
        public byte byDetectionTarget;
        public byte[] byRes2 = new byte[37];

        public NET_VCA_TRAVERSE_PLANE(Pointer pointer) {
            super(pointer);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struPlaneBottom", "dwCrossDirection", "bySensitivity", "byPlaneHeight", "byDetectionTarget", "byRes2");
        }
    }

    public static class NET_VCA_VIOLENT_MOTION extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDuration;
        public byte bySensitivity;
        public byte byMode;
        public byte[] byRes = new byte[4];

        public NET_VCA_VIOLENT_MOTION(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDuration", "bySensitivity", "byMode", "byRes");
        }
    }

    public static class NET_VCA_YARD_TARRY extends Structure {
        public HCNetSDKByJNA.NET_VCA_POLYGON struRegion = new HCNetSDKByJNA.NET_VCA_POLYGON();
        public short wDelay;
        public byte[] byRes = new byte[6];

        public NET_VCA_YARD_TARRY(Pointer p) {
            super(p);
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struRegion", "wDelay", "byRes");
        }
    }

    public static class arrayCardRightPlan extends Structure {
        public byte[] byDoorRightPlan = new byte[4];

        public arrayCardRightPlan() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byDoorRightPlan");
        }
    }

    public static class arrayStruPlanCfg extends Structure {
        public HCNetSDKByJNA.NET_DVR_SINGLE_PLAN_SEGMENT[] struDaysPlanCfg = (HCNetSDKByJNA.NET_DVR_SINGLE_PLAN_SEGMENT[])(new HCNetSDKByJNA.NET_DVR_SINGLE_PLAN_SEGMENT()).toArray(8);

        public arrayStruPlanCfg() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("struDaysPlanCfg");
        }
    }

    public interface fRemoteConfigCallback extends Callback {
        void invoke(int var1, Pointer var2, int var3, Pointer var4);
    }

    public static class struAlarmChannel extends Structure {
        public int dwAlarmChanNum;

        public struAlarmChannel() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwAlarmChanNum");
        }
    }

    public static class struAlarmHardDisk extends Structure {
        public int dwAlarmHardDiskNum;

        public struAlarmHardDisk() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwAlarmHardDiskNum");
        }
    }

    public static class struIOAlarm extends Structure {
        public int dwAlarmInputNo;
        public int dwTrigerAlarmOutNum;
        public int dwTrigerRecordChanNum;

        public struIOAlarm() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwAlarmInputNo", "dwTrigerAlarmOutNum", "dwTrigerRecordChanNum");
        }
    }

    public static class struRecordingHost extends Structure {
        public byte bySubAlarmType;
        public byte[] byRes1 = new byte[3];
        public HCNetSDKByJNA.NET_DVR_TIME_EX struRecordEndTime = new HCNetSDKByJNA.NET_DVR_TIME_EX();
        public byte[] byRes = new byte[116];

        public struRecordingHost() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("bySubAlarmType", "byRes1", "struRecordEndTime", "byRes");
        }
    }

    public static class struStartFrame extends Structure {
        public int dwRelativeTime;
        public int dwAbsTime;
        public byte[] byRes = new byte[92];

        public struStartFrame() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("dwRelativeTime", "dwAbsTime", "byRes");
        }
    }

    public static class struStartTime extends Structure {
        public HCNetSDKByJNA.NET_DVR_TIME tmStart = new HCNetSDKByJNA.NET_DVR_TIME();
        public HCNetSDKByJNA.NET_DVR_TIME tmEnd = new HCNetSDKByJNA.NET_DVR_TIME();
        public byte[] byRes = new byte[92];

        public struStartTime() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("tmStart", "tmEnd", "byRes");
        }
    }

    public static class tagNET_DVR_TIME_EX extends Structure {
        public short wYear;
        public byte byMonth;
        public byte byDay;
        public byte byHour;
        public byte byMinute;
        public byte bySecond;
        public byte byRes;

        public tagNET_DVR_TIME_EX() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("wYear", "byMonth", "byDay", "byHour", "byMinute", "bySecond", "byRes");
        }
    }

    public static class tagNET_DVR_VALID_PERIOD_CFG extends Structure {
        public byte byEnable;
        public byte byBeginTimeFlag;
        public byte byEnableTimeFlag;
        public byte byTimeDurationNo;
        public HCNetSDKByJNA.tagNET_DVR_TIME_EX struBeginTime;
        public HCNetSDKByJNA.tagNET_DVR_TIME_EX struEndTime;
        public byte[] byRes2 = new byte[32];

        public tagNET_DVR_VALID_PERIOD_CFG() {
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("byEnable", "byBeginTimeFlag", "byEnableTimeFlag", "byTimeDurationNo", "struBeginTime", "struEndTime", "byRes2");
        }
    }

    public static class uStruAlarm extends Union {
        public byte[] byUnionLen = new byte[128];
        public HCNetSDKByJNA.struIOAlarm struioAlarm = new HCNetSDKByJNA.struIOAlarm();
        public HCNetSDKByJNA.struAlarmHardDisk strualarmHardDisk = new HCNetSDKByJNA.struAlarmHardDisk();
        public HCNetSDKByJNA.struAlarmChannel sstrualarmChannel = new HCNetSDKByJNA.struAlarmChannel();
        public HCNetSDKByJNA.struRecordingHost strurecordingHost = new HCNetSDKByJNA.struRecordingHost();

        public uStruAlarm() {
        }
    }

    public static class unionRegion extends Union {
        public HCNetSDKByJNA.NET_VCA_RECT struRect = new HCNetSDKByJNA.NET_VCA_RECT();
        public HCNetSDKByJNA.NET_ITC_POLYGON struPolygon = new HCNetSDKByJNA.NET_ITC_POLYGON();

        public unionRegion() {
        }
    }

    public static class unionStartModeParam extends Union {
        public HCNetSDKByJNA.struStartFrame struStartFrame = new HCNetSDKByJNA.struStartFrame();
        public HCNetSDKByJNA.struStartTime struStartTime = new HCNetSDKByJNA.struStartTime();

        public unionStartModeParam() {
        }
    }
}
