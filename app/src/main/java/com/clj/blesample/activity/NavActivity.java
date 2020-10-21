package com.clj.blesample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.clj.blesample.R;

import cn.cb.baselibrary.activity.BaseActivity;

public class NavActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);

        findViewById(R.id.nav_main).setOnClickListener(listener);
        findViewById(R.id.nav_scan).setOnClickListener(listener);
        findViewById(R.id.nav_scan_connect).setOnClickListener(listener);
        findViewById(R.id.nav_ad).setOnClickListener(listener);
        findViewById(R.id.nav_bt).setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Class cls = null;
            String title = "";
            if (v.getId() == R.id.nav_main) {
                cls = MainActivity.class;
                title = "main";
            } else if (v.getId() == R.id.nav_scan) {
                cls = BleScanActivity.class;
                title = "scan";
            } else if (v.getId() == R.id.nav_ad) {
                cls = MyAdActivity.class;
                title = "ad ";
            } else if (v.getId() == R.id.nav_scan_connect) {
                cls = BleScanConnectActivity.class;
                title = "scan & connect";
            } else if (v.getId() == R.id.nav_bt) {
                cls = BTActivity.class;
                title = "经典蓝牙";
            }
            Intent intent = new Intent(NavActivity.this, cls);
            intent.putExtra(Intent.EXTRA_TITLE, title);
            startActivity(intent);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showFinishDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}