package com.tienfeek.handy;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends BaseActivity implements OnClickListener{
    
    private SwipeRefreshLayout swipeLayout;
    private ListView mListView;
    private LinearLayout mSearchLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_layout);
        
    }
    
    private void findView(){
        
    }

    
    @Override
    public void onClick(View v) {
        
    }
    
    
    
}
