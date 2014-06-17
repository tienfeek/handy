/**
 * 
 */
package com.tienfeek.handy;

import com.tienfeek.handy.utils.XLog;

import android.app.Application;


public class HandyApplication extends Application {
    
    private static HandyApplication mInstance;
    
   
    public HandyApplication(){
        mInstance = this;
    }
    
    public static HandyApplication getInstance(){
        return mInstance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        XLog.setLogLevel(1);
        
    }
    
    
    
    
}
