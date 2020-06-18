package com.feipulai.host.activity.sitreach.more.pair;

import com.feipulai.host.activity.situp.pair.BasePairActivity;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;

/**
 * Created by pengjf on 2020/5/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachPairActivity extends BasePairActivity {
    @Override
    public BasePairPresenter getPresenter() {
        return new SitReachPairPresenter(this, this);
    }
}
