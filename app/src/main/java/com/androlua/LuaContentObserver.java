package com.androlua;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class LuaContentObserver extends ContentObserver implements LuaGcable {

    private OnChangeListener onChangeListener;
    private boolean isGc;

    private LuaContentObserver(Handler handler) {
        super(handler);
    }

    public LuaContentObserver(LuaContext context, String uri) {
        this(new Handler(LuaApplication.getInstance().getMainLooper()));
        Uri mUri = Uri.parse(uri);
        context.regGc(this);
        LuaApplication.getInstance().getContentResolver().registerContentObserver(mUri, true, this);
    }

    public LuaContentObserver(LuaContext context, Uri mUri) {
        this(new Handler(LuaApplication.getInstance().getMainLooper()));

        context.regGc(this);
        LuaApplication.getInstance().getContentResolver().registerContentObserver(mUri, true, this);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (onChangeListener != null) {
            Cursor cursor = LuaApplication.getInstance().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) cursor.moveToFirst();
            onChangeListener.onChange(selfChange, uri, cursor);
            if (cursor != null) cursor.close();
        }
    }

    public void setOnChangeListener(OnChangeListener listener) {
        onChangeListener = listener;
    }

    @Override
    public void gc() {
        LuaApplication.getInstance().getContentResolver().unregisterContentObserver(this);
        isGc = true;
    }

    @Override
    public boolean isGc() {
        return isGc;
    }

    public interface OnChangeListener {
        void onChange(boolean selfChange, Uri uri, Cursor cursor);
    }

}
