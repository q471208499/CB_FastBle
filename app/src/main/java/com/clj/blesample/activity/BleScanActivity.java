package com.clj.blesample.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.blesample.R;
import com.clj.blesample.adapter.BleLZAdapter;
import com.clj.blesample.data.MyBleDevice;
import com.clj.fastble.BleManager;
import com.clj.fastble.activity.BleBaseActivity;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

import cn.cb.baselibrary.widget.MyDividerItemDecoration;
import es.dmoral.toasty.MyToast;

public class BleScanActivity extends BleBaseActivity {

    private final String TAG = getClass().getSimpleName();
    private BleLZAdapter adapter;
    private ImageView imgLoading;
    private Switch filterSwitch, respondSwitch;
    private Button button;
    private TextView total;
    private Animation operatingAnim;
    private List<MyBleDevice> list = new ArrayList<>();
    private final int REQUEST_ENABLE_BT = 2009;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        bindView();
    }

    private void bindView() {
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        adapter = new BleLZAdapter(this, list);
        imgLoading = findViewById(R.id.img_loading);
        total = findViewById(R.id.text_total);
        filterSwitch = findViewById(R.id.ble_switch_filter);
        respondSwitch = findViewById(R.id.ble_switch_respond);
        button = findViewById(R.id.ble_scan_btn);
        button.setOnClickListener(clickListener);
        RecyclerView recyclerView = findViewById(R.id.ble_recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MyDividerItemDecoration());
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ble_scan_btn) {
                clickBtn();
            }
        }
    };

    private void clickBtn() {
        String btnText = button.getText().toString();
        if (getString(R.string.start_scan).equals(btnText)) {
            imgLoading.startAnimation(operatingAnim);
            imgLoading.setVisibility(View.VISIBLE);
            button.setText(R.string.stop_scan);
            list.clear();
            adapter.notifyDataSetChanged();
            //startScan();
            tmp = System.currentTimeMillis();
            times = 0;
            newClick();
        } else {
            imgLoading.clearAnimation();
            imgLoading.setVisibility(View.INVISIBLE);
            button.setText(R.string.start_scan);
            //BleManager.getInstance().cancelScan();
            leScanner.stopScan(callback);
        }
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
                Log.i(TAG, "###onLeScan: " + bleDevice.getMac());
                adapter.addItem(bleDevice, filterSwitch.isChecked(), respondSwitch.isChecked());
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.i(TAG, "###onScanning: " + bleDevice.getMac());
                adapter.addItem(bleDevice, filterSwitch.isChecked(), respondSwitch.isChecked());
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
            }
        });
    }

    //******************************************************************************************
    private BluetoothLeScanner leScanner;
    private BluetoothAdapter bluetoothAdapter;

    private void newClick() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (enableBluetooth(bluetoothAdapter)) {
            startScanBLE();
        }
        //register();
    }

    /*private void register() {
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(searchDevices, intent);
    }*/


    protected void startScanBLE() {
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();
        leScanner.startScan(null, settings, callback);
    }

    /**
     * 打开蓝牙
     */
    public boolean enableBluetooth(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScanBLE();
            } else {
                MyToast.errorL("开启蓝牙失败！");
            }
        }
    }

    private int times = 0;
    private long tmp = 0;
    private ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.i(TAG, "onScanResult: " + ++times);
            long sp = (System.currentTimeMillis() - tmp) / 1000;
            total.setText("发现" + ++times + "次ad，耗时：" + sp + "s");
            BluetoothDevice device = result.getDevice();
            BleDevice bleDevice = new BleDevice(device, result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
            adapter.addItem(bleDevice, filterSwitch.isChecked(), respondSwitch.isChecked());
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
