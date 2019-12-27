package com.ww.fpl.videolibrary.play.cover;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kk.taurus.playerbase.receiver.BaseCover;
import com.ww.fpl.videolibrary.R;
import com.ww.fpl.videolibrary.play.play.DataInter;

public class CloseCover extends BaseCover implements View.OnClickListener {

    private ImageView mCloseIcon;

    public CloseCover(Context context) {
        super(context);
    }

    @Override
    public void onReceiverBind() {
        super.onReceiverBind();
        mCloseIcon=findViewById(R.id.iv_close);
        mCloseIcon.setOnClickListener(this);
    }

    @Override
    public void onReceiverUnBind() {
        super.onReceiverUnBind();
    }

    @Override
    public View onCreateCoverView(Context context) {
        return View.inflate(context, R.layout.layout_close_cover, null);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {

    }

    @Override
    public void onErrorEvent(int eventCode, Bundle bundle) {

    }

    @Override
    public void onReceiverEvent(int eventCode, Bundle bundle) {

    }

    @Override
    public int getCoverLevel() {
        return levelMedium(10);
    }

    @Override
    public void onClick(View v) {
        notifyReceiverEvent(DataInter.Event.EVENT_CODE_REQUEST_CLOSE, null);
    }
}
