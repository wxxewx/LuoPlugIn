package com.xiaofeiluo.luoplugindemo.hook;


import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import org.joor.Reflect;

public class ProxyCallBack implements Handler.Callback {


    private Handler base;

    public ProxyCallBack(Handler base) {

        this.base = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 100) {
            //这里简单起见 ， 直接取出 TargetActivity
            Object obj = msg.obj;
            //把替身恢复成真身
            Intent intent = (Intent) Reflect.on(obj).field("intent").get();
            Intent targetintent = intent.getParcelableExtra("hook");
            if (targetintent != null) {
                Reflect.on(obj).set("intent", targetintent);
                base.handleMessage(msg);
            }
        }
        base.handleMessage(msg);

        return true;
    }
}
