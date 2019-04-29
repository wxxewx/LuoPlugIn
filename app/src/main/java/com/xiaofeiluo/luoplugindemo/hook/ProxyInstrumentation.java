package com.xiaofeiluo.luoplugindemo.hook;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.xiaofeiluo.luoplugin.StubActivity;
import com.xiaofeiluo.luoplugindemo.MainActivity;

import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class ProxyInstrumentation extends Instrumentation {
    private Context mContext;
    private Instrumentation mBase;
    private Class mainActivity;
    private DexClassLoader localDexClassLoader;

    public ProxyInstrumentation(Context activity, Instrumentation instrumentation, Class mainActivity, DexClassLoader localDexClassLoader) {
        this.mContext = activity;
        this.mBase = instrumentation;
        this.mainActivity = mainActivity;
        this.localDexClassLoader = localDexClassLoader;
    }


    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        Intent stubIntent = new Intent(who, StubActivity.class);
        // Hook之前, XXX到此一游!
        Log.e("ProxyInstrumentation", "\n执行了startActivity, 参数如下: \n" + "who = [" + who + "], " +
                "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                "\ntarget = [" + target + "], \nintent = [" + intent +
                "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");

        // 开始调用原始的方法, 调不调用随你,但是不调用的话, 所有的startActivity都失效了.
        // 由于这个方法是隐藏的,因此需要使用反射调用;首先找到这个方法
        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(mBase, who,
                    contextThread, token, target, stubIntent, requestCode, options);
        } catch (Exception e) {
            // 某该死的rom修改了  需要手动适配
            throw new RuntimeException("do not support!!! pls adapt it");
        }
    }



    public Activity newActivity(Class<?> clazz, Context context,
                                IBinder token, Application application, Intent intent, ActivityInfo info,
                                CharSequence title, Activity parent, String id,
                                Object lastNonConfigurationInstance) throws InstantiationException,
            IllegalAccessException {
        return mBase.newActivity(MainActivity.class, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }



    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        Intent intent1 = new Intent(mContext, mainActivity);
        return mBase.newActivity(localDexClassLoader,"com.xiaofeiluo.viewtoimagedemo.MainActivity2", intent1);
    }
}
