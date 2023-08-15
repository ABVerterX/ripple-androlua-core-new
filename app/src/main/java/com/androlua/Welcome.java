package com.androlua;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.luajava.LuaFunction;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import vinx.abverterx.ripplelua.core.R;

public class Welcome extends AppCompatActivity {

    private LuaApplication luaApplication;

    private String luaModuleDir;
    private String dataFileDir;

    private boolean isVersionChanged;
    private String newVersionName;
    private String oldVersionName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        luaApplication = (LuaApplication) getApplication();
        luaModuleDir = luaApplication.luaMdDir;
        dataFileDir = luaApplication.localDir;
        if (checkInfo()) {
            new UpdateTask().execute();
        } else {
            startActivity();
        }
    }

    public void startActivity() {
        Intent intent = new Intent(Welcome.this, Main.class);
        if (isVersionChanged) {
            intent.putExtra("isVersionChanged", true);
            intent.putExtra("newVersionName", newVersionName);
            intent.putExtra("oldVersionName", oldVersionName);
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
        finish();
    }

    public boolean checkInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);

            long lastTime = packageInfo.lastUpdateTime;
            String versionName = packageInfo.versionName;

            SharedPreferences info = getSharedPreferences("appInfo", 0);
            String oldVersionName = info.getString("versionName", "");
            if (!versionName.equals(oldVersionName)) {
                SharedPreferences.Editor edit = info.edit();
                edit.putString("versionName", versionName);
                edit.apply();
                isVersionChanged = true;
                newVersionName = versionName;
                this.oldVersionName = oldVersionName;
            }
            long oldLastTime = info.getLong("lastUpdateTime", 0);
            if (oldLastTime != lastTime) {
                SharedPreferences.Editor edit = info.edit();
                edit.putLong("lastUpdateTime", lastTime);
                edit.apply();
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return !new File(luaApplication.getLuaPath("main.lua")).exists();
    }


    @SuppressLint("StaticFieldLeak")
    private class UpdateTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String[] p1) {
            // TODO: Implement this method
            onUpdate();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            startActivity();
        }

        private void onUpdate() {

            LuaState luaState = LuaStateFactory.newLuaState();
            luaState.openLibs();
            try {
                if (luaState.LloadBuffer(LuaUtil.readAsset(Welcome.this, "update.lua"), "update") == 0) {
                    if (luaState.pcall(0, 0, 0) == 0) {
                        LuaFunction func = luaState.getFunction("onUpdate");
                        if (func != null) func.call(newVersionName, oldVersionName);
                    }
                }

            } catch (Exception e) {
                // e.printStackTrace();
            }

            try {
                unzipApk("assets", dataFileDir);
                unzipApk("lua", luaModuleDir);
            } catch (IOException e) {
                sendMsg(e.getMessage());
            }
        }

        private void sendMsg(String message) {
            // TODO: Implement this method

        }

        private void unzipApk(String dir, String extDir) throws IOException {
            int i = dir.length() + 1;
            ZipFile zip = new ZipFile(getApplicationInfo().publicSourceDir);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.indexOf(dir) != 0) continue;
                String path = name.substring(i);
                if (entry.isDirectory()) {
                    File f = new File(extDir + File.separator + path);
                    if (!f.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        f.mkdirs();
                    }
                } else {
                    String fname = extDir + File.separator + path;
                    File ff = new File(fname);
                    File temp = new File(fname).getParentFile();
                    assert temp != null;
                    if (!temp.exists()) {
                        if (!temp.mkdirs()) {
                            throw new RuntimeException("create file " + temp.getName() + " fail");
                        }
                    }
                    try {
                        if (ff.exists() && entry.getSize() == ff.length() && Objects.equals(LuaUtil.getFileMD5(zip.getInputStream(entry)), LuaUtil.getFileMD5(ff)))
                            continue;
                    } catch (NullPointerException ignored) {
                    }
                    FileOutputStream out = new FileOutputStream(extDir + File.separator + path);
                    InputStream in = zip.getInputStream(entry);
                    byte[] buf = new byte[4096];
                    int count;
                    while ((count = in.read(buf)) != -1) {
                        out.write(buf, 0, count);
                    }
                    out.close();
                    in.close();
                }
            }
            zip.close();
        }

    }
}
