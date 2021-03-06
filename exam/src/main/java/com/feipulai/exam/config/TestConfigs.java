package com.feipulai.exam.config;

import android.app.Activity;
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
import com.feipulai.exam.activity.ranger.RangerSetting;
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
 * ??????????????????????????????????????????   ????????????:??????
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
    // ???????????????????????????????????????
    public static final String HEIGHT_ITEM_CODE = "E01";
    public static final String WEIGHT_ITEM_CODE = "E02";
    public static final Map<Object, Object> baseGroupMap = new HashMap<>();

    public static final int GROUP_PATTERN_SUCCESIVE = 0x0;  //????????????
    public static final int GROUP_PATTERN_LOOP = 0x1;   //????????????

    //????????????????????????
    public static final Map<Integer, Integer> itemMinScope = new HashMap<>();
    public static final Map<Integer, Integer> itemMaxScope = new HashMap<>();
    /**
     * ????????????
     * ?????????null,???????????????????????????;
     */
    public static Item sCurrentItem;

//     /**
//      * ?????????{@link #sCurrentItem},????????????????????????
//      * <p>
//      * ????????????????????????:
//      * 1. ??????{@link #sCurrentItem}???null,??????????????????????????????,?????????????????????;
//      * 2. ??????{@link #sCurrentItem}??????null,????????????{@link # sCurrentItem.getItemCode()}???null,
//      * ???????????????itemCode???null,???????????????,???????????????????????????,????????????????????????,?????????????????????,?????????????????????;?????????????????????:
//      * 2.1 ????????????????????????,?????????;
//      * 2.2 ????????????????????????,?????????????????????????????????,??????????????????????????????????????????itemCode,????????????????????????????????????:
//      * 2.2.1 ?????????????????????????????????????????????1???,???????????????????????????????????????itemCode???default?????????????????????;
//      * 2.2.2 ?????????????????????????????????????????????1???,??????????????????????????????;
//      * 3. ??????{@link #sCurrentItem}??????null,???{@link # sCurrentItem.getItemCode()}??????null,
//      * ????????????????????????????????????????????????????????????????????????,???????????????   2.2.1(??????????????????????????????????????????,??????????????????????????????????????????) ?????????
//      * <p>
//      * ?????????????????????????????????????????????null?????????,???????????????????????????
//      *
//      * @param machineCode ?????????,????????????
//      * @param itemCode    ????????????  ????????????,??????null??????,????????????itemCode???null  ?????????sCurrentItem???itemCode????????????itemCode
//      */
//     public static void init(int machineCode, String itemCode) {
//         Logger.i("machineCode:" + machineCode + "\titemCode:" + itemCode);
//         // ?????????????????????,?????????;??????sCurrentItem???null
//         if (machineCode == SharedPrefsConfigs.DEFAULT_MACHINE_CODE) {
//             return;
//         }
//         // ????????????????????????,??????itemcode???????????????
// //        if (machineCode == ItemDefault.CODE_HW) {
// //            sCurrentItem = DBManager.getInstance().queryItemByItemCode(TestConfigs.HEIGHT_ITEM_CODE);
// //        } else {
//         // ???????????????????????????machineCode???itemCode????????????:
//         // ????????????????????????,???????????????????????????,??????????????????
//         if (itemCode == null) {
//             List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
//             sCurrentItem = itemList.get(0);
//             sCurrentItem.setItemCode(null);
//         } else {
//             // ???????????????????????????machineCode???itemCode????????????:
//             // ????????????????????????,???????????????????????????,??????????????????
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
     * ?????????{@link #sCurrentItem},????????????????????????
     * ????????????????????????????????????????????????????????????
     *
     * @param context  ?????????,??????activity????????????
     * @param listener ????????? itemCode,???????????????????????????????????????(????????? sCurrentItem ???????????????????????????,listener?????????????????????????????????)
     * @return ???????????????????????????{@link #INIT_SUCCESS} ?????????????????????
     * 1. ??????????????????,???????????????{@link #INIT_NO_MACHINE_CODE}
     * 2. ??????machineCode???????????? itemCode ,??????????????? itemCode {@link #INIT_MULTI_ITEM_CODE},???????????????????????????,?????????????????????????????????????????? listener ???????????????
     */
    public static int init(final Context context, final int machineCode, String itemCode,
                           final DialogInterface.OnClickListener listener) {
        // ?????????????????????,?????????
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
                // ????????????????????????,???????????? ???????????????,?????????????????????,??????????????????
                itemCode = null;
            } else {
                sCurrentItem = item;
                // ???????????????,???????????????????????????,?????????????????????????????????????????????,????????????????????????????????????????????????????????????,????????????????????????
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, itemCode);
                LogUtils.operation("??????????????????:" + sCurrentItem.toString());
                return INIT_SUCCESS;
            }
        }

        final List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);

        String newItemCode = itemList.get(0).getItemCode();
//        // ???????????? itemCode
//        if (newItemCode == null) {
//            sCurrentItem = itemList.get(0);
//            Logger.i("sCurrentItem:" + sCurrentItem.toString());
//            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
//            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
//            return INIT_SUCCESS;
//        }

        // ?????????????????????
        // ???????????????????????????????????????,??????????????????????????????????????????????????????itemCode????????????
        if (itemList.size() == 1) {
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, newItemCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
            sCurrentItem = itemList.get(0);
            LogUtils.operation("??????????????????:" + sCurrentItem.toString());

            return INIT_SUCCESS;

        }
        if (machineCode == ItemDefault.CODE_ZCP) {
            sCurrentItem = itemList.get(0);
            newItemCode = itemList.get(0).getItemCode();
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, newItemCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
            SharedPrefsUtil.putValue(context, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, newItemCode);
            LogUtils.operation("??????????????????:" + sCurrentItem.toString());
            return INIT_SUCCESS;
        }
        sCurrentItem = null;
        // ????????????,?????????????????????
        if (context instanceof Activity) {
            if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new ItemDecideDialogBuilder(context, itemList, "?????????????????????(???????????????????????????????????????????????????)", new DialogInterface.OnClickListener() {
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
                    }
                });
            }
        }
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
                    ToastUtils.showShort("????????????,???????????????????????????,????????????");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " ????????????,???????????????????????????,????????????");
                    return false;
                } else if (codeItem.getMachineCode() != ItemDefault.CODE_ZCP || nameItem.getMachineCode() != ItemDefault.CODE_ZCP) {
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " ????????????,???????????????????????????,????????????");
                    ToastUtils.showShort("????????????,???????????????????????????,????????????");
                    return false;
                }
            } else if (codeItem == null && nameItem == null) {
                DBManager.getInstance().insertItem(ItemDefault.CODE_ZCP, item.getItemCode()
                        , item.getItemName(), "???'???", DBManager.TEST_TYPE_TIME);
            } else if (codeItem != null && nameItem == null) {
                if (codeItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    codeItem.setItemName(item.getItemName());
                    DBManager.getInstance().updateItem(codeItem);// ????????????????????????
                } else {
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " ????????????,???????????????????????????,????????????");
                    ToastUtils.showShort("????????????,???????????????????????????,????????????");
                    return false;
                }
            } else if (codeItem == null && nameItem != null) {
                if (nameItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    updateItemFillAll(nameItem, item.getItemCode());
                    nameItem.setItemCode(item.getItemCode());
                    DBManager.getInstance().updateItem(nameItem);// ????????????????????????
                } else {
                    ToastUtils.showShort("????????????,???????????????????????????,????????????");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + " ????????????,???????????????????????????,????????????");
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


        // ?????????????????????????????????Activity
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

        // ?????????????????????????????????Acitivity
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

        // ????????????????????????????????????
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZWTQQ, "???????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_LDTY, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZFP, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_HWSXQ, "???????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_TS, "??????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_YWQZ, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_YTXS, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_PQ, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_MG, "??????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_FWC, "?????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_LQYQ, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZCP, "?????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_ZQYQ, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_JGCJ, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_WLJ, "??????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SHOOT, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SPORT_TIMER, "????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_SGBQS, "???????????????");
        TestConfigs.machineNameMap.put(ItemDefault.CODE_FHL, "?????????");
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
        // ????????????????????????????????????
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

    public static int getMaxTestCount(String stuCode) {
        if (!TextUtils.isEmpty(stuCode)) {
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(stuCode);
            if (studentItem.getExamType() == StudentItem.EXAM_MAKE) {
                return 1;
            }
        }
        return getMaxTestCount();
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
            case ItemDefault.CODE_JGCJ:
                result = SharedPrefsUtil.loadFormSource(context, RangerSetting.class).getTestNo();
                break;
            default:
                throw new IllegalArgumentException("wrong machine code");
        }
        return result;
    }

    /**
     * ????????????????????????
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
            case ItemDefault.CODE_LQYQ:
                BasketBallSetting basketBallSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), BasketBallSetting.class);
                if (basketBallSetting.isFullSkip()) {
                    full = new int[2];
                    full[0] = (int) (basketBallSetting.getMaleFullScore() * 1000);
                    full[1] = (int) (basketBallSetting.getFemaleFullScore() * 1000);
                }
                break;
            case ItemDefault.CODE_ZQYQ:
                FootBallSetting footBallSetting = SharedPrefsUtil.loadFormSource(MyApplication.getInstance(), FootBallSetting.class);
                if (footBallSetting.isFullSkip()) {
                    full = new int[2];
                    full[0] = (int) (footBallSetting.getMaleFullScore() * 1000);
                    full[1] = (int) (footBallSetting.getFemaleFullScore() * 1000);
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
