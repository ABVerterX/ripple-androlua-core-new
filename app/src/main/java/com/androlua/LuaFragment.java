package com.androlua;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.luajava.LuaObject;
import com.luajava.LuaTable;

@SuppressLint("ValidFragment")
public class LuaFragment extends Fragment {

    private LuaTable mLayout = null;

    private final LuaObject mLoadLayout = null;
    private View mView;

   public LuaFragment(LuaTable layout) {
        super();
        mLayout=layout;
    }
    public LuaFragment(View layout) {
        super();
        mView=layout;
    }
    /*
        public LuaFragment(LuaTable layout){
            mLoadLayout=layout.getLuaState().getLuaObject("loadlayout");
            mLayout=layout;
        }*/
    public void setLayout(LuaTable layout) {
        mLayout = layout;
        mView = null;
    }

    public void setLayout(View layout) {
        mView = layout;
        mLayout = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            if (mView != null)
                return mView;
            if (mLayout != null)
                return (View) ((LuaObject) (mLayout.getLuaState().getLuaObject("require").call("loadlayout"))).call(mLayout);
            return new TextView(getActivity());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
