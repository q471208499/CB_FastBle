package com.clj.blesample.adapter;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.blesample.R;
import com.clj.blesample.comm.ObserverManager;
import com.clj.blesample.data.MyBleDevice;
import com.clj.fastble.utils.ADHelper;
import com.clj.fastble.utils.BytesScanUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.LzBleHelper;

import java.lang.reflect.Field;
import java.util.List;

import cn.cb.baselibrary.utils.ABDateUtils;
import es.dmoral.toasty.MyToast;

public class BleLZAdapter extends RecyclerView.Adapter {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<MyBleDevice> mList;
    private final String uuid_server = "0000FF12-0000-1000-8000-00805F9B34FB";
    private final String uuid_write = "0000FF01-0000-1000-8000-00805F9B34FB";
    private final String uuid_notify = "0000FF02-0000-1000-8000-00805F9B34FB";

    public BleLZAdapter(Context context, List<MyBleDevice> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recycler_ble_lz, parent, false);
        return new BleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyBleDevice device = mList.get(position);
        BleViewHolder bleViewHolder = (BleViewHolder) holder;

        String name = TextUtils.isEmpty(device.getName()) ? "" : device.getName();
        bleViewHolder.name.setText("设备名称：" + name);
        bleViewHolder.id.setText("MAC：" + device.getMac());
        bleViewHolder.rssi.setText("信号强度RSSI：" + device.getRssi());
        bleViewHolder.button.setTag(position);
        bleViewHolder.button.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            final MyBleDevice device = mList.get((Integer) v.getTag());
            BleManager.getInstance().connect(device, new BleGattCallback() {
                private ProgressDialog progressDialog = new ProgressDialog(mContext);
                BluetoothGattService service;

                @Override
                public void onStartConnect() {
                    progressDialog.show();
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    /*img_loading.clearAnimation();
                    img_loading.setVisibility(View.INVISIBLE);
                    btn_scan.setText(getString(R.string.start_scan));*/
                    progressDialog.dismiss();
                    Toast.makeText(mContext, mContext.getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    progressDialog.dismiss();
                    /*mDeviceAdapter.addDevice(bleDevice);
                    mDeviceAdapter.notifyDataSetChanged();*/
                    Button btn = (Button) v;
                    btn.setText("已连接");
                    write(gatt);
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    progressDialog.dismiss();

                    /*mDeviceAdapter.removeDevice(bleDevice);
                    mDeviceAdapter.notifyDataSetChanged();*/

                    if (isActiveDisConnected) {
                        Toast.makeText(mContext, mContext.getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                        ObserverManager.getInstance().notifyObserver(bleDevice);
                    }

                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                }

                private void write(BluetoothGatt gatt) {
                    LzBleHelper.Send helper = new LzBleHelper.Send("10000002");
                    long enterTime = System.currentTimeMillis();

                    BleManager.getInstance().notify(device, uuid_server, uuid_notify, new BleNotifyCallback() {
                        @Override
                        public void onNotifySuccess() {
                            MyToast.show("onNotifySuccess");
                        }

                        @Override
                        public void onNotifyFailure(BleException exception) {
                            MyToast.show("onNotifyFailure" + exception.toString());
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            MyToast.show("onCharacteristicChanged = " + HexUtil.formatHexString(data, true));
                            Log.e(TAG, "onCharacteristicChanged: " + HexUtil.formatHexString(data, true), null);
                        }
                    });

                    while ((System.currentTimeMillis() - enterTime) < 2000) {
                        if(isDeviceBusy(gatt)){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else {
                            break;
                        }
                    }

                    BleManager.getInstance().write(device, uuid_server, uuid_write, helper.signRead().get(0), new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            MyToast.show("success");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            MyToast.show("failure");
                        }
                    });


                    while ((System.currentTimeMillis() - enterTime) < 2000) {
                        if(isDeviceBusy(gatt)){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else {
                            break;
                        }
                    }

                    BleManager.getInstance().write(device, uuid_server, uuid_write, helper.signRead().get(1), new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            MyToast.show("success");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            MyToast.show("failure");
                        }
                    });

                    /*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("写数据");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setCancelable(false);
                    builder.show();*/
                }
            });
        }
    };


    private boolean isDeviceBusy(BluetoothGatt gatt){
        boolean state = false;
        try {
            state = (boolean)readField(gatt,"mDeviceBusy");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return state;
    }

    public  Object readField(Object object, String name) throws IllegalAccessException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }


    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void addItem(BleDevice bleDevice, boolean filter, boolean respond) {
        if (bleDevice.getScanRecord() == null) {
            return;
        }
        if (filter) {
            BytesScanUtils scanUtils = new BytesScanUtils(bleDevice.getScanRecord());
            boolean isOrder02;
            if (!scanUtils.isValid()) {
                return;
            } else {
                Log.i(TAG, "###addItem: " + HexUtil.formatHexString(bleDevice.getScanRecord(), true));
                isOrder02 = scanUtils.getOrder().equals(BytesScanUtils.ORDER_02);
                if (isOrder02 && respond) {
                    ADHelper helper = new ADHelper(mContext);
                    String dateStr = ABDateUtils.getCurDateStr("yyyyMMddHHmmss");
                    String macStr = bleDevice.getMac().replaceAll(":", "");
                    Log.i(TAG, "###addItem: date/" + dateStr + " - macStr/" + macStr);
                    helper.startAction0x11(dateStr, macStr);
                    helper.stopAction();
                }
            }
        }
        for (int i = 0; i < mList.size(); i++) {
            MyBleDevice device = mList.get(i);
            if (device.getMac().equals(bleDevice.getMac())) {
                int times = device.getTimes() + 1;
                MyBleDevice newBle = new MyBleDevice(bleDevice);
                newBle.setTimes(times);
                mList.set(i, newBle);
                notifyItemChanged(i);
                return;
            }
        }
        mList.add(new MyBleDevice(bleDevice));
        notifyItemInserted(mList.size());
    }

    class BleViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView name, id, rssi;
        Button button;

        public BleViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            name = itemView.findViewById(R.id.item_ble_lz_name);
            rssi = itemView.findViewById(R.id.item_ble_lz_rssi);
            id = itemView.findViewById(R.id.item_ble_lz_id);
            button = itemView.findViewById(R.id.item_ble_lz_connect);
        }
    }
}
