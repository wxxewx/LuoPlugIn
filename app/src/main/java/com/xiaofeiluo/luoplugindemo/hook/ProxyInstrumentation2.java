package com.xiaofeiluo.luoplugindemo.hook;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
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
        Activity activity = mBase.newActivity(localDexClassLoader, oldIntent.getComponent().getClassName(), oldIntent);
        return activity;
    }



}
