package com.clj.blesample.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clj.blesample.R;
import com.clj.blesample.uitls.BytesADUtils;

public class MyAdActivity extends AppCompatActivity {
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        //checkPermissions();

        findViewById(R.id.ad_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(MyAdActivity.this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
                    return;
                } else {
                    getAdvertiser();
                }
                if (flag) {
                    stopAction(v);
                } else {
                    startAction(v);
                }
                flag = !flag;
            }
        });
    }

    private void getAdvertiser() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    private int times = 0;

    public void startAction(View v) {
        times++;
        byte[] broadcastData;
        if (times % 3 == 0) {
            broadcastData = new BytesADUtils(null).get0x02Bytes();
        } else {
            broadcastData = new BytesADUtils(null).get0x01Bytes();
        }
        //broadcastData = new BytesADUtils("200527").get0x10Bytes();
        //System.out.println("####" + HexUtil.encodeHexStr(broadcastData));
        AdvertiseSettings settings = createAdvSettings(true, 0);
        AdvertiseData advertiseData = createAdvertiseData(broadcastData);
        AdvertiseCallback callback = mAdvertiseCallback;
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, callback);
    }

    public void stopAction(View v) {
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
        mDataBuilder.addManufacturerData(0xFFFF, data);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);

            Toast.makeText(MyAdActivity.this, "发送广播成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Toast.makeText(MyAdActivity.this, "发送广播失败", Toast.LENGTH_LONG).show();
        }
    };

}
