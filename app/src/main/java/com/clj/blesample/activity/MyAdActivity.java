package com.clj.blesample.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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
import com.clj.fastble.activity.BleBaseActivity;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.project.hk.BytesScanUtils;
import com.clj.fastble.utils.HexUtil;

import java.util.List;

import cn.cb.baselibrary.utils.ABTimeUtils;
import es.dmoral.toasty.Toasty;

public class MyAdActivity extends BleBaseActivity {
    private final String TAG = getClass().getSimpleName();

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private RadioGroup radioGroup;
    private TextView adLog, adMac;
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private String order;
    private final String order_search = "search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        initBarView();
        //checkPermissions();

        setScanCallback();
        bindView();
    }

    private void bindView() {
        radioGroup = findViewById(R.id.ad_radio_group);
        adLog = findViewById(R.id.ad_log);
        adMac = findViewById(R.id.ad_mac);
        findViewById(R.id.ad_btn).setOnClickListener(clickListener);
        findViewById(R.id.ad_search).setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ad_btn:
                    showLoading();
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            boolean gone = adMac.getVisibility() == View.GONE;
                            String macAddress = gone ? "112233445566" : adMac.getText().toString().replaceAll(":", "");
                            new AdDialog(MyAdActivity.this, radioGroup, adBtnCallback, macAddress).show();
                            dismissLoading();
                        }
                    });
                    break;
                case R.id.ad_search:
                    order = order_search;
                    showLoading(SCAN_TIME_OUT / 1000);
                    goScan();
                    break;
            }
        }
    };

    private void setScanCallback() {
        callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                Log.i(TAG, "onScanResult: " + device.getAddress());
                BleDevice bleDevice = new BleDevice(device, result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
                BytesScanUtils scanUtils = new BytesScanUtils(bleDevice.getScanRecord());
                if (!scanUtils.isValid()) {
                    return;
                }
                //scanUtils.getOrder();
                if (order.equals(order_search)) {
                    int rssi = result.getRssi();
                    if (rssi > -50) {
                        String address = device.getAddress();
                        adMac.setText(address);
                        adMac.setVisibility(View.VISIBLE);
                        dismissLoading();
                    }
                } else if (order.equals(BytesScanUtils.ORDER_14) || order.equals(BytesScanUtils.ORDER_15)) {
                    dismissLoading();
                    addLog(scanUtils.getAllData());
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    @Override
    protected void startScanBLE() {
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();
        leScanner.startScan(null, settings, callback);
        Toasty.info(this, getString(com.clj.fastble.R.string.start_scan));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (leScanner != null) {
                    leScanner.stopScan(callback);
                }
                Toasty.info(MyAdActivity.this, getString(com.clj.fastble.R.string.stop_scan));
            }
        }, SCAN_TIME_OUT);
    }

    private AdBtnCallback adBtnCallback = new AdBtnCallback() {
        @Override
        public void positive(String hexStr, byte[] broadcastData, String o) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(MyAdActivity.this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
                return;
            } else {
                getAdvertiser();
            }
            startAction(broadcastData);
            addLog(HexUtil.encodeHexStr(broadcastData, false));
            Log.i(TAG, "positive: " + HexUtil.encodeHexStr(broadcastData, false));
            order = o;
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

    private void addLog(String data) {
        StringBuilder sb = new StringBuilder();
        sb.append("【");
        sb.append(ABTimeUtils.getCurrentTimeInString(ABTimeUtils.DEFAULT_DATE_FORMAT));
        sb.append("】");
        sb.append(data);
        sb.append("\n");
        sb.append(adLog.getText().toString());
        adLog.setText(sb.toString());
    }

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
        if (data.length > 26) {
            throw new ArrayIndexOutOfBoundsException("Android 广播长度最大 24");
        }
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        byte[] d = new byte[data.length - 2];
        for (int i = 0; i < d.length; i++) {
            d[i] = data[i + 2];
        }
        mDataBuilder.addManufacturerData(0x6A73, d);
        //Log.i(TAG, "createAdvertiseData: " + HexUtil.formatHexString(data, true));
        //mDataBuilder.addServiceData(ParcelUuid.fromString(String.valueOf(UUID.randomUUID())), data);
        //String uuid = UUID.randomUUID().toString();
        //Log.i(TAG, "createAdvertiseData: " + uuid);
        //mDataBuilder.addServiceUuid(ParcelUuid.fromString(uuid));
        //mDataBuilder.setIncludeDeviceName(true);
        //mDataBuilder.setIncludeTxPowerLevel(true);
        return mDataBuilder.build();
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG, "onStartSuccess: ");
            Toast.makeText(MyAdActivity.this, "发送广播成功", Toast.LENGTH_LONG).show();
            if (BytesScanUtils.ORDER_14.equals(order) || BytesScanUtils.ORDER_15.equals(order)) {
                goScan();
                showLoading(SCAN_TIME_OUT / 1000);
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            dismissLoading();
            Log.e(TAG, "onStartFailure: 发送广播失败" + errorCode);
            Toast.makeText(MyAdActivity.this, "发送广播失败" + errorCode, Toast.LENGTH_LONG).show();
        }
    };

}
