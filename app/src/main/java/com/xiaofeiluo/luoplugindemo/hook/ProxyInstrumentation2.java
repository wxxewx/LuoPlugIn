package com.xiaofeiluo.luoplugindemo.hook;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.xiaofeiluo.luoplugin.StubActivity;
import com.xiaofeiluo.luoplugindemo.MainActivity;

import org.joor.Reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class ProxyInstrumentation2 extends Instrumentation {
    private Context mContext;
    private Instrumentation mBase;
    private Class mainActivity;
    private DexClassLoader localDexClassLoader;
    private String plugPath;

    public ProxyInstrumentation2(Context activity, Instrumentation instrumentation, Class mainActivity, DexClassLoader localDexClassLoader, String plugPath) {
        this.mContext = activity;
        this.mBase = instrumentation;
        this.mainActivity = mainActivity;
        this.localDexClassLoader = localDexClassLoader;
        this.plugPath = plugPath;
    }


    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
//        Intent intent1 = new Intent(mContext, mainActivity);
        Intent oldIntent = intent.getParcelableExtra("hook");
        if (oldIntent != null) {
            Activity activity = mBase.newActivity(localDexClassLoader, oldIntent.getComponent().getClassName(), oldIntent);

            return activity;
        }
        return mBase.newActivity(cl,className,intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
//        AssetManager assets = activity.getResources().getAssets();
//        Reflect.on(assets).call("addAssetPath", "/data/user/0/com.xiaofeiluo.luoplugindemo/cache/plug.apk");
//        Reflect.on(activity).set("mTheme", mContext.getTheme());


        mBase.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
//        AssetManager assets = activity.getResources().getAssets();
//        Reflect.on(assets).call("addAssetPath", "/data/user/0/com.xiaofeiluo.luoplugindemo/cache/plug.apk");
//        Reflect.on(activity).set("mTheme", mContext.getTheme());
    try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Reflect.on(assetManager).call("addAssetPath", "/data/user/0/com.xiaofeiluo.luoplugindemo/cache/plug.apk");
            Resources resources = new Resources(assetManager,
                    mContext.getResources().getDisplayMetrics(),
                    mContext.getResources().getConfiguration());
            Reflect.on(activity).set("mResources", resources);
            Reflect.on(activity).set("mTheme", mContext.getTheme());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        mBase.callActivityOnCreate(activity, icicle);
    }
}
