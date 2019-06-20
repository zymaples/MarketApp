package com.sqh.market;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication mInstance;

    public static MyApplication getInstance(){
        if(mInstance == null){
            mInstance = new MyApplication();
        }
        return mInstance;
    }
}
