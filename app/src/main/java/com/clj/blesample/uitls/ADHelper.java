package com.clj.blesample.uitls;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.util.Log;

import com.clj.fastble.utils.HexUtil;

public class ADHelper {
    private final String TAG = getClass().getSimpleName();

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private Context mContext;

    public ADHelper(Context context) {
        this.mContext = context;
        getAdvertiser();
    }

    public void startActionTest() {
        startAction(new BytesADUtils("200527").get0x10Bytes());
    }

    public void startAction0x10(String data, String mac) {
        startAction(new BytesADUtils(data, mac).get0x10Bytes());
    }

    public void startAction0x11(String data, String mac) {
        startAction(new BytesADUtils(data, mac).get0x11Bytes());
    }

    public void startAction(byte[] data) {
        Log.i(TAG, "###startAction: " + HexUtil.encodeHexStr(data));
        AdvertiseSettings settings = createAdvSettings(true, 3 * 1000);
        AdvertiseData advertiseData = createAdvertiseData(data);
        AdvertiseCallback callback = mAdvertiseCallback;
        mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, callback);
    }

    public void stopAction() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        //Toast.makeText(MyAdActivity.this, "关闭广播成功", Toast.LENGTH_LONG).show();
    }

    private AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        //mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
        return mAdvertiseSettings;
    }

    private AdvertiseData createAdvertiseData(byte[] data) {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.addManufacturerData(0xFFFF, data);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "###onStartSuccess: 发送广播成功");
            //stopAction();
            //Toast.makeText(mContext, "发送广播成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.i(TAG, "###onStartFailure: 发送广播失败" + errorCode);
            //stopAction();
            //Toast.makeText(mContext, "发送广播失败", Toast.LENGTH_LONG).show();
        }
    };

    private void getAdvertiser() {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }
}
