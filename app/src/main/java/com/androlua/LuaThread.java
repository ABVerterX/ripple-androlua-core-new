package com.androlua;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaMetaTable;
import com.luajava.LuaObject;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import java.io.IOException;
import java.util.regex.Pattern;

public class LuaThread extends Thread implements Runnable, LuaMetaTable, LuaGcable {

    private boolean mGc;

    @Override
    public void gc() {
        // TODO: Implement this method
        quit();
        mGc = true;
    }

    @Override
    public boolean isGc() {
        return mGc;
    }


    @Override
    public Object __call(Object[] arg) {
        // TODO: Implement this method
        return null;
    }

    @Override
    public Object __index(final String key) {
        // TODO: Implement this method
        return new LuaMetaTable() {
            @Override
            public Object __call(Object[] arg) {
                // TODO: Implement this method
                call(key, arg);
                return null;
            }

            @Override
            public Object __index(String key) {
                // TODO: Implement this method
                return null;
            }

            @Override
            public void __newIndex(String key, Object value) {
                // TODO: Implement this method
            }
        };
    }

    @Override
    public void __newIndex(String key, Object value) {
        // TODO: Implement this method
        set(key, value);
    }

    private LuaState luaState;
    private Handler handler;
    public boolean isRunning = false;
    private final LuaContext luaContext;

    private final boolean isLoop;

    private String src;

    private Object[] arg = new Object[0];

    private byte[] buffer;

    public LuaThread(LuaContext luaContext, String src) {
        this(luaContext, src, false, null);
    }

    public LuaThread(LuaContext luaContext, String src, Object[] arg) {
        this(luaContext, src, false, arg);
    }

    public LuaThread(LuaContext luaContext, String src, boolean isLoop) {
        this(luaContext, src, isLoop, null);
    }

    public LuaThread(LuaContext luaContext, String src, boolean isLoop, Object[] arg) {
        luaContext.regGc(this);
        this.luaContext = luaContext;
        this.src = src;
        this.isLoop = isLoop;
        if (arg != null) this.arg = arg;
    }

    public LuaThread(LuaContext luaContext, LuaObject func) throws LuaException {
        this(luaContext, func, false, null);
    }

    public LuaThread(LuaContext luaContext, LuaObject func, Object[] arg) throws LuaException {
        this(luaContext, func, false, arg);
    }

    public LuaThread(LuaContext luaContext, LuaObject func, boolean isLoop) throws LuaException {
        this(luaContext, func, isLoop, null);
    }

    public LuaThread(LuaContext luaContext, LuaObject func, boolean isLoop, Object[] arg) throws LuaException {
        this.luaContext = luaContext;
        if (arg != null) this.arg = arg;
        this.isLoop = isLoop;
        buffer = func.dump();
    }

    @Override
    public void run() {
        try {
            if (luaState == null) {
                initLua();
                if (buffer != null) newLuaThread(buffer, arg);
                else newLuaThread(src, arg);
            }
        } catch (LuaException e) {
            luaContext.sendError(this.toString(), e);
            return;
        }
        if (isLoop) {
            Looper.prepare();
            handler = new ThreadHandler();
            isRunning = true;
            luaState.getGlobal("run");
            if (!luaState.isNil(-1)) {
                luaState.pop(1);
                runFunc("run");
            }

            Looper.loop();
        }
        isRunning = false;
        luaState.gc(LuaState.LUA_GCCOLLECT, 1);
        System.gc();
    }

    public void call(String func) {
        push(3, func);
    }

    public void call(String func, Object[] args) {
        if (args.length == 0) push(3, func);
        else push(1, func, args);
    }

    public void set(String key, Object value) {
        push(4, key, new Object[]{value});
    }

    public Object get(String key) throws LuaException {
        luaState.getGlobal(key);
        return luaState.toJavaObject(-1);
    }

    public void quit() {
        if (isRunning) {
            isRunning = false;
            handler.getLooper().quit();
        }
    }

    public void push(int what, String s) {
        if (!isRunning) {
            luaContext.sendMsg("thread is not running");
            return;
        }

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("data", s);
        message.setData(bundle);
        message.what = what;

        handler.sendMessage(message);

    }

    public void push(int what, String s, Object[] args) {
        if (!isRunning) {
            luaContext.sendMsg("thread is not running");
            return;
        }

        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("data", s);
        bundle.putSerializable("args", args);
        message.setData(bundle);
        message.what = what;

        handler.sendMessage(message);

    }

    private String errorReason(int error) {
        switch (error) {
            case 6:
                return "error error";
            case 5:
                return "GC error";
            case 4:
                return "Out of memory";
            case 3:
                return "Syntax error";
            case 2:
                return "Runtime error";
            case 1:
                return "Yield error";
        }
        return "Unknown error " + error;
    }


    private void initLua() throws LuaException {
        luaState = LuaStateFactory.newLuaState();
        luaState.openLibs();
        luaState.pushJavaObject(luaContext.getContext());
        if (luaContext instanceof LuaActivity) {
            luaState.setGlobal("activity");
        } else if (luaContext instanceof LuaService) {
            luaState.setGlobal("service");
        }
        luaState.pushJavaObject(this);
        luaState.setGlobal("this");
        luaState.pushContext(luaContext);

        JavaFunction print = new LuaPrint(luaContext, luaState);
        print.register("print");

        luaState.getGlobal("package");

        luaState.pushString(luaContext.getLuaLPath());
        luaState.setField(-2, "path");
        luaState.pushString(luaContext.getLuaCpath());
        luaState.setField(-2, "cpath");
        luaState.pop(1);

        JavaFunction set = new JavaFunction(luaState) {
            @Override
            public int execute() throws LuaException {

                luaContext.set(L.toString(2), L.toJavaObject(3));
                return 0;
            }
        };
        set.register("set");

        JavaFunction call = new JavaFunction(luaState) {
            @Override
            public int execute() throws LuaException {

                int top = L.getTop();
                if (top > 2) {
                    Object[] args = new Object[top - 2];
                    for (int i = 3; i <= top; i++) {
                        args[i - 3] = L.toJavaObject(i);
                    }
                    luaContext.call(L.toString(2), args);
                } else if (top == 2) {
                    luaContext.call(L.toString(2));
                }
                return 0;
            }
        };
        call.register("call");
    }

    private void newLuaThread(String str, Object... args) {
        try {

            if (Pattern.matches("^\\w+$", str)) {
                doAsset(str + ".lua", args);
            } else if (Pattern.matches("^[\\w\\.\\_/]+$", str)) {
                luaState.getGlobal("luajava");
                luaState.pushString(luaContext.getLuaDir());
                luaState.setField(-2, "luadir");
                luaState.pushString(str);
                luaState.setField(-2, "luapath");
                luaState.pop(1);

                doFile(str, args);
            } else {
                doString(str, args);
            }

        } catch (Exception e) {
            luaContext.sendError(this.toString(), e);
            quit();
        }

    }

    private void newLuaThread(byte[] buf, Object... args) {
        try {
            int ok;
            luaState.setTop(0);
            ok = luaState.LloadBuffer(buf, "TimerTask");

            if (ok == 0) {
                luaState.getGlobal("debug");
                luaState.getField(-1, "traceback");
                luaState.remove(-2);
                luaState.insert(-2);
                int l = args.length;
                for (Object o : args) {
                    luaState.pushObjectValue(o);
                }
                ok = luaState.pcall(l, 0, -2 - l);
                if (ok == 0) {
                    return;
                }
            }
            throw new LuaException(errorReason(ok) + ": " + luaState.toString(-1));
        } catch (Exception e) {
            luaContext.sendError(this.toString(), e);
            quit();
        }
    }

    private void doFile(String filePath, Object... args) throws LuaException {
        int ok;
        luaState.setTop(0);
        ok = luaState.LloadFile(filePath);

        if (ok == 0) {
            luaState.getGlobal("debug");
            luaState.getField(-1, "traceback");
            luaState.remove(-2);
            luaState.insert(-2);
            int l = args.length;
            for (Object o : args) {
                luaState.pushObjectValue(o);
            }
            ok = luaState.pcall(l, 0, -2 - l);
            if (ok == 0) {
                return;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + luaState.toString(-1));
    }


    public void doAsset(String name, Object... args) throws LuaException, IOException {
        int ok;
        byte[] bytes = LuaUtil.readAsset(luaContext.getContext(), name);
        luaState.setTop(0);
        ok = luaState.LloadBuffer(bytes, name);

        if (ok == 0) {
            luaState.getGlobal("debug");
            luaState.getField(-1, "traceback");
            luaState.remove(-2);
            luaState.insert(-2);
            int l = args.length;
            for (Object o : args) {
                luaState.pushObjectValue(o);
            }
            ok = luaState.pcall(l, 0, -2 - l);
            if (ok == 0) {
                return;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + luaState.toString(-1));
    }

    private void doString(String src, Object... args) throws LuaException {
        luaState.setTop(0);
        int ok = luaState.LloadString(src);

        if (ok == 0) {
            luaState.getGlobal("debug");
            luaState.getField(-1, "traceback");
            luaState.remove(-2);
            luaState.insert(-2);
            int l = args.length;
            for (Object o : args) {
                luaState.pushObjectValue(o);
            }
            ok = luaState.pcall(l, 0, -2 - l);
            if (ok == 0) {

                return;
            }
        }
        throw new LuaException(errorReason(ok) + ": " + luaState.toString(-1));
    }


    private void runFunc(String funcName, Object... args) {
        try {
            luaState.setTop(0);
            luaState.getGlobal(funcName);
            if (luaState.isFunction(-1)) {
                luaState.getGlobal("debug");
                luaState.getField(-1, "traceback");
                luaState.remove(-2);
                luaState.insert(-2);

                int l = args.length;
                for (Object o : args) {
                    luaState.pushObjectValue(o);
                }

                int ok = luaState.pcall(l, 1, -2 - l);
                if (ok == 0) {
                    return;
                }
                throw new LuaException(errorReason(ok) + ": " + luaState.toString(-1));
            }
        } catch (LuaException e) {
            luaContext.sendError(this + " " + funcName, e);
        }

    }

    private void setField(String key, Object value) {
        try {
            luaState.pushObjectValue(value);
            luaState.setGlobal(key);
        } catch (LuaException e) {
            luaContext.sendError(this.toString(), e);
        }
    }

    private class ThreadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch (msg.what) {
                case 0:
                    newLuaThread(data.getString("data"), (Object[]) data.getSerializable("args"));
                    break;
                case 1:
                    runFunc(data.getString("data"), (Object[]) data.getSerializable("args"));
                    break;
                case 2:
                    newLuaThread(data.getString("data"));
                    break;
                case 3:
                    runFunc(data.getString("data"));
                    break;
                case 4:
                    setField(data.getString("data"), ((Object[]) data.getSerializable("args"))[0]);
                    break;
            }
        }
    }

}
