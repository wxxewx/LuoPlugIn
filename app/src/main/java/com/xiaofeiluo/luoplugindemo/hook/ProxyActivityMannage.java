package com.xiaofeiluo.luoplugindemo.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.xiaofeiluo.luoplugin.StubActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyActivityMannage implements InvocationHandler {
    private Object mInstance;

    public ProxyActivityMannage(Object mInstance) {

        this.mInstance = mInstance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.e("ProxyActivityMannage", "hook成功了");
        if (method.getName().equals("startActivity")) {
            Intent raw;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];
            Intent newintent = new Intent();
            //替身 Activity的包名 ， 也就是我们自己的包名
            String stubPackage = raw.getComponent().getPackageName();
            //这里我们把启动的 Activity临时替换为 StubActiv工ty
            ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
            newintent.setComponent(componentName);//把我们原始要启动的 TargetActivity先存起来
            newintent.putExtra("hook", raw);
            //替换掉 Intent， 达到欺骗AMS的目的
            args[index] = newintent;
            return method.invoke(mInstance, args);
        }
        return method.invoke(mInstance, args);
    }
}
