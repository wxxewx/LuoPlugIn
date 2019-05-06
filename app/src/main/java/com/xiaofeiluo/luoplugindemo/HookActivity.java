package com.xiaofeiluo.luoplugindemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiaofeiluo.luoplugindemo.hook.ProxyActivityMannage;
import com.xiaofeiluo.luoplugindemo.hook.ProxyCallBack;
import com.xiaofeiluo.luoplugindemo.hook.ProxyInstrumentation;
import com.xiaofeiluo.luoplugindemo.hook.ProxyInstrumentation2;

import org.joor.Reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import dalvik.system.DexClassLoader;

public class HookActivity extends AppCompatActivity {

    private Button button1;

    private Resources plugResources;
    AssetManager assets = null;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlugActivity("plug.apk");
            }
        });


    }

    @Override
    public Resources.Theme getTheme() {
        return super.getTheme();
    }

    private void startPlugActivity(String plugName) {
        String assetsPlugPath = getAssetsPlugPath(plugName);
        String dexoutputpath = "/mnt/sdcard/";
        DexClassLoader localDexClassLoader = new DexClassLoader(assetsPlugPath, dexoutputpath, null, getClassLoader());
        try {
            Class mainActivity = localDexClassLoader.loadClass("com.xiaofeiluo.viewtoimagedemo.MainActivity2");
            Object instance = mainActivity.newInstance();
            Intent intent = new Intent(this, mainActivity);

            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            Object getService = Reflect.on(activityManager).call("getService").get();
            Object iActivityManagerSingleton = Reflect.on(activityManager).field("IActivityManagerSingleton").get();
            Object mInstance = Reflect.on(iActivityManagerSingleton).field("mInstance").get();


            Class<?> aClass = Class.forName("android.app.IActivityManager");


            Object newProxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{aClass},
                    new ProxyActivityMannage(mInstance));
            Reflect.on(iActivityManagerSingleton).set("mInstance", newProxyInstance);

            //hook activityThread H 类 callback


            //先获取到当前的 ActivityThread对象
//            Object sCurrentActivityThread = Reflect.on("android.app.ActivityThread").call("currentActivityThread").get();//由于ActivityThread一个进程只有一个，我们获取这个对象的 rnH
//            Handler mH = Reflect.on(sCurrentActivityThread).field("mH").get();
//            //把Handler的rnCallback字段，替换为 new MockClass2(rnH)
//            setFieldObject("android.os.Handler", mH, "mCallback", new ProxyCallBack(mH));

            Object sCurrentActivityThread = Reflect.on("android.app.ActivityThread").call("currentActivityThread").get();//由于ActivityThread一个进程只有一个，我们获取这个对象的 rnH
            Instrumentation mInstrumentation1 = Reflect.on(sCurrentActivityThread).field("mInstrumentation").get();
            ProxyInstrumentation2 proxyInstrumentation2 = new ProxyInstrumentation2(this, mInstrumentation1, mainActivity, localDexClassLoader, assetsPlugPath);
            Reflect.on(sCurrentActivityThread).set("mInstrumentation", proxyInstrumentation2);



            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }



    private String getAssetsPlugPath(String plugName) {
        boolean b = copyAssetAndWrite(plugName);
        if (b) {
            File dataFile = new File(getCacheDir(), plugName);
            Log.d("getAssetsPlugPath", "filePath:" + dataFile.getPath());
            return dataFile.getAbsolutePath();
        }
        return null;
    }

    private boolean copyAssetAndWrite(String fileName) {
        try {
            File cacheDir = getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File outFile = new File(cacheDir, fileName);
            if (!outFile.exists()) {
                boolean res = outFile.createNewFile();
                if (!res) {
                    return false;
                }
            } else {
                if (outFile.length() > 10) {//表示已经写入一次
                    return true;
                }
            }
            InputStream is = getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void setFieldObject(String classname, Object obj, String filedName, Object filedVaule) {
        try {
            Class obj_class = Class.forName(classname);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            field.set(obj, filedVaule);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getFieldObject(String className, Object obj, String filedName) {
        try {
            Class obj_class = Class.forName(className);
            Field field = obj_class.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (
                Exception e) {
            e.printStackTrace();
            return null;

        }
    }
}
