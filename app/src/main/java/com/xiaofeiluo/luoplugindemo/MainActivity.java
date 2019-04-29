package com.xiaofeiluo.luoplugindemo;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiaofeiluo.luoplugindemo.hook.ProxyInstrumentation;

import org.joor.Reflect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private Button button1;

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

    private void startPlugActivity(String plugName) {
        String assetsPlugPath = getAssetsPlugPath(plugName);
        String dexoutputpath = "/mnt/sdcard/";
        ClassLoader localClassLoader = getClass().getClassLoader();
        DexClassLoader localDexClassLoader = new DexClassLoader(assetsPlugPath, dexoutputpath, null, localClassLoader);
        try {
            Class mainActivity = localDexClassLoader.loadClass("com.xiaofeiluo.viewtoimagedemo.MainActivity2");
            Object instance = mainActivity.newInstance();
            Intent intent = new Intent(this, mainActivity);
            //这里要对starActivity进行hook
            hook(mainActivity, localDexClassLoader,assetsPlugPath);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void hook(Class mainActivity, DexClassLoader localDexClassLoader, String assetsPlugPath) {
        //替换activity的mInstrumentation
        Instrumentation mInstrumentation = Reflect.on(this).field("mInstrumentation").get();
        ProxyInstrumentation proxyInstrumentation = new ProxyInstrumentation(this, mInstrumentation, mainActivity, localDexClassLoader);
        Reflect.on(this).set("mInstrumentation", proxyInstrumentation);
        //替换activityThread的mInstrumentation
        Object mMainThread = Reflect.on(this).field("mMainThread").get();
        Instrumentation mInstrumentation1 = Reflect.on(mMainThread).field("mInstrumentation").get();
        ProxyInstrumentation proxyInstrumentation1 = new ProxyInstrumentation(this, mInstrumentation1, mainActivity, localDexClassLoader);
        Reflect.on(mMainThread).set("mInstrumentation", proxyInstrumentation1);

        Resources resources = getResources();
        AssetManager am = getResources().getAssets();
        Reflect.on(am).call("addAssetPath",assetsPlugPath);
    }



    private String getAssetsPlugPath(String plugName) {
        boolean b = copyAssetAndWrite(plugName);
        if (b) {
            File dataFile = new File(getCacheDir(), plugName);
            Log.d("getAssetsPlugPath", "filePath:" + dataFile.getAbsolutePath());
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
}
