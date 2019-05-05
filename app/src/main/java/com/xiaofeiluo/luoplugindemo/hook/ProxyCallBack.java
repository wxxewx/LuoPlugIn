package com.xiaofeiluo.luoplugindemo.hook;


import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import org.joor.Reflect;

public class ProxyCallBack implements Handler.Callback {


    private Handler.Callback mCallback;

    public ProxyCallBack(Handler.Callback mCallback) {

        this.mCallback = mCallback;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 100) {
        //这里简单起见 ， 直接取出 TargetActivity
            Object obj = msg.obj;
            //把替身恢复成真身
            Intent intent = (Intent) Reflect.on(obj).field("intent").get();
            Intent targetintent = intent.getParcelableExtra("hook");
            intent.setComponent(targetintent.getComponent());
        }
        mCallback.handleMessage(msg);
        return true;
    }
}
