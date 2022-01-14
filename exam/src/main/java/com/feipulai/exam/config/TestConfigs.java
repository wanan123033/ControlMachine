package com.feipulai.exam.config;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForGroupActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForPersonActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleRaceSettingActivity;
import com.feipulai.exam.activity.RadioTimer.RunTimerActivityGroupActivity;
import com.feipulai.exam.activity.RadioTimer.RunTimerActivityTestActivity;
import com.feipulai.exam.activity.RadioTimer.RunTimerSelectActivity;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.RadioTimer.RunTimerSettingActivity;
import com.feipulai.exam.activity.basketball.BasketBallGroupActivity;
import com.feipulai.exam.activity.basketball.BasketBallSelectActivity;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.BasketBallSettingActivity;
import com.feipulai.exam.activity.basketball.BasketBallShootActivity;
import com.feipulai.exam.activity.basketball.BasketBallShootGroupActivity;
import com.feipulai.exam.activity.basketball.ShootSetting;
import com.feipulai.exam.activity.basketball.ShootSettingActivity;
import com.feipulai.exam.activity.footBall.FootBallGroupActivity;
import com.feipulai.exam.activity.footBall.FootBallItemSelectActivity;
import com.feipulai.exam.activity.footBall.FootBallSetting;
import com.feipulai.exam.activity.footBall.FootBallSettingActivity;
import com.feipulai.exam.activity.grip.GripMoreActivity;
import com.feipulai.exam.activity.grip.GripMoreGroupActivity;
import com.feipulai.exam.activity.grip.GripSetting;
import com.feipulai.exam.activity.grip.GripSettingActivity;
import com.feipulai.exam.activity.jump_rope.check.JumpRopeCheckActivity;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSettingActivity;
import com.feipulai.exam.activity.medicineBall.MedicineBallGroupActivity;
import com.feipulai.exam.activity.medicineBall.MedicineBallSelectActivity;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.medicineBall.MedicineBallSettingActivity;
import com.feipulai.exam.activity.pullup.PullUpSelectActivity;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.pullup.setting.PullUpSettingActivity;
import com.feipulai.exam.activity.pushUp.PushPatternSelectActivity;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.pushUp.PushUpSettingActivity;
import com.feipulai.exam.activity.pushUp.check.PushUpCheckActivity;
import com.feipulai.exam.activity.ranger.RangerMoreActivity;
import com.feipulai.exam.activity.ranger.RangerSettingActivity;
import com.feipulai.exam.activity.ranger.RangerTestActivity;
import com.feipulai.exam.activity.sargent_jump.SargentGroupActivity;
import com.feipulai.exam.activity.sargent_jump.SargentItemSelectActivity;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.sargent_jump.SargentSettingActivity;
import com.feipulai.exam.activity.sitreach.SitReachGroupTestActivity;
import com.feipulai.exam.activity.sitreach.SitReachSelectActivity;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.sitreach.SitReachSettingActivity;
import com.feipulai.exam.activity.situp.check.SitUpCheckActivity;
import com.feipulai.exam.activity.situp.check.SitUpPatternSelectActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.situp.setting.SitUpSettingActivity;
import com.feipulai.exam.activity.sport_timer.SportSettingActivity;
import com.feipulai.exam.activity.sport_timer.SportTimerActivity;
import com.feipulai.exam.activity.sport_timer.SportTimerGroupActivity;
import com.feipulai.exam.activity.sport_timer.bean.SportTimerSetting;
import com.feipulai.exam.activity.standjump.StandJumpGroupTestActivity;
import com.feipulai.exam.activity.standjump.StandJumpSelectActivity;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSettingActivity;
import com.feipulai.exam.activity.volleyball.VolleyBallGroupActivity;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSettingActivity;
import com.feipulai.exam.activity.volleyball.VolleyballPatternSelectActivity;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.db.MachineItemCodeUtil;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.ItemDecideDialogBuilder;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pengjf on 2018/11/19.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TestConfigs {

    public static final int CHECK_IN_INPUT = 0;
    public static final int CHECK_IN_IDCARD = 1;
    public static final int CHECK_IN_BARCODE = 2;
    public static final int CHECK_IN_ICCARD = 3;

    public static final int UPDATE_GRIDVIEW = 0x4;
    public static final int MAX_TEST_NO = 3;
    public static final String DEFAULT_IP_ADDRESS = "192.168.1.100:7979";
    public static final Map<Integer, Class<?>> proActivity = new HashMap<>();
    public static final Map<Integer, Class<?>> groupActivity = new HashMap<>();
    public static final List<Integer> selectActivity = new ArrayList<>();
    public static final Map<Integer, String> machineNameMap = new HashMap<>();

    public static final String DEFAULT_ITEM_CODE = "default";
    public static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Map<Integer, Class<?>> settingActivity = new HashMap<>();
    // 身高体重的项目代码是固定的
    public static final String HEIGHT_ITEM_CODE = "E01";
    public static final String WEIGHT_ITEM_CODE = "E02";
    public static final Map<Object, Object> baseGroupMap = new HashMap<>();

    public static final int GROUP_PATTERN_SUCCESIVE = 0x0;  //连续模式
    public static final int GROUP_PATTERN_LOOP = 0x1;   //循环模式

    //项目默认取值范围
    public static final Map<Integer, Integer> itemMinScope = new HashMap<>();
    public static final Map<Integer, Integer> itemMaxScope = new HashMap<>();
    /**
     * 当前项目
     * 如果为null,表示当前未选择项目;
     */
    public static Item sCurrentItem;

//     /**
//      * 初始化{@link #sCurrentItem},表明当前测试项目
//      * <p>
//      * 此方法调用完成后:
//      * 1. 如果{@link #sCurrentItem}为null,表示未进行过项目选择,所有均为默认的;
//      * 2. 如果{@link #sCurrentItem}不为null,但是如果{@link # sCurrentItem.getItemCode()}为null,
//      * 表示传入的itemCode为null,已选择机器,之前项目代码选择时,因为没有项目信息,没有进行过选择,项目代码为默认;此时有两种情况:
//      * 2.1 项目代码没有更新,不处理;
//      * 2.2 项目代码已经更新,但是本地设置项没有更新,需要更新报名信息和成绩信息的itemCode,更新需要依据下列情况处理:
//      * 2.2.1 如果更新的机器码对应的项目只有1个,直接将报名信息和成绩信息中itemCode为default的代码更新即可;
//      * 2.2.2 如果更新的机器码对应的项目不止1个,弹框让用户选择并更新;
//      * 3. 如果{@link #sCurrentItem}不为null,且{@link # sCurrentItem.getItemCode()}不为null,
//      * 这时需要考虑之前是否有没更新完成的成绩信息等数据,需要做上述   2.2.1(因为用户已经选择过了项目代码,这里直接使用这个项目代码就是) 的步骤
//      * <p>
//      * 身高体重永远不会出现项目代码为null的情况,其项目代码为固定的
//      *
//      * @param machineCode 机器码,必须要有
//      * @param itemCode    项目代码  如果没有,传入null即可,如果传入itemCode为null  生成的sCurrentItem的itemCode为传入的itemCode
//      */
//     public static void init(int machineCode, String itemCode) {
//         Logger.i("machineCode:" + machineCode + "\titemCode:" + itemCode);
//         // 项目还没有选择,不处理;保证sCurrentItem为null
//         if (machineCode == SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
//             return;
//         }
//         // 身高体重特殊处理,设置itemcode为身高项目
// //        if (machineCode == ItemDefault.CODE_HW) {
// //            sCurrentItem = DBManager.getInstance().queryItemByItemCode(TestConfigs.HEIGHT_ITEM_CODE);
// //        } else {
//         // 这里必须保证传入的machineCode和itemCode是匹配的:
//         // 在每次切换项目时,及时更新着两个参数,并且保存配置
//         if (itemCode == null) {
//             List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
//             sCurrentItem = itemList.get(0);
//             sCurrentItem.setItemCode(null);
//         } else {
//             // 这里必须保证传入的machineCode和itemCode是匹配的:
//             // 在每次切换项目时,及时更新着两个参数,并且保存配置
//             if (itemCode == null) {
//                 List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
//                 sCurrentItem = itemList.get(0);
//                 sCurrentItem.setItemCode(null);
//             } else {
//                 sCurrentItem = DBManager.getInstance().queryItemByMachineItemCode(machineCode, itemCode);
//             }
//         }
// //        }
//         MachineCode.machineCode = machineCode;
// //        Logger.i("sCurrentItem:" + sCurrentItem.toString());
//     }

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
        MachineCode.machineCode = machineCode;
        final List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault(machineCode);
        final List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault(machineCode);
        final List<MachineResult> machineResults = DBManager.getInstance().queryMachineResultByItemCodeDefault(machineCode);
        if (itemCode != null) {
            Item item = DBManager.getInstance().queryItemByMachineItemCode(machineCode, itemCode);
            if (item == null) {
                // 不存在这样的项目,那么就是 机器码改了,项目代码没有改,这里直接改掉
                itemCode = null;
            } else {
                sCurrentItem = item;
                // 有项目代码,证明同步过项目信息,这时候要看成绩是否也同步了过来,这种就是之前的某次这样的操作没有完成而已,这里继续完成就是
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, itemCode);
                LogUtils.operation("当前选择项目:" + sCurrentItem.toString());
                return INIT_SUCCESS;
            }
        }

        final List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);

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
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, newItemCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
            sCurrentItem = itemList.get(0);
            LogUtils.operation("当前选择项目:" + sCurrentItem.toString());

            return INIT_SUCCESS;

        }
        if (machineCode == ItemDefault.CODE_ZCP) {
            sCurrentItem = itemList.get(0);
            newItemCode = itemList.get(0).getItemCode();
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, newItemCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
            LogUtils.operation("当前选择项目:" + sCurrentItem.toString());
            return INIT_SUCCESS;
        }
        sCurrentItem = null;
        // 不止一个,弹框让用户选择
        new ItemDecideDialogBuilder(context, itemList, "请选择测试项目(该项会被应用至当前机器所有已测成绩)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sCurrentItem = itemList.get(which);
                String newItemCode = itemList.get(which).getItemCode();
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, newItemCode);
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

    public static boolean initZCP(List<Item> itemList) {
        for (Item item : itemList) {
            if (item.getMachineCode() != ItemDefault.CODE_ZCP) {
                continue;
            }
            Item codeItem = DBManager.getInstance().queryItemByCode(item.getItemCode());
            Item nameItem = DBManager.getInstance().queryItemByName(item.getItemName());

            if (codeItem != null && nameItem != null) {
                if (!TextUtils.equals(codeItem.getItemName(), nameItem.getItemName())) {
                    ToastUtils.showShort("导入失败,导入项目代码已存在,拒绝导入");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " 导入失败,导入项目代码已存在,拒绝导入");
                    return false;
                } else if (codeItem.getMachineCode() != ItemDefault.CODE_ZCP || nameItem.getMachineCode() != ItemDefault.CODE_ZCP) {
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " 导入失败,导入项目机器码错误,拒绝导入");
                    ToastUtils.showShort("导入失败,导入项目机器码错误,拒绝导入");
                    return false;
                }
            } else if (codeItem == null && nameItem == null) {
                DBManager.getInstance().insertItem(ItemDefault.CODE_ZCP, item.getItemCode()
                        , item.getItemName(), "分'秒", DBManager.TEST_TYPE_TIME);
            } else if (codeItem != null && nameItem == null) {
                if (codeItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    codeItem.setItemName(item.getItemName());
                    DBManager.getInstance().updateItem(codeItem);// 更新项目表中信息
                } else {
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " 导入失败,导入项目机器码错误,拒绝导入");
                    ToastUtils.showShort("导入失败,导入项目机器码错误,拒绝导入");
                    return false;
                }
            } else if (codeItem == null && nameItem != null) {
                if (nameItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    updateItemFillAll(nameItem, item.getItemCode());
                    nameItem.setItemCode(item.getItemCode());
                    DBManager.getInstance().updateItem(nameItem);// 更新项目表中信息
                } else {
                    ToastUtils.showShort("导入失败,导入项目机器码错误,拒绝导入");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " 导入失败,导入项目机器码错误,拒绝导入");
                    return false;
                }

            }
        }
        return true;
    }

    public static void updateItemFillAll(Item item, String updateItemCode) {
        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCode(item.getItemCode());
        List<StudentItem> studentItems = DBManager.getInstance().querystuItemsByMachineItemCode(item.getMachineCode(), item.getItemCode());
        List<GroupItem> groupItemList = DBManager.getInstance().queryGroupItemByItemCode(item.getItemCode());
        List<Group> groupList = DBManager.getInstance().queryGroupByItemCode(item.getItemCode());
        List<MachineResult> machineResultList = DBManager.getInstance().getMachineResultByItemCode(item.getItemCode());
        List<ItemSchedule> itemSchedules = DBManager.getInstance().queryItemSchedulesByItemCode(item.getItemCode());

        if (itemSchedules != null && itemSchedules.size() > 0) {
            for (ItemSchedule itemSchedule : itemSchedules
            ) {
                itemSchedule.setItemCode(updateItemCode);
            }
            DBManager.getInstance().deleteSchedules(itemSchedules);
            DBManager.getInstance().insertItemSchedulesList(itemSchedules);
        }

        if (roundResults != null && roundResults.size() > 0) {
            for (RoundResult roundResult : roundResults) {
                roundResult.setItemCode(updateItemCode);
            }
            DBManager.getInstance().updateRoundResult(roundResults);
        }
        if (studentItems != null && studentItems.size() > 0) {
            for (StudentItem studentItem : studentItems) {
                studentItem.setItemCode(updateItemCode);
            }
            DBManager.getInstance().updateStudentItem(studentItems);
        }

        if (groupItemList != null && groupItemList.size() > 0) {
            for (GroupItem groupItem : groupItemList) {
                groupItem.setItemCode(updateItemCode);
            }
            DBManager.getInstance().updateStudentGroupItems(groupItemList);
        }

        if (groupList != null && groupList.size() > 0) {
            for (Group group : groupList) {
                group.setItemCode(updateItemCode);
            }
            DBManager.getInstance().updateGroups(groupList);
        }

        if (machineResultList != null && machineResultList.size() > 0) {
            for (MachineResult machineResult : machineResultList) {
                machineResult.setItemCode(updateItemCode);
            }
            DBManager.getInstance().updateMachineResults(machineResultList);
        }
    }

    static {
        selectActivity.add(ItemDefault.CODE_LDTY);
        selectActivity.add(ItemDefault.CODE_FWC);
        selectActivity.add(ItemDefault.CODE_MG);
        selectActivity.add(ItemDefault.CODE_PQ);
        selectActivity.add(ItemDefault.CODE_LQYQ);
        selectActivity.add(ItemDefault.CODE_HWSXQ);
        selectActivity.add(ItemDefault.CODE_ZQYQ);
        selectActivity.add(ItemDefault.CODE_ZWTQQ);
        selectActivity.add(ItemDefault.CODE_ZFP);
//        selectActivity.add(ItemDefault.CODE_YWQZ);


        // 每个项目对应的个人检录Activity
        TestConfigs.proActivity.put(ItemDefault.CODE_LDTY, StandJumpSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_ZWTQQ, SitReachSelectActivity.class);

        TestConfigs.proActivity.put(ItemDefault.CODE_ZFP, RunTimerSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_HWSXQ, MedicineBallSelectActivity.class);

        TestConfigs.proActivity.put(ItemDefault.CODE_TS, JumpRopeCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_YWQZ, SitUpCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_SGBQS, SitUpCheckActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_YTXS, PullUpSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_PQ, VolleyballPatternSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_MG, SargentItemSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_FWC, PushPatternSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_LQYQ, BasketBallSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_ZCP, MiddleDistanceRaceForPersonActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_ZQYQ, FootBallItemSelectActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_JGCJ, RangerTestActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_WLJ, GripMoreActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_SHOOT, BasketBallShootActivity.class);
        TestConfigs.proActivity.put(ItemDefault.CODE_SPORT_TIMER, SportTimerActivity.class);

        // 每个项目对应的分组检录Acitivity
        TestConfigs.groupActivity.put(ItemDefault.CODE_LDTY, StandJumpGroupTestActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_ZWTQQ, SitReachGroupTestActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_ZFP, RunTimerActivityGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_HWSXQ, MedicineBallGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_TS, JumpRopeCheckActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_YWQZ, SitUpCheckActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_SGBQS, SitUpCheckActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_YTXS, PullUpSelectActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_PQ, VolleyBallGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_MG, SargentGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_FWC, PushUpCheckActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_ZCP, MiddleDistanceRaceForGroupActivity.class);

        TestConfigs.groupActivity.put(ItemDefault.CODE_LQYQ, BasketBallGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_ZQYQ, FootBallGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_JGCJ, RangerMoreActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_WLJ, GripMoreGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_SHOOT, BasketBallShootGroupActivity.class);
        TestConfigs.groupActivity.put(ItemDefault.CODE_SPORT_TIMER, SportTimerGroupActivity.class);

        // 每个机器码对应的机器名称
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZWTQQ, "坐位体前屈");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_LDTY, "立定跳远");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZFP, "红外计时");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_HWSXQ, "红外实心球");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_TS, "跳绳");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_YWQZ, "仰卧起坐");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_YTXS, "引体向上");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_PQ, "排球垫球");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_MG, "摸高");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_FWC, "俯卧撑");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_LQYQ, "篮球运球");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZCP, "中长跑");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZQYQ, "足球运球");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_JGCJ, "激光测距");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_WLJ, "握力");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SHOOT, "篮球投篮");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SPORT_TIMER, "运动计时");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SGBQS, "双杠臂屈伸");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_FHL, "肺活量");
        TestConfigs.settingActivity.put(ItemDefault.CODE_LDTY, StandJumpSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_ZWTQQ, SitReachSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_ZFP, RunTimerSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_HWSXQ, MedicineBallSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_TS, JumpRopeSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_YWQZ, SitUpSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_SGBQS, SitUpSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_YTXS, PullUpSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_PQ, VolleyBallSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_MG, SargentSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_FWC, PushUpSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_ZCP, MiddleRaceSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_LQYQ, BasketBallSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_ZQYQ, FootBallSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_WLJ, GripSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_SHOOT, ShootSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_SPORT_TIMER, SportSettingActivity.class);
        TestConfigs.settingActivity.put(ItemDefault.CODE_JGCJ, RangerSettingActivity.class);
        // 每个机器码对应的机器名称
        TestConfigs.itemMinScope.put(ItemDefault.CODE_ZWTQQ, -200);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_ZWTQQ, 400);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_LDTY, 510);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_LDTY, 3420);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_HWSXQ, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_HWSXQ, 50000);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_TS, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_TS, 999);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_YWQZ, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_YWQZ, 999);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_SGBQS, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_SGBQS, 999);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_YTXS, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_YTXS, 999);
        TestConfigs.itemMinScope.put(ItemDefault.CODE_ZFP, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_ZFP, 360000);
        // todo change
        TestConfigs.itemMinScope.put(ItemDefault.CODE_MG, 0);
        TestConfigs.itemMaxScope.put(ItemDefault.CODE_MG, 3600);
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

            case ItemDefault.CODE_ZWTQQ:
                result = SharedPrefsUtil.loadFormSource(context, SitReachSetting.class).getTestCount();
                break;

            case ItemDefault.CODE_LDTY:
                result = SharedPrefsUtil.loadFormSource(context, StandJumpSetting.class).getTestCount();
                break;

            case ItemDefault.CODE_ZFP:
                result = SharedPrefsUtil.loadFormSource(context, RunTimerSetting.class).getTestTimes();
                break;

            case ItemDefault.CODE_HWSXQ:
                result = SharedPrefsUtil.loadFormSource(context, MedicineBallSetting.class).getTestTimes();
                break;

            case ItemDefault.CODE_TS:
                result = SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class).getTestNo();
                break;

            case ItemDefault.CODE_YWQZ:
            case ItemDefault.CODE_SGBQS:
                result = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class).getTestNo();
                break;

            case ItemDefault.CODE_YTXS:
                result = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class).getTestNo();
                break;

            case ItemDefault.CODE_PQ:
                result = SharedPrefsUtil.loadFormSource(context, VolleyBallSetting.class).getTestNo();
                break;
            case ItemDefault.CODE_FWC:
                result = SharedPrefsUtil.loadFormSource(context, PushUpSetting.class).getTestNo();
                break;
            case ItemDefault.CODE_LQYQ:
                result = SharedPrefsUtil.loadFormSource(context, BasketBallSetting.class).getTestNo();
                break;
            case ItemDefault.CODE_ZQYQ:
                result = SharedPrefsUtil.loadFormSource(context, FootBallSetting.class).getTestNo();
                break;
            case ItemDefault.CODE_ZCP:
                result = 1;
                break;
            case ItemDefault.CODE_SHOOT:
                result = SharedPrefsUtil.loadFormSource(context, ShootSetting.class).getTestNo();
                break;
            case ItemDefault.CODE_MG:
                result = SharedPrefsUtil.loadFormSource(context, SargentSetting.class).getTestTimes();
                break;
            case ItemDefault.CODE_SPORT_TIMER:
                result = SharedPrefsUtil.loadFormSource(context, SportTimerSetting.class).getTestTimes();
                break;
            case ItemDefault.CODE_WLJ:
                result = SharedPrefsUtil.loadFormSource(context, GripSetting.class).getTestRound();
                break;
            default:
                throw new IllegalArgumentException("wrong machine code");
        }
        return result;
    }

    /**
     * 获取是否满分跳过
     *
     * @return
     */
    public static int[] getFullSkip() {
        int code = MachineCode.machineCode;
        int[] full = null;
        switch (code) {
            case ItemDefault.CODE_ZWTQQ:
                SitReachSetting sitReachSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), SitReachSetting.class);
                if (sitReachSetting.isFullReturn()) {
                    full = new int[2];
                    full[0] = (int) (sitReachSetting.getManFull() * 10);
                    full[1] = (int) (sitReachSetting.getWomenFull() * 10);
                }
                break;
            case ItemDefault.CODE_LDTY:
                StandJumpSetting jumpSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), StandJumpSetting.class);
                if (jumpSetting.isFullReturn()) {
                    full = new int[2];
                    full[0] = jumpSetting.getManFull() * 10;
                    full[1] = jumpSetting.getWomenFull() * 10;
                }

                break;
            case ItemDefault.CODE_HWSXQ:
                MedicineBallSetting medicineBallSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), MedicineBallSetting.class);
                if (medicineBallSetting.isFullReturn()) {
                    full = new int[2];
                    if (!TextUtils.isEmpty(medicineBallSetting.getMaleFull())) {
                        full[0] = Integer.parseInt(medicineBallSetting.getMaleFull()) * 10;
                    }
                    if (!TextUtils.isEmpty(medicineBallSetting.getFemaleFull())) {
                        full[1] = Integer.parseInt(medicineBallSetting.getFemaleFull()) * 10;
                    }
                }
                break;
            case ItemDefault.CODE_MG:
                SargentSetting sargentSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), SargentSetting.class);
                if (sargentSetting.isFullReturn()) {
                    full = new int[2];
                    if (!TextUtils.isEmpty(sargentSetting.getMaleFull())) {
                        full[0] = Integer.parseInt(sargentSetting.getMaleFull()) * 10;
                    }
                    if (!TextUtils.isEmpty(sargentSetting.getFemaleFull())) {
                        full[1] = Integer.parseInt(sargentSetting.getFemaleFull()) * 10;
                    }
                }
                break;
            case ItemDefault.CODE_PQ:
                VolleyBallSetting volleyBallSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), VolleyBallSetting.class);
                if (volleyBallSetting.isFullSkip()) {
                    full = new int[2];
                    full[0] = volleyBallSetting.getMaleFullScore();
                    full[1] = volleyBallSetting.getFemaleFullScore();
                }
                break;
        }
        return full;
    }

    public static int getMaxTestCount() {
        return getMaxTestCount(MyApplication.getInstance());
    }

//    public static int getDeviceSumNum() {
//        int result = 0;
//        int machineCode = TestConfigs.sCurrentItem.getMachineCode();
//        switch (machineCode) {
//            case ItemDefault.CODE_ZWTQQ:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), SitReachSetting.class).getTestDeviceCount();
//                break;
//            case ItemDefault.CODE_LDTY:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), StandJumpSetting.class).getTestDeviceCount();
//                break;
//            case ItemDefault.CODE_ZFP:
//                result = Integer.parseInt(SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), RunTimerSetting.class).getRunNum());
//                break;
//            case ItemDefault.CODE_HWSXQ:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), MedicineBallSetting.class).getSpDeviceCount();
//                break;
//            case ItemDefault.CODE_TS:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), JumpRopeSetting.class).getDeviceSum();
//                break;
//            case ItemDefault.CODE_YWQZ:
//            case ItemDefault.CODE_SGBQS:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), SitUpSetting.class).getDeviceSum();
//                break;
//            case ItemDefault.CODE_YTXS:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), PullUpSetting.class).getDeviceSum();
//                break;
//            case ItemDefault.CODE_PQ:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), VolleyBallSetting.class).getSpDeviceCount();
//                break;
//            case ItemDefault.CODE_FWC:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), PushUpSetting.class).getDeviceSum();
//                break;
//            case ItemDefault.CODE_WLJ:
//            case ItemDefault.CODE_SHOOT:
//            case ItemDefault.CODE_ZCP:
//            case ItemDefault.CODE_ZQYQ:
//            case ItemDefault.CODE_LQYQ:
//                result = 1;
//                break;
//            case ItemDefault.CODE_MG:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), SargentSetting.class).getSpDeviceCount();
//                break;
//            case ItemDefault.CODE_SPORT_TIMER:
//                result = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), SportTimerSetting.class).getDeviceCount();
//                break;
//            default:
//                throw new IllegalArgumentException("wrong machine code");
//        }
//        return result;
//    }
}
