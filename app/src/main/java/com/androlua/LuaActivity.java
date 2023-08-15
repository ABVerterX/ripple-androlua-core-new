package com.androlua;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.androlua.accessibility.LuaAccessibilityService;
import com.bumptech.glide.signature.ObjectKey;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaObject;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class LuaActivity extends AppCompatActivity implements LuaBroadcastReceiver.OnReceiveListener, LuaContext {

    public LuaViewModel viewModel;

    private final static String ARG = "arg";
    private final static String DATA = "data";
    private final static String NAME = "name";
    private static final ArrayList<String> prjCache = new ArrayList<>();
    private String luaDir;
    private Handler handler;
    private TextView status;
    private String luaCpath;
    private LuaDexLoader mLuaDexLoader;
    private int mWidth;
    private int mHeight;
    private ArrayListAdapter<String> adapter;
    private LuaState L;
    private String luaPath;
    private final StringBuilder toastBuilder = new StringBuilder();
    private Toast toast;
    private LinearLayout layout;
    private boolean isSetViewed;
    private long lastShow;
    private LuaObject mOnKeyDown;
    private LuaObject mOnKeyUp;
    private LuaObject mOnKeyLongPress;
    private LuaObject mOnTouchEvent;
    private String localDir;

    private String libDir;

    private String luaExtDir;

    private LuaBroadcastReceiver mReceiver;

    private String luaLPath;

    private boolean mDebug = true;
    private LuaResources mResources;
    private final ArrayList<LuaGcable> gcList = new ArrayList<>();
    private String pageName = "main";
    private static String sKey;
    private static final HashMap<String, LuaActivity> sLuaActivityMap = new HashMap<>();
    private LuaObject mOnKeyShortcut;

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }



    @Override
    public ArrayList<ClassLoader> getClassLoaders() {
        // Implement this method
        return mLuaDexLoader.getClassLoaders();
    }

    public HashMap<String, String> getLibrarys() {
        return mLuaDexLoader.getLibraries();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //设置print界面
        super.onCreate(null);

        viewModel = new ViewModelProvider(this).get(LuaViewModel.class);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;

        layout = new LinearLayout(this);
        //layout.setBackgroundColor(Color.WHITE);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        status = new TextView(this);

        status.setTextColor(Color.BLACK);
        scroll.addView(status, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        //layout.addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        status.setText("");
        status.setTextIsSelectable(true);
        ListView list = new ListView(this);
        list.setFastScrollEnabled(true);
        adapter = new ArrayListAdapter<>(this, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (convertView == null) view.setTextIsSelectable(true);
                return view;
            }
        };
        list.setAdapter(adapter);
        layout.addView(list, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        //定义文件夹
        LuaApplication app = (LuaApplication) getApplication();
//        if (app.getClass() != LuaApplication.class) {
//            while (true) {
//                if (app.getClass() == LuaApplication.class) break;
//            }
//        }
        localDir = app.getLocalDir();
        String odexDir = app.getOdexDir();
        libDir = app.getLibDir();
        String luaMdDir = app.getMdDir();
        luaCpath = app.getLuaCpath();
        luaDir = localDir;
        luaLPath = app.getLuaLPath();
        luaExtDir = app.getLuaExtDir();


        handler = new MainHandler();


        try {
            status.setText("");
            adapter.clear();
            Intent intent = getIntent();
            Object[] arg = (Object[]) intent.getSerializableExtra(ARG);
            if (arg == null) arg = new Object[0];

            luaPath = getLuaPath();
            pageName = new File(luaPath).getName();
            int idx = pageName.lastIndexOf(".");
            if (idx > 0) pageName = pageName.substring(0, idx);

            luaLPath = (luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;") + luaLPath;
            initLua();

            mLuaDexLoader = new LuaDexLoader(this);
            mLuaDexLoader.loadLibs();
            //MultiDex.installLibs(this);
            sLuaActivityMap.put(pageName, this);
            doFile(luaPath, arg);
            Boolean isCreate = true;
            if (!pageName.equals("main")) runFunc("main", arg);
            runFunc(pageName, arg);
            runFunc("onCreate", savedInstanceState);
            if (!isSetViewed) {
                TypedArray array = getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorBackground, android.R.attr.textColorPrimary, android.R.attr.textColorHighlightInverse,});
                int backgroundColor = array.getColor(0, 0xFF00FF);
                int textColor = array.getColor(1, 0xFF00FF);
                array.recycle();
                status.setTextColor(textColor);
                layout.setBackgroundColor(backgroundColor);
                setContentView(layout);
            }
        } catch (Exception e) {
            sendMsg(e.getMessage());
            setContentView(layout);
            return;
        }

        mOnKeyShortcut = L.getLuaObject("onKeyShortcut");
        if (mOnKeyShortcut.isNil()) mOnKeyShortcut = null;
        mOnKeyDown = L.getLuaObject("onKeyDown");
        if (mOnKeyDown.isNil()) mOnKeyDown = null;
        mOnKeyUp = L.getLuaObject("onKeyUp");
        if (mOnKeyUp.isNil()) mOnKeyUp = null;
        mOnKeyLongPress = L.getLuaObject("onKeyLongPress");
        if (mOnKeyLongPress.isNil()) mOnKeyLongPress = null;
        mOnTouchEvent = L.getLuaObject("onTouchEvent");
        if (mOnTouchEvent.isNil()) mOnTouchEvent = null;
        LuaObject onAccessibilityEvent = L.getLuaObject("onAccessibilityEvent");
        if (onAccessibilityEvent.isFunction())
            LuaAccessibilityService.onAccessibilityEvent = onAccessibilityEvent.getFunction();

        try {
            throw new RuntimeException("");
        } catch (Exception e) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            PrintStream p = new PrintStream(b);
            e.printStackTrace(p);
            String s = b.toString();
            String[] bs = s.split("\n");
            for (int i = 1; i < bs.length; i++) {
                String l = bs[i];
                if (l.contains("com.androlua") || l.contains("com.nirenr.talkman") || l.contains("android.app") || l.contains("android.os") || l.contains("java.lang") || l.contains("com.android"))
                    continue;
                runFunc("onHook");
                return;
            }
        }
    }

    public void setFragment(Fragment fragment) {
        isSetViewed = true;
        setContentView(new View(this));
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (mOnKeyShortcut != null) {
            try {
                Object ret = mOnKeyShortcut.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) return true;
            } catch (LuaException e) {
                sendError("onKeyShortcut", e);
            }
        }
        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    public void regGc(LuaGcable obj) {
        // Implement this method
        gcList.add(obj);
    }
    
    public void initMain() {
        prjCache.add(getLocalDir());
    }

    public String getLuaPath() {
        // Implement this method
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String path;
        if (uri == null) return null;

        path = uri.getPath();
        if (!new File(path).exists() && new File(getLuaPath(path)).exists())
            path = getLuaPath(path);

        luaPath = path;
        File f = new File(path);

        luaDir = new File(luaPath).getParent();
        if (f.getName().equals("main.lua") && new File(luaDir, "init.lua").exists()) {
            if (!prjCache.contains(luaDir)) prjCache.add(luaDir);
        } else {
            String parent = luaDir;
            while (parent != null) {
                if (prjCache.contains(parent)) {
                    luaDir = parent;
                    break;
                } else {
                    if (new File(parent, "main.lua").exists() && new File(parent, "init.lua").exists()) {
                        luaDir = parent;
                        if (!prjCache.contains(luaDir)) prjCache.add(luaDir);
                        break;
                    }
                }
                parent = new File(parent).getParent();
            }
        }
        return path;
    }

    public String getQuery(String name) {
        Uri uri = getIntent().getData();
        if (uri == null) return null;
        return uri.getQueryParameter(name);
    }

    public Object getArg(int index) {
        Object[] arg = (Object[]) getIntent().getSerializableExtra(ARG);
        if (arg != null && arg.length > index && index >= 0) {
            return arg[index];
        }
        return null;
    }

    @Override
    public String getLuaPath(String path) {
        return new File(getLuaDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaPath(String dir, String name) {
        return new File(getLuaDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String path) {
        return new File(getLuaExtDir(), path).getAbsolutePath();
    }

    @Override
    public String getLuaExtPath(String dir, String name) {
        return new File(getLuaExtDir(dir), name).getAbsolutePath();
    }

    @Override
    public String getLuaLPath() {
        return luaLPath;
    }

    @Override
    public String getLuaCpath() {
        return luaCpath;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public LuaState getLuaState() {
        return L;
    }

    public View getDecorView() {
        return getWindow().getDecorView();
    }

    public String getLocalDir() {
        return localDir;
    }

    @Override
    public String getLuaExtDir() {
        return luaExtDir;
    }

    @Override
    public void setLuaExtDir(String dir) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            luaExtDir = new File(sdDir, dir).getAbsolutePath();
        } else {
            File[] fs = new File("/storage").listFiles();
            assert fs != null;
            for (File f : fs) {
                String[] ls = f.list();
                if (ls == null) continue;
                if (ls.length > 5) luaExtDir = new File(f, dir).getAbsolutePath();
            }
            if (luaExtDir == null) luaExtDir = getDir(dir, Context.MODE_PRIVATE).getAbsolutePath();
        }
        File d = new File(luaExtDir);
        if (!d.exists()) d.mkdirs();
    }

    @Override
    public String getLuaExtDir(String name) {
        File dir = new File(getLuaExtDir(), name);
        if (!dir.exists()) if (!dir.mkdirs()) return null;
        return dir.getAbsolutePath();
    }

    @Override
    public String getLuaDir() {
        return luaDir;
    }

    public void setLuaDir(String dir) {
        luaDir = dir;
    }

    @Override
    public String getLuaDir(String name) {
        File dir = new File(luaDir + "/" + name);
        if (!dir.exists()) if (!dir.mkdirs()) return null;
        return dir.getAbsolutePath();
    }

    public DexClassLoader loadApp(String path) {
        return mLuaDexLoader.loadApp(path);
    }

    public DexClassLoader loadDex(String path) throws LuaException {
        return mLuaDexLoader.loadDex(path);
    }

    public void loadResources(String path) {
        mLuaDexLoader.loadResources(path);
    }

    @Override
    public AssetManager getAssets() {
        if (mLuaDexLoader != null && mLuaDexLoader.getAssets() != null)
            return mLuaDexLoader.getAssets();
        return super.getAssets();
    }

    public LuaResources getLuaResources() {
        Resources superRes = super.getResources();
        if (mLuaDexLoader != null && mLuaDexLoader.getResources() != null)
            superRes = mLuaDexLoader.getResources();
        mResources = new LuaResources(getAssets(), superRes.getDisplayMetrics(), superRes.getConfiguration());
        mResources.setSuperResources(superRes);
        return mResources;
    }

    public Resources getSuperResources() {
        return super.getResources();
    }

    @Override
    public Resources getResources() {
        if (mLuaDexLoader != null && mLuaDexLoader.getResources() != null)
            return mLuaDexLoader.getResources();
        if (mResources != null) return mResources;
        return super.getResources();
    }

    public Object loadLib(String name) throws LuaException {
        int i = name.indexOf(".");
        String fn = name;
        if (i > 0) fn = name.substring(0, i);
        File f = new File(libDir + "/lib" + fn + ".so");
        if (!f.exists()) {
            f = new File(luaDir + "/lib" + fn + ".so");
            if (!f.exists()) throw new LuaException("can not find lib " + name);
            LuaUtil.copyFile(luaDir + "/lib" + fn + ".so", libDir + "/lib" + fn + ".so");
        }
        LuaObject require = L.getLuaObject("require");
        return require.call(name);
    }

    public Intent registerReceiver(LuaBroadcastReceiver receiver, IntentFilter filter) {
        return super.registerReceiver(receiver, filter);
    }

    public Intent registerReceiver(LuaBroadcastReceiver.OnReceiveListener ltr, IntentFilter filter) {
        LuaBroadcastReceiver receiver = new LuaBroadcastReceiver(ltr);
        return super.registerReceiver(receiver, filter);
    }

    public Intent registerReceiver(IntentFilter filter) {
        if (mReceiver != null) unregisterReceiver(mReceiver);
        mReceiver = new LuaBroadcastReceiver(this);
        return super.registerReceiver(mReceiver, filter);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        try {
            super.unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.i("lua", "unregisterReceiver: " + receiver);
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        runFunc("onReceive", context, intent);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        isSetViewed = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        runFunc("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        runFunc("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        runFunc("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        runFunc("onStop");
    }

    public static LuaActivity getActivity(String name) {
        return sLuaActivityMap.get(name);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) unregisterReceiver(mReceiver);

        for (LuaGcable obj : gcList) {
            obj.gc();
        }
        sLuaActivityMap.remove(pageName);
        runFunc("onDestroy");
        super.onDestroy();
        System.gc();
        L.gc(LuaState.LUA_GCCOLLECT, 1);
        //L.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Implement this method
        if (data != null) {
            String name = data.getStringExtra(NAME);
            if (name != null) {
                Object[] res = (Object[]) data.getSerializableExtra(DATA);
                if (res == null) {
                    runFunc("onResult", name);
                } else {
                    Object[] arg = new Object[res.length + 1];
                    arg[0] = name;
                    for (int i = 0; i < res.length; i++)
                        arg[i + 1] = res[i];
                    Object ret = runFunc("onResult", arg);
                    if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) return;
                }
            }
        }
        runFunc("onActivityResult", requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        runFunc("onRequestPermissionsResult", requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mOnKeyDown != null) {
            try {
                Object ret = mOnKeyDown.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) return true;
            } catch (LuaException e) {
                sendError("onKeyDown", e);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mOnKeyUp != null) {
            try {
                Object ret = mOnKeyUp.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) return true;
            } catch (LuaException e) {
                sendError("onKeyUp", e);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mOnKeyLongPress != null) {
            try {
                Object ret = mOnKeyLongPress.call(keyCode, event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) return true;
            } catch (LuaException e) {
                sendError("onKeyLongPress", e);
            }
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnTouchEvent != null) {
            try {
                Object ret = mOnTouchEvent.call(event);
                if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret) return true;
            } catch (LuaException e) {
                sendError("onTouchEvent", e);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        runFunc("onCreateOptionsMenu", menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Object ret = null;
        if (!item.hasSubMenu())
            ret = runFunc("onOptionsItemSelected", item);
        if (ret != null && ret.getClass() == Boolean.class && (Boolean) ret)
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        runFunc("onCreateContextMenu", menu, v, menuInfo);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        runFunc("onContextItemSelected", item);
        return super.onContextItemSelected(item);
    }

//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        // TODO: Implement this method
//        if (!item.hasSubMenu())
//            runFunc("onMenuItemSelected", featureId, item);
//        return super.onMenuItemSelected(featureId, item);
//    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        //wm.getDefaultDisplay().getRealMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = outMetrics.heightPixels;
        runFunc("onConfigurationChanged", newConfig);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public Map getGlobalData() {
        return ((LuaApplication) getApplication()).getGlobalData();
    }

    @Override
    public Object getSharedData() {
        return LuaApplication.getInstance().getSharedData();
    }

    @Override
    public Object getSharedData(String key) {
        return LuaApplication.getInstance().getSharedData(key);
    }

    @Override
    public Object getSharedData(String key, Object def) {
        return LuaApplication.getInstance().getSharedData(key, def);
    }

    @Override
    public boolean setSharedData(String key, Object value) {
        return LuaApplication.getInstance().setSharedData(key, value);
    }

    public boolean bindService(int flag) {
        ServiceConnection conn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName comp, IBinder binder) {
                // TODO: Implement this method
                runFunc("onServiceConnected", comp, ((LuaService.LuaBinder) binder).getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName comp) {
                // TODO: Implement this method
                runFunc("onServiceDisconnected", comp);
            }
        };
        return bindService(conn, flag);
    }

    public boolean bindService(ServiceConnection conn, int flag) {
        // TODO: Implement this method
        Intent service = new Intent(this, LuaService.class);
        service.putExtra("luaDir", luaDir);
        service.putExtra("luaPath", luaPath);
        return super.bindService(service, conn, flag);
    }

    public boolean stopService() {
        return stopService(new Intent(this, LuaService.class));
    }

    public ComponentName startService() {
        return startService(null, null);
    }

    public ComponentName startService(Object[] arg) {
        return startService(null, arg);
    }

    public ComponentName startService(String path) {
        return startService(path, null);
    }

    public ComponentName startService(String path, Object[] arg) {
        // TODO: Implement this method
        Intent intent = new Intent(this, LuaService.class);
        intent.putExtra("luaDir", luaDir);
        intent.putExtra("luaPath", luaPath);
        if (path != null) {
            if (path.charAt(0) != '/')
                intent.setData(Uri.parse("file://" + luaDir + "/" + path + ".lua"));
            else intent.setData(Uri.parse("file://" + path));
        }

        if (arg != null) intent.putExtra(ARG, arg);

        return super.startService(intent);
    }

    public void newActivity(String path, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, null, newDocument);
    }

    public void newActivity(String path, Object[] arg, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, arg, newDocument);
    }

    public void newActivity(int req, String path, boolean newDocument) throws FileNotFoundException {
        newActivity(req, path, null, newDocument);
    }

    public void newActivity(String path) throws FileNotFoundException {
        newActivity(1, path, new Object[0]);
    }

    public void newActivity(String path, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, arg);
    }

    public void newActivity(int req, String path) throws FileNotFoundException {
        newActivity(req, path, new Object[0]);
    }

    public void newActivity(int req, String path, Object[] arg) throws FileNotFoundException {
        newActivity(req, path, arg, false);
    }

    public void newActivity(int req, String path, Object[] arg, boolean newDocument) throws FileNotFoundException {
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/') path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/main.lua").exists()) path += "/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua")) path += ".lua";
        if (!new File(path).exists()) throw new FileNotFoundException(path);

        if (newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }

        intent.setData(Uri.parse("file://" + path));

        if (arg != null) intent.putExtra(ARG, arg);
        if (newDocument) startActivity(intent);
        else startActivityForResult(intent, req);
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void newActivity(String path, int in, int out, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, in, out, null, newDocument);
    }

    public void newActivity(String path, int in, int out, Object[] arg, boolean newDocument) throws FileNotFoundException {
        newActivity(1, path, in, out, arg, newDocument);
    }

    public void newActivity(int req, String path, int in, int out, boolean newDocument) throws FileNotFoundException {
        newActivity(req, path, in, out, null, newDocument);
    }

    public void newActivity(String path, int in, int out) throws FileNotFoundException {
        newActivity(1, path, in, out, new Object[0]);
    }

    public void newActivity(String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(1, path, in, out, arg);
    }

    public void newActivity(int req, String path, int in, int out) throws FileNotFoundException {
        newActivity(req, path, in, out, new Object[0]);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg) throws FileNotFoundException {
        newActivity(req, path, in, out, arg, false);
    }

    public void newActivity(int req, String path, int in, int out, Object[] arg, boolean newDocument) throws FileNotFoundException {
        Intent intent = new Intent(this, LuaActivity.class);
        intent.putExtra(NAME, path);
        if (path.charAt(0) != '/') path = luaDir + "/" + path;
        File f = new File(path);
        if (f.isDirectory() && new File(path + "/main.lua").exists()) path += "/main.lua";
        else if ((f.isDirectory() || !f.exists()) && !path.endsWith(".lua")) path += ".lua";
        if (!new File(path).exists()) throw new FileNotFoundException(path);
        intent.setData(Uri.parse("file://" + path));

        if (newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }


        if (arg != null) intent.putExtra(ARG, arg);
        if (newDocument) startActivity(intent);
        else startActivityForResult(intent, req);
        overridePendingTransition(in, out);

    }

    public void finish(boolean finishTask) {
        if (!finishTask) {
            super.finish();
            return;
        }
        Intent intent = getIntent();
        if (intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
            finishAndRemoveTask();
        else super.finish();
    }

    public LuaAsyncTask newTask(LuaObject func) throws LuaException {
        return newTask(func, null, null);
    }

    public LuaAsyncTask newTask(LuaObject func, LuaObject callback) throws LuaException {
        return newTask(func, null, callback);
    }

    public LuaAsyncTask newTask(LuaObject func, LuaObject update, LuaObject callback) throws LuaException {
        return new LuaAsyncTask(this, func, update, callback);
    }

    public LuaThread newThread(LuaObject func) throws LuaException {
        return newThread(func, new Object[0]);
    }

    public LuaThread newThread(LuaObject func, Object[] arg) throws LuaException {
        LuaThread thread = new LuaThread(this, func, true, arg);
        return thread;
    }

    public LuaTimer newTimer(LuaObject func) throws LuaException {
        return newTimer(func, new Object[0]);
    }

    public LuaTimer newTimer(LuaObject func, Object[] arg) throws LuaException {
        return new LuaTimer(this, func, arg);
    }

    public LuaAsyncTask task(long delay, LuaObject func) throws LuaException {
        return task(delay, null, null);
    }

    public LuaAsyncTask task(long delay, Object[] arg, LuaObject func) throws LuaException {
        LuaAsyncTask task = new LuaAsyncTask(this, delay, func);
        task.execute(arg);
        return task;
    }

    public LuaAsyncTask task(LuaObject func) throws LuaException {
        return task(func, null, null, null);
    }

    public LuaAsyncTask task(LuaObject func, Object[] arg) throws LuaException {
        return task(func, arg, null, null);
    }

    public LuaAsyncTask task(LuaObject func, Object[] arg, LuaObject callback) throws LuaException {
        return task(func, null, null, callback);
    }

    public LuaAsyncTask task(LuaObject func, LuaObject update, LuaObject callback) throws LuaException {
        return task(func, null, update, callback);
    }

    public LuaAsyncTask task(LuaObject func, Object[] arg, LuaObject update, LuaObject callback) throws LuaException {
        LuaAsyncTask task = new LuaAsyncTask(this, func, update, callback);
        task.execute(arg);
        return task;
    }

    public LuaThread thread(LuaObject func) throws LuaException {
        LuaThread thread = newThread(func, new Object[0]);
        thread.start();
        return thread;
    }

    public LuaThread thread(LuaObject func, Object[] arg) throws LuaException {
        LuaThread thread = new LuaThread(this, func, true, arg);
        thread.start();
        return thread;
    }

    public LuaTimer timer(LuaObject func, long period) throws LuaException {
        return timer(func, 0, period, new Object[0]);
    }

    public LuaTimer timer(LuaObject func, long period, Object[] arg) throws LuaException {
        return timer(func, 0, period, arg);
    }

    public LuaTimer timer(LuaObject func, long delay, long period) throws LuaException {
        return timer(func, delay, period, new Object[0]);
    }

    public LuaTimer timer(LuaObject func, long delay, long period, Object[] arg) throws LuaException {
        LuaTimer timer = new LuaTimer(this, func, arg);
        timer.start(delay, period);
        return timer;
    }

    public Ticker ticker(final LuaObject func, long period) {
        Ticker timer = new Ticker();
        timer.setOnTickListener(() -> {
            try {
                func.call();
            } catch (LuaException e) {
                e.printStackTrace();
                sendError("onTick", e);
            }
        });
        timer.start();
        timer.setPeriod(period);
        return timer;
    }

    public void result(Object[] data) {
        Intent res = new Intent();
        res.putExtra(NAME, getIntent().getStringExtra(NAME));
        res.putExtra(DATA, data);
        setResult(0, res);
        finish();
    }

    //初始化lua使用的Java函数
    private void initLua() throws Exception {
        L = LuaStateFactory.newLuaState();
        L.openLibs();
        L.pushJavaObject(this);
        L.setGlobal("activity");
        L.getGlobal("activity");
        L.setGlobal("this");
        L.pushContext(this);
        L.getGlobal("luajava");
        L.pushString(luaExtDir);
        L.setField(-2, "luaextdir");
        L.pushString(luaDir);
        L.setField(-2, "luadir");
        L.pushString(luaPath);
        L.setField(-2, "luapath");
        L.pop(1);
        initENV();

        JavaFunction print = new LuaPrint(this, L);
        print.register("print");

        L.getGlobal("package");
        L.pushString(luaLPath);
        L.setField(-2, "path");
        L.pushString(luaCpath);
        L.setField(-2, "cpath");
        L.pop(1);

        JavaFunction set = new JavaFunction(L) {
            @Override
            public int execute() throws LuaException {
                LuaThread thread = (LuaThread) L.toJavaObject(2);

                thread.set(L.toString(3), L.toJavaObject(4));
                return 0;
            }
        };
        set.register("set");

        JavaFunction call = new JavaFunction(L) {
            @Override
            public int execute() throws LuaException {
                LuaThread thread = (LuaThread) L.toJavaObject(2);

                int top = L.getTop();
                if (top > 3) {
                    Object[] args = new Object[top - 3];
                    for (int i = 4; i <= top; i++) {
                        args[i - 4] = L.toJavaObject(i);
                    }
                    thread.call(L.toString(3), args);
                } else if (top == 3) {
                    thread.call(L.toString(3));
                }

                return 0;
            }

        };
        call.register("call");

    }

    public void setDebug(boolean isDebug) {
        mDebug = isDebug;
    }

    private void initENV() throws LuaException {
        if (!new File(luaDir + "/init.lua").exists()) return;

        try {
            int ok = L.LloadFile(luaDir + "/init.lua");
            if (ok == 0) {
                L.newTable();
                LuaObject env = L.getLuaObject(-1);
                L.setUpValue(-2, 1);
                ok = L.pcall(0, 0, 0);
                if (ok == 0) {
                    if (sKey == null) {
                        LuaObject key = env.getField("app_key");
                        if (key.isString()) {
                            sKey = key.toString();
                        }
                        LuaObject channel = env.getField("app_channel");
                    }

                    LuaObject title = env.getField("appname");
                    if (title.isString()) setTitle(title.getString());
                    title = env.getField("app_name");
                    if (title.isString()) setTitle(title.getString());

                    LuaObject debug = env.getField("debugmode");
                    if (debug.isBoolean()) mDebug = debug.getBoolean();
                    debug = env.getField("debug_mode");
                    if (debug.isBoolean()) mDebug = debug.getBoolean();

                    LuaObject theme = env.getField("theme");

                    if (theme.isNumber()) setTheme((int) theme.getInteger());
                    else if (theme.isString())
                        setTheme(android.R.style.class.getField(theme.getString()).getInt(null));
                    return;
                }
            }
            throw new LuaException(L.toString(-1));
        } catch (Exception e) {
            sendMsg(e.getMessage());
        }
    }

    Menu optionsMenu;

    public Menu getOptionsMenu() {
        return optionsMenu;
    }
//
//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        // TODO: Implement this method
//        if (!item.hasSubMenu())
//            runFunc("onMenuItemSelected", featureId, item);
//            runFunc("onMenuItemSelected", featureId, item);
//        return super.onMenuItemSelected(featureId, item);
//    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);
        ActivityManager.TaskDescription tDesc;
        try {
            tDesc = new ActivityManager.TaskDescription(title.toString(), LuaBitmap.getBitmap(this, getLuaPath("icon.png")));
        } catch (IOException e) {
            e.printStackTrace();
            tDesc = new ActivityManager.TaskDescription(title.toString());
        }
        setTaskDescription(tDesc);
    }


    //运行lua脚本
    public Object doFile(String filePath) {
        return doFile(filePath, new Object[0]);
    }

    public Object doFile(String filePath, Object[] args) {
        int ok = 0;
        try {
            if (filePath.charAt(0) != '/') filePath = luaDir + "/" + filePath;

            L.setTop(0);
            ok = L.LloadFile(filePath);

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);
                int l = args.length;
                for (int i = 0; i < l; i++) {
                    L.pushObjectValue(args[i]);
                }
                ok = L.pcall(l, 1, -2 - l);
                if (ok == 0) {
                    return L.toJavaObject(-1);
                }
            }
            Intent res = new Intent();
            res.putExtra(DATA, L.toString(-1));
            setResult(ok, res);
            throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
        } catch (LuaException e) {
            setTitle(errorReason(ok));
            setContentView(layout);
            sendMsg(e.getMessage());
            String s = e.getMessage();
            String p = "android.permission.";
            assert s != null;
            int i = s.indexOf(p);
            if (i > 0) {
                i = i + p.length();
                int n = s.indexOf(".", i);
                if (n > i) {
                    String m = s.substring(i, n);
                    L.getGlobal("require");
                    L.pushString("permission");
                    L.pcall(1, 0, 0);
                    L.getGlobal("permission_info");
                    L.getField(-1, m);
                    if (L.isString(-1)) m = m + " (" + L.toString(-1) + ")";
                    sendMsg("权限错误: " + m);
                    return null;
                }
            }

        }

        return null;
    }

    public Object doAsset(String name, Object[] args) {
        int ok = 0;
        try {
            byte[] bytes = readAsset(name);
            L.setTop(0);
            ok = L.LloadBuffer(bytes, name);

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);
                int l = args.length;
                for (int i = 0; i < l; i++) {
                    L.pushObjectValue(args[i]);
                }
                ok = L.pcall(l, 0, -2 - l);
                if (ok == 0) {
                    return L.toJavaObject(-1);
                }
            }
            throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
        } catch (Exception e) {
            setTitle(errorReason(ok));
            setContentView(layout);
            sendMsg(e.getMessage());
        }

        return null;
    }

    //运行lua函数
    public Object runFunc(String funcName, Object... args) {
        if (L != null) {
            synchronized (L) {
                try {
                    L.setTop(0);
                    L.pushGlobalTable();
                    L.pushString(funcName);
                    L.rawGet(-2);
                    if (L.isFunction(-1)) {
                        L.getGlobal("debug");
                        L.getField(-1, "traceback");
                        L.remove(-2);
                        L.insert(-2);

                        int l = args.length;
                        for (int i = 0; i < l; i++) {
                            L.pushObjectValue(args[i]);
                        }

                        int ok = L.pcall(l, 1, -2 - l);
                        if (ok == 0) {
                            return L.toJavaObject(-1);
                        }
                        throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
                    }
                } catch (LuaException e) {
                    sendError(funcName, e);
                }
            }
        }
        return null;
    }

    //运行lua代码
    public Object doString(String funcSrc, Object... args) {
        try {
            L.setTop(0);
            int ok = L.LloadString(funcSrc);

            if (ok == 0) {
                L.getGlobal("debug");
                L.getField(-1, "traceback");
                L.remove(-2);
                L.insert(-2);

                int l = args.length;
                for (int i = 0; i < l; i++) {
                    L.pushObjectValue(args[i]);
                }

                ok = L.pcall(l, 1, -2 - l);
                if (ok == 0) {
                    return L.toJavaObject(-1);
                }
            }
            throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
        } catch (LuaException e) {
            sendMsg(e.getMessage());
        }
        return null;
    }

//读取asset文件

    //生成错误信息
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

    public byte[] readAsset(String name) throws IOException {
        AssetManager am = getAssets();
        InputStream is = am.open(name);
        byte[] ret = readAll(is);
        is.close();
        //am.close();
        return ret;
    }

//    //复制asset文件到sd卡
//    public void assetsToSD(String InFileName, String OutFileName) throws IOException {
//        InputStream myInput;
//        OutputStream myOutput = new FileOutputStream(OutFileName);
//        myInput = this.getAssets().open(InFileName);
//        byte[] buffer = new byte[4096];
//        int length = myInput.read(buffer);
//        while (length > 0) {
//            myOutput.write(buffer, 0, length);
//            length = myInput.read(buffer);
//        }
//
//        myOutput.flush();
//        myInput.close();
//        myOutput.close();
//    }
//
//    public void showLogs() {
//        new AlertDialog.Builder(this).setTitle("Logs").setAdapter(adapter, null).setPositiveButton(android.R.string.ok, null).create().show();
//    }

    //显示信息
    public void sendMsg(String msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, msg);
        message.setData(bundle);
        message.what = 0;
        handler.sendMessage(message);
        Log.i("lua", msg);
    }

    public void sendMsg(String msg, Object obj) {
        sendMsg(msg);
    }

    public void sendMsg(Object obj, String msg) {
        sendMsg(msg);
    }

    @Override
    public void sendError(String title, Exception msg) {
        Object ret = runFunc("onError", title, msg);
        if (ret == null || ret.getClass() != Boolean.class) {
            sendMsg(title + ": " + msg.getMessage());
        }
    }

    public void crash() {
        throw new NullPointerException();
    }

    private AlertDialog toastDialog = null;
    //显示toast
    @SuppressLint("ShowToast")
    public void showToast(String text) {
        long now = System.currentTimeMillis();
        if (toast == null || now - lastShow > 1000) {
            toastBuilder.setLength(0);
            toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toastBuilder.append(text);
//            toast.show();
            new AlertDialog.Builder(this).setTitle("Lua Log").setMessage(text).show();
        } else {
            toastBuilder.append("\n");
            toastBuilder.append(text);
            toast.setText(toastBuilder.toString());
            toast.setDuration(Toast.LENGTH_LONG);
        }
        lastShow = now;
    }

    private void setField(String key, Object value) {
        synchronized (L) {
            try {
                L.pushObjectValue(value);
                L.setGlobal(key);
            } catch (LuaException e) {
                sendError("setField", e);
            }
        }
    }

    public void call(String func) {
        push(2, func);

    }

    public void call(String func, Object[] args) {
        if (args.length == 0) push(2, func);
        else push(3, func, args);
    }

    public void set(String key, Object value) {
        push(1, key, new Object[]{value});
    }

    public Object get(String key) throws LuaException {
        synchronized (L) {
            L.getGlobal(key);
            return L.toJavaObject(-1);
        }
    }

    public void push(int what, String s) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, s);
        message.setData(bundle);
        message.what = what;

        handler.sendMessage(message);

    }

    public void push(int what, String s, Object[] args) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(DATA, s);
        bundle.putSerializable("args", args);
        message.setData(bundle);
        message.what = what;

        handler.sendMessage(message);
    }

    public void putToApplication(String name, Object value) {
        ((LuaApplication) getApplication()).set(name, value);
    }

    public Object getFromApplication(String name) {
        return ((LuaApplication) getApplication()).get(name);
    }

    public class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0: {
                    String data = msg.getData().getString(DATA);
                    if (mDebug) showToast(data);
                    status.append(data + "\n");
                    adapter.add(data);
                }
                break;
                case 1: {
                    Bundle data = msg.getData();
                    setField(data.getString(DATA), ((Object[]) data.getSerializable("args"))[0]);
                }
                break;
                case 2: {
                    String src = msg.getData().getString(DATA);
                    runFunc(src);
                }
                break;
                case 3: {
                    String src = msg.getData().getString(DATA);
                    Serializable args = msg.getData().getSerializable("args");
                    runFunc(src, (Object[]) args);
                }
            }
        }
    }
}
