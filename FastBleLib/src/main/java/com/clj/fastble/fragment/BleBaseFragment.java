package com.clj.fastble.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.R;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.ArrayList;
import java.util.List;

import cn.cb.baselibrary.fragment.BaseFragment;
import es.dmoral.toasty.Toasty;

/**
 * copy from BleBaseActivity.java
 */
public class BleBaseFragment extends BaseFragment {

    private final String TAG = getClass().getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 2001;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2002;
    private final int REQUEST_ENABLE_BT = 2009;
    protected int SCAN_TIME_OUT = 10000;//每次扫描时间

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checkPermissions();
        BleManager.getInstance().init(getActivity().getApplication());
        BleManager.getInstance()
                .enableLog(false)
                //.setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
    }

    /**
     * ************************************************
     * 打开蓝牙扫描逻辑
     * 打开蓝牙 → 要权限 → 扫描
     * 打开蓝牙 → 有权限 → 扫描
     * 蓝牙已开 → 要权限 → 扫描
     * 蓝牙已开 → 有权限 → 扫描
     * ************************************************
     */
    protected void goScan() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            enableBluetooth();
        } else if (!checkGPS()) {
            openGPS();
        } else {
            if (enableBluetooth())
                checkPermissions();
        }
    }

    private void openGPS() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.notifyTitle)
                .setMessage(R.string.gpsNotifyMsg)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toasty.info(getContext(), getString(R.string.no_permission));
                            }
                        })
                .setPositiveButton(R.string.setting,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                            }
                        })

                .setCancelable(false)
                .show();
    }

    private boolean checkGPS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkGPSIsOpen();
        }
        return true;
    }

    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothLeScanner leScanner;

    /**
     * 打开蓝牙
     */
    public boolean enableBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onPermissionGranted(permissions[i]);
                        } else {
                            Toasty.info(getContext(), getString(R.string.no_permission));
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                setScanRule();
                startScanBLE();
                break;
        }
    }

    protected void startScanBLE() {
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();
        leScanner.startScan(null, settings, callback);
        Toasty.info(getContext(), getString(R.string.start_scan));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (leScanner != null) {
                    leScanner.stopScan(callback);
                }
                Toasty.info(getContext(), getString(R.string.stop_scan));
            }
        }, SCAN_TIME_OUT);
    }

    protected ScanCallback callback;

    private void setScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                //.setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                //.setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                //.setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                //.setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                //.setScanTimeOut(SCAN_TIME_OUT)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return true;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(getActivity(), deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (!checkGPS()) {
                    openGPS();
                } else {
                    checkPermissions();
                }
            } else {
                Toasty.info(getContext(), getString(R.string.bluetooth_failed_to_open));
            }
        } else if (requestCode == REQUEST_CODE_OPEN_GPS) {
            checkPermissions();
        }
    }
}
