package com.example.ui;

import android.app.Application;

/**
 * Created by lizhiyun on 16/6/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //在这里为应用设置异常处理程序，然后我们的程序才能捕获未处理的异常
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }
}
