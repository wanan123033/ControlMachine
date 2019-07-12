package com.feipulai.exam.config;

/**
 * Created by James on 2017/12/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SharedPrefsConfigs {

    public static final String DEFAULT_PREFS = "host_prefs";

//    public static final String HOST_ID = "host_id";
//    public static final String REAL_TIME_UPLOAD = "real_time_upload";
//    public static final String GRADE_BROADCAST = "grade_broadcast";
//    public static final String AUTO_PRINT = "auto_print";

    public static final String MACHINE_CODE = "machine_code";
    public static final String BRIGHTNESS = "brightness";

    public static final String TIME_WAIT_FOR_TEST = "time_wait_for_test";
    public static final String TIME_WAIT_AFTER_TEST = "time_wait_after_test";

    public static final String ITEM_CODE = "item_code";
    public static final String IP_ADDRESS = "IP_ADDRESS";
    public static final String TOKEN = "TOKEN";
    public static final String UPLOAD_DATE = "UPLOAD_DATE";
    public static final String PUSH_UP_TEST_TIME = "PUSH_UP_TEST_TIME";
    public static final String PUSH_UP_TEST_NUMBER = "PUSH_UP_TEST_NUMBER";
    public static final String PUSH_UP_AUTO_PAIR = "PUSH_UP_AUTO_PAIR";

    // JUMP ROPE
    //public static final String JUMP_ROPE_AUTO_PAIR = "JUMP_ROPE_AUTO_PAIR";
    //public static final String JUMP_ROPE_TEST_NUMBER = "JUMP_ROPE_TEST_NUMBER";
    //public static final String JUMP_ROPE_TEST_TIME = "JUMP_ROPE_TEST_TIME";
    //public static final String JUMP_ROPE_CURRENT_ROPE_GROUP = "JUMP_ROPE_CURRENT_ROPE_GROUP";
    //public static final String JUMP_ROPE_TEST_TIMES = "JUMP_ROPE_TEST_TIMES";
    //public static final String JUMP_ROPE_FULL_SKIP = "JUMP_ROPE_FULL_SKIP";
    //public static final String JUMP_ROPE_MALE_FULL_MARK = "JUMP_ROPE_MALE_FULL_MARK";
    //public static final String JUMP_ROPE_FEMALE_FULL_MARK = "JUMP_ROPE_FEMALE_FULL_MARK";
    //public static final String JUMP_GROUP_MODE = "JUMP_GROUP_MODE";

    // SIT UP
    public static final String SIT_UP_GROUP_MODE = "SIT_UP_GROUP_MODE";
    public static final String SIT_UP_TEST_TIME = "SIT_UP_TEST_TIME";
    public static final String SIT_UP_TEST_NUMBER = "SIT_UP_TEST_NUMBER";
    public static final String SIT_UP_FULL_SKIP = "SIT_UP_FULL_SKIP";
    public static final String SIT_UP_MALE_FULL_MARK = "SIT_UP_MALE_FULL_MARK";
    public static final String SIT_UP_FEMALE_FULL_MARK = "SIT_UP_FEMALE_FULL_MARK";
    public static final String SIT_UP_TEST_TIMES = "SIT_UP_TEST_TIMES";
    public static final String SIT_UP_AUTO_PAIR = "SIT_UP_AUTO_PAIR";
    public static final String SIT_UP_PENALTY = "SIT_UP_PENALTY";

    // PULL UP
    public static final String PULL_UP_TEST_NUMBER = "PULL_UP_TEST_NUMBER";
    public static final String PULL_UP_GROUP_MODE = "PULL_UP_GROUP_MODE";
    public static final String PULL_UP_TEST_TIME = "PULL_UP_TEST_TIME";
    public static final String PULL_UP_FULL_SKIP = "PULL_UP_FULL_SKIP";
    public static final String PULL_UP_MALE_FULL_MARK = "PULL_UP_MALE_FULL_MARK";
    public static final String PULL_UP_FEMALE_FULL_MARK = "PULL_UP_FEMALE_FULL_MARK";
    public static final String PULL_UP_TEST_TIMES = "PULL_UP_TEST_TIMES";
    public static final String PULL_UP_AUTO_PAIR = "PULL_UP_AUTO_PAIR";
    public static final String PULL_UP_PENALTY = "PULL_UP_PENALTY";

    //    public static final String TEST_NAME = "TEST_NAME";
//    public static final String TEST_SITE = "TEST_SITE";

    // 肺活量设备数量
    public static final String VITAL_CAPACITY_TEST_NUMBER = "VITAL_CAPACITY_TEST_NUMBER";
    //实心球
//    public static final String MEDICINE_BALL_TEST_TIMES = "MEDICINE_BALL_TEST_TIMES";
//    public static final String MEDICINE_BALL_TEST_WAY  = "MEDICINE_BALL_TEST_WAY";
//    public static final String MEDICINE_BALL_FULL_RETURN =  "MEDICINE_BALL_FULL_RETURN";

    //50m跑
//    public static final String RUN_TIMER_DEGREE = "RUN_TIMER_DEGREE";
//    public static final String RUN_TIMER_TEST_TIMES = "RUN_TIMER_TEST_TIMES";
//    public static final String RUN_TIMER_INTERCEPT = "RUN_TIMER_INTERCEPT";
//    public static final String RUN_TIMER_RUN_WAY = "RUN_TIMER_INTERCEPT";
//    public static final String RUN_TIMER_MARK = "RUN_TIMER_MARK";
//    public static final String RUN_TIMER_POINT = "RUN_TIMER_POINT";
//    public static final String RUN_TIMER_TEST_WAY = "RUN_TIMER_TEST_WAY";
//    public static final String RUN_TIMER_FULL_RETURN =  "RUN_TIMER_FULL_RETURN";

    public static final int DEFAULT_MACHINE_CODE = 0;

    public static final String DEFAULT_SERVER_TOKEN = "DEFAULT_SERVER_TOKEN";

    //中长跑参数设置
    public static final String MIDDLE_RACE = "middle_race";
    public static final String MIDDLE_RACE_NUMBER = "middle_race_number";//计时器数量
    public static final String MIDDLE_RACE_TIME_FIRST = "middle_race_time_first";//首次接收时间/秒
    public static final String MIDDLE_RACE_TIME_SPAN = "middle_race_time_span";//最小时间间隔/秒

    public static final String MIDDLE_RACE_CARRY = "middle_race_carry";//成绩进位方式
    public static final String MIDDLE_RACE_DIGITAL = "middle_race_digital";//成绩精确位数
//    public static final String MIDDLE_RACE_ROUNDING = "middle_race_rounding";//成绩进位方式 四舍五入 0
//    public static final String MIDDLE_RACE_NON_ZERO_INTEGER = "middle_race_non_zero_integer";//成绩进位方式 非零取整1
//    public static final String MIDDLE_RACE_NON_ZERO_CARRY = "middle_race_non_zero_carry";//成绩进位方式 非零进位2

    public static final int FIRST_TIME = 10;//首次接收时间/秒
    public static final int SPAN_TIME = 10;//最小时间间隔/秒
    public static final String VEST_CHIP_NO = "vest_chip_no";//背心芯片数

    public static final String MACHINE_IP = "machine_ip";//连接设备ip
    public static final String MACHINE_PORT = "machine_port";//连接设备端口
}