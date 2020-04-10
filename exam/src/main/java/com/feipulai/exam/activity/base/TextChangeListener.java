package com.feipulai.exam.activity.base;

import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextChangeListener implements TextWatcher {
    private int id;

    public TextChangeListener(int id){
        this.id = id;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTextChanged(s,id);
    }

    protected abstract void afterTextChanged(Editable s, int id);
}
