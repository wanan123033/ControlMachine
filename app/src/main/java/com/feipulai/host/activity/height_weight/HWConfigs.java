package com.feipulai.host.activity.height_weight;

import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Item;

/**
 * Created by James on 2018/9/28 0028.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class HWConfigs {

    //这里写死
    public static Item HEIGHT_ITEM = DBManager.getInstance().queryItemByItemCode(TestConfigs.HEIGHT_ITEM_CODE);
    public static Item WEIGHT_ITEM = DBManager.getInstance().queryItemByItemCode(TestConfigs.WEIGHT_ITEM_CODE);

    public static void  init(){
        HEIGHT_ITEM = DBManager.getInstance().queryItemByItemCode(TestConfigs.HEIGHT_ITEM_CODE);
        WEIGHT_ITEM = DBManager.getInstance().queryItemByItemCode(TestConfigs.WEIGHT_ITEM_CODE);
    }
}
