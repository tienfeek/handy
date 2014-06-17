package com.tienfeek.handy;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.tienfeek.handy.utils.SystemBarTintManager;

public class BaseActivity extends Activity {
    
    private SystemBarTintManager mTintManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        initSystemBar();
    }
    
    
    
    
    private void initSystemBar() {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
        }
        
        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setNavigationBarTintEnabled(true);
        mTintManager.setTintColor(getResources().getColor(R.color.main_bg));
        mTintManager.setStatusBarTintResource(R.color.main_bg);
    }
}
