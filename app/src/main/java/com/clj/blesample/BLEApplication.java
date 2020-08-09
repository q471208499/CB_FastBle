package com.clj.blesample;

import cn.cb.baselibrary.BaseApplication;
import es.dmoral.toasty.MyToast;

public class BLEApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MyToast.init(this, true, true);
    }
}
