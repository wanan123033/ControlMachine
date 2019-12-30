package com.ww.fpl.videolibrary.play.play;

import android.text.TextUtils;
import android.util.Log;

import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.provider.BaseDataProvider;

public class DemoDataProvider extends BaseDataProvider {

    @Override
    public void handleSourceData(final DataSource sourceData) {
        Log.i("handleSourceData", "cancel");
        if (!TextUtils.isEmpty(sourceData.getData())) {
            onProviderMediaDataSuccess(sourceData);
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public void destroy() {
        cancel();
    }

}
