package com.feipulai.host.config;

import android.content.Context;
import android.content.DialogInterface;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.height_weight.HeightWeightCheckActivity;
import com.feipulai.host.activity.jump_rope.check.JumpRopeCheckActivity;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.host.activity.medicine_ball.MedicineBallSelectActivity;
import com.feipulai.host.activity.medicine_ball.MedicineBallSetting;
import com.feipulai.host.activity.pullup.PullUpSelectActivity;
import com.feipulai.host.activity.pullup.setting.PullUpSetting;
import com.feipulai.host.activity.radio_timer.RunTimerFreeTestActivity;
import com.feipulai.host.activity.radio_timer.RunTimerSetting;
import com.feipulai.host.activity.radio_timer.RunTimerTestActivity;
import com.feipulai.host.activity.ranger.RangerPersonTestActivity;
import com.feipulai.host.activity.ranger.RangerTestActivity;
import com.feipulai.host.activity.sitreach.SitReachSelectActivity;
import com.feipulai.host.activity.sitreach.SitReachSetting;
import com.feipulai.host.activity.situp.check.SitUpCheckActivity;
import com.feipulai.host.activity.situp.setting.SitUpSetting;
import com.feipulai.host.activity.sporttime.SportTimeActivity;
import com.feipulai.host.activity.sporttime.SportTimerSetting;
import com.feipulai.host.activity.standjump.StandJumpSelectActivity;
import com.feipulai.host.activity.standjump.StandJumpSetting;
import com.feipulai.host.activity.vccheck.VitalTestActivity;
import com.feipulai.host.activity.vision.Radio.VisionCheckActivity;
import com.feipulai.host.activity.vision.Radio.VisionTestActivity;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.db.MachineItemCodeUtil;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.view.ItemDecideDialogBuilder;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2018/1/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TestConfigs {

    public static final int CHECK_IN_INPUT = 0;
    public static final int CHECK_IN_IDCARD = 1;
    public static final int CHECK_IN_BARCODE = 2;
    public static final int CHECK_IN_ICCARD = 3;
    public static final int UPDATE_GRIDVIEW = 0x4;
    public static final int GROUP_PATTERN_SUCCESIVE = 0x0;

    public static final String DEFAULT_IP_ADDRESS = "192.168.0.100:8099";
    public static final Map<Integer, Class<?>> proActivity = new HashMap<>();
    public static final Map<Integer, Class<?>> freedomActivity = new HashMap<>();
    public static final Map<Integer, String> machineNameMap = new HashMap<>();


    public static final String DEFAULT_ITEM_CODE = "default";
    public static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 身高体重的项目代码是固定的
    public static final String HEIGHT_ITEM_CODE = "E01";
    public static final String WEIGHT_ITEM_CODE = "E02";
    //项目默认取值范围
    public static final Map<Integer, Integer> itemMinScope = new HashMap<>();
    public static final Map<Integer, Integer> itemMaxScope = new HashMap<>();

    static {
        // 每个项目对应的检录Activity
        TestConfigs.proActivity.put(ItemDefault.CODE_TS, JumpRopeCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_YWQZ, SitUpCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_HW, HeightWeightCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_SGBQS, SitUpCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_FHL, VitalTestActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_ZWTQQ, SitReachSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_LDTY, StandJumpSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_HWSXQ, MedicineBallSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_ZFP, RunTimerTestActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_WLJ, VitalTestActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_YTXS, PullUpSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_SL, VisionCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_JGCJ, RangerPersonTestActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_SPORT_TIMER, SportTimeActivity.class);

        TestConfigs.freedomActivity.put(ItemDefault.CODE_SL, VisionTestActivity.class);
        TestConfigs.freedomActivity.put(ItemDefault.CODE_JGCJ, RangerTestActivity.class);
        TestConfigs.freedomActivity.put(ItemDefault.CODE_LDTY, StandJumpSelectActivity.class);
        TestConfigs.freedomActivity.put(ItemDefault.CODE_HWSXQ, MedicineBallSelectActivity.class);
        TestConfigs.freedomActivity.put(ItemDefault.CODE_ZFP, RunTimerFreeTestActivity.class);
        // 每个机器码对应的机器名称
        TestConfigs.machineNameMap.put(ItemDefault.CODE_TS, MyApplication.getInstance().getString(R.string.jump_rope));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_YWQZ, MyApplication.getInstance().getString(R.string.sit_up));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SGBQS, "双杠臂屈伸");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_HW, MyApplication.getInstance().getString(R.string.height_weight));

        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZWTQQ, MyApplication.getInstance().getString(R.string.sit_reach));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_LDTY, MyApplication.getInstance().getString(R.string.stand_jump));

        TestConfigs.machineNameMap.put(ItemDefault.CODE_FHL, MyApplication.getInstance().getString(R.string.vital_capacity));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_HWSXQ, MyApplication.getInstance().getString(R.string.medicine_ball));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZFP, MyApplication.getInstance().getString(R.string.run_time));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_WLJ, MyApplication.getInstance().getString(R.string.grip_meter));
        TestConfigs.machineNameMap.put(ItemDefault.CODE_YTXS, "引体向上");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_JGCJ, "激光测距");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SL, "视力");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SPORT_TIMER, "运动计时");

        // 每个机器码对应的机器名称
        TestConfigs.itemMinScope.put(ItemDefault.CODE_ZWTQQ, -200);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_ZWTQQ, 400);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_LDTY, 510);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_LDTY, 3420);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_HWSXQ, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_HWSXQ, 50000);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_TS, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_TS, 999);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_YTXS, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_YTXS, 999);
    }

    public static Item sCurrentItem;

    // /**
    //  * @deprecated
    //  * 初始化{@link #sCurrentItem},表明当前测试项目
    //  *
    //  * 此方法调用完成后:
    //  * 1. 如果{@link #sCurrentItem}为null,表示未进行过项目选择,所有均为默认的;
    //  * 2. 如果{@link #sCurrentItem}不为null,但是如果{#sCurrentItem.getItemCode()}为null,
    //  * 表示传入的itemCode为null,已选择机器,之前项目代码选择时,因为没有项目信息,没有进行过选择,项目代码为默认;此时有两种情况:
    //  *      2.1 项目代码没有更新,不处理;
    //  *      2.2 项目代码已经更新,但是本地设置项没有更新,需要更新报名信息和成绩信息的itemCode,更新需要依据下列情况处理:
    //  *          2.2.1 如果更新的机器码对应的项目只有1个,直接将报名信息和成绩信息中itemCode为default的代码更新即可;
    //  *          2.2.2 如果更新的机器码对应的项目不止1个,弹框让用户选择并更新;
    //  * 3. 如果{@link #sCurrentItem}不为null,且{#sCurrentItem.getItemCode()}不为null,
    //  * 这时需要考虑之前是否有没更新完成的成绩信息等数据,需要做上述   2.2.1(因为用户已经选择过了项目代码,这里直接使用这个项目代码就是) 的步骤
    //  *
    //  * 身高体重永远不会出现项目代码为null的情况,其项目代码为固定的
    //  *
    //  * @param machineCode 机器码,必须要有
    //  * @param itemCode    项目代码  如果没有,传入null即可,如果传入itemCode为null  生成的sCurrentItem的itemCode为传入的itemCode
    //  */
    // public static void init(int machineCode,String itemCode){
    // 	// 项目还没有选择,不处理;保证sCurrentItem为null
    // 	if(machineCode == SharedPrefsConfigs.DEFAULT_MACHINE_CODE){
    // 		return;
    // 	}
    // 	// 身高体重特殊处理,设置itemcode为身高项目
    // 	if(machineCode == ItemDefault.CODE_HW){
    // 		sCurrentItem = DBManager.getInstance().queryItemByItemCode(TestConfigs.HEIGHT_ITEM_CODE);
    // 	}else{
    // 		// 这里必须保证传入的machineCode和itemCode是匹配的:
    // 		// 在每次切换项目时,及时更新着两个参数,并且保存配置
    // 		if(itemCode == null){
    // 			List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
    // 			sCurrentItem = itemList.get(0);
    // 			sCurrentItem.setItemCode(null);
    // 		}else{
    // 			sCurrentItem = DBManager.getInstance().queryItemByMachineItemCode(machineCode,itemCode);
    // 		}
    // 	}
    // 	Logger.i("sCurrentItem:" + sCurrentItem.toString());
    // }


    public static final int INIT_SUCCESS = 0x1;
    public static final int INIT_NO_MACHINE_CODE = 0x2;
    public static final int INIT_MULTI_ITEM_CODE = 0x3;

    /**
     * 初始化{@link #sCurrentItem},表明当前测试项目
     * 该方法会自动保存机器码和项目代码的设置项
     *
     * @param context  上下文,应为activity的上下文
     * @param listener 有多个 itemCode,在用户选择完成时会回调方法(回调时 sCurrentItem 和成绩相关均已处理,listener中只需处理自身逻辑即可)
     * @return 表示初始化是否成功{@link #INIT_SUCCESS} 及不成功的原因
     * 1. 初次进入应用,没有机器码{@link #INIT_NO_MACHINE_CODE}
     * 2. 原来machineCode没有对应 itemCode ,现在有多个 itemCode {@link #INIT_MULTI_ITEM_CODE},这里弹框让用户选择,在用户选择完成时会调用传入的 listener 的回调方法
     */
    public static int init(final Context context, final int machineCode, String itemCode,
                           final DialogInterface.OnClickListener listener) {
        // 项目还没有选择,不处理
        if (machineCode == SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
            sCurrentItem = null;
            return INIT_NO_MACHINE_CODE;
        }

        // 身高体重特殊处理,设置itemcode为身高项目
        if (machineCode == ItemDefault.CODE_HW) {
            sCurrentItem = DBManager.getInstance().queryItemByItemCode(TestConfigs.HEIGHT_ITEM_CODE);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, TestConfigs.HEIGHT_ITEM_CODE);
            Logger.i("sCurrentItem:" + sCurrentItem.toString());
            return INIT_SUCCESS;
        }

        final List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault(machineCode);
        final List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault(machineCode);

        if (itemCode != null) {
            Item item = DBManager.getInstance().queryItemByMachineItemCode(machineCode, itemCode);
            if (item == null) {
                // 不存在这样的项目,那么就是 机器码改了,项目代码没有改,这里直接改掉
                itemCode = null;
            } else {
                sCurrentItem = item;
                // 有项目代码,证明同步过项目信息,这时候要看成绩是否也同步了过来,这种就是之前的某次这样的操作没有完成而已,这里继续完成就是
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, itemCode);
                Logger.i("sCurrentItem:" + sCurrentItem.toString());
                return INIT_SUCCESS;
            }
        }

        final List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
        Logger.e("machineCode=" + machineCode + "----" + itemList);
        String newItemCode = itemList.get(0).getItemCode();
//        // 还是没有 itemCode
//        if (newItemCode == null) {
//            sCurrentItem = itemList.get(0);
//            Logger.i("sCurrentItem:" + sCurrentItem.toString());
//            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
//            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
//            return INIT_SUCCESS;
//        }

        // 项目代码已更新
        // 如果当前机器码只测一个项目,直接把学生项目报名信息和成绩信息中的itemCode改掉即可
        if (itemList.size() == 1) {
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, newItemCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
            sCurrentItem = itemList.get(0);
            Logger.i("sCurrentItem:" + sCurrentItem.toString());
            return INIT_SUCCESS;
        }

        sCurrentItem = null;
        // 不止一个,弹框让用户选择
        new ItemDecideDialogBuilder(context, itemList, "请选择测试项目(该项会被应用至当前机器所有已测成绩)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sCurrentItem = itemList.get(which);
                String newItemCode = itemList.get(which).getItemCode();
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, newItemCode);
                SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
                SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(dialog, which);

                }
            }
        }).show();
        return INIT_MULTI_ITEM_CODE;
    }

    public static String getCurrentItemCode() {
        return sCurrentItem.getItemCode() == null ? DEFAULT_ITEM_CODE :
                sCurrentItem.getItemCode();
    }

    public static String getItemCode(Item item) {
        return item.getItemCode() == null ? DEFAULT_ITEM_CODE :
                item.getItemCode();
    }
    public static int getMaxTestCount(Context context) {
        int result = TestConfigs.sCurrentItem.getTestNum();
        if (result > 0) {
            return result;
        }
        int machineCode = TestConfigs.sCurrentItem.getMachineCode();
        switch (machineCode) {
            case ItemDefault.CODE_SPORT_TIMER:
                result = SharedPrefsUtil.loadFormSource(context, SportTimerSetting.class).getTestTimes();
                break;

            default:
                throw new IllegalArgumentException("wrong machine code");
        }
        return result;
    }
    public static int getMaxTestCount() {
        return getMaxTestCount(MyApplication.getInstance());
    }

}
