package com.clj.blesample.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.R;
import com.clj.blesample.dialog.AdDialog;
import com.clj.blesample.listener.AdBtnCallback;
import com.clj.fastble.utils.HexUtil;

import cn.cb.baselibrary.activity.BaseActivity;
import cn.cb.baselibrary.utils.ABTimeUtils;
import es.dmoral.toasty.MyToast;

public class MyAdActivity extends BaseActivity {
    private final String TAG = getClass().getSimpleName();

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private RadioGroup radioGroup;
    private TextView adLog;
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        initBarView();
        //checkPermissions();
        radioGroup = findViewById(R.id.ad_radio_group);
        adLog = findViewById(R.id.ad_log);

        findViewById(R.id.ad_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        new AdDialog(MyAdActivity.this, radioGroup, adBtnCallback).show();
                        dismissLoading();
                    }
                });
            }
        });
    }

    private AdBtnCallback adBtnCallback = new AdBtnCallback() {
        @Override
        public void positive(String hexStr, byte[] broadcastData) {
            MyToast.show(hexStr);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(MyAdActivity.this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
                return;
            } else {
                getAdvertiser();
            }
            startAction(broadcastData);
            StringBuilder sb = new StringBuilder();
            sb.append("【");
            sb.append(ABTimeUtils.getCurrentTimeInString(ABTimeUtils.DEFAULT_DATE_FORMAT));
            sb.append("】");
            sb.append(HexUtil.encodeHexStr(broadcastData, false));
            sb.append("\n");
            sb.append(adLog.getText().toString());
            adLog.setText(sb.toString());
            Log.i(TAG, "positive: " + HexUtil.encodeHexStr(broadcastData, false));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopAction();
                }
            }, 3000);
        }

        @Override
        public void negative() {

        }
    };

    private void getAdvertiser() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    public void startAction(byte[] broadcastData) {
        //byte[] broadcastData = new BytesADUtils(hexStr).get0x12Bytes();
        //broadcastData = new BytesADUtils(null).get0x02Bytes();
        //broadcastData = new BytesADUtils(null).get0x01Bytes();
        //broadcastData = new BytesADUtils("200527").get0x10Bytes();
        //System.out.println("####" + HexUtil.encodeHexStr(broadcastData));
        AdvertiseSettings settings = createAdvSettings(true, 0);
        AdvertiseData advertiseData = createAdvertiseData(broadcastData);
        AdvertiseCallback callback = mAdvertiseCallback;
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, callback);
    }

    public void stopAction() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        Toast.makeText(MyAdActivity.this, "关闭广播成功", Toast.LENGTH_LONG).show();
    }

    public AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        //mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
        return mAdvertiseSettings;
    }

    public AdvertiseData createAdvertiseData(byte[] data) {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        //mDataBuilder.addManufacturerData(0xFFFF, data);
        mDataBuilder.setIncludeDeviceName(false);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "onStartSuccess: ");
            Toast.makeText(MyAdActivity.this, "发送广播成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "onStartFailure: 发送广播失败" + errorCode);
            Toast.makeText(MyAdActivity.this, "发送广播失败" + errorCode, Toast.LENGTH_LONG).show();
        }
    };

}
