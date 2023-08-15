package com.androlua;

import android.os.FileObserver;

public class LuaFileObserver extends FileObserver {
    private OnEventListener mOnEventListener;

    public LuaFileObserver(String path) {
        super(path);
    }

    public LuaFileObserver(String path, int mask) {
        super(path, mask);
    }

    public void setOnEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    @Override
    public void onEvent(int event, String path) {
        if (mOnEventListener != null) mOnEventListener.onEvent(event, path);
    }

    public interface OnEventListener {
        void onEvent(int event, String path);
    }
}
