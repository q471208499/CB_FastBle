package com.clj.blesample.activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.blesample.R;
import com.clj.blesample.adapter.BTAdapter;
import com.clj.blesample.adapter.ReadingAdapter;
import com.clj.blesample.bean.ReadingBean;
import com.clj.bt.activity.BTBaseActivity;
import com.clj.bt.utils.ReceiveDataHelper;
import com.clj.bt.utils.SendDataHelper;

import java.util.ArrayList;
import java.util.List;

import cn.cb.baselibrary.utils.ViewUtils;
import cn.cb.baselibrary.widget.MyDividerItemDecoration;
import es.dmoral.toasty.MyToast;

public class BTActivity extends BTBaseActivity {
    private final String TAG = getClass().getSimpleName();

    private BTAdapter adapter;
    private RecyclerView readingRecyclerView;
    private ReadingAdapter readingAdapter;
    private List<ReadingBean> readingList = new ArrayList<>();
    private Button readingBtn;

    private final int READING_BTN_WHAT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_t);
        initBarView();
        bindView();
    }

    @Override
    protected void callbackConnectStatus(boolean connect) {
        if (connect) {
            handler.sendEmptyMessage(READING_BTN_WHAT);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == READING_BTN_WHAT) {
                readingBtn.setVisibility(View.VISIBLE);
            }
        }
    };

    private void bindView() {
        adapter = new BTAdapter(this, clickListener);
        readingAdapter = new ReadingAdapter(this, readingList);
        readingBtn = findViewById(R.id.bt_reading);
        readingBtn.setOnClickListener(clickListener);
        View readingView = findViewById(R.id.bt_reading_vew);
        findViewById(R.id.bt_search).setOnClickListener(clickListener);
        RecyclerView devRecyclerView = findViewById(R.id.bt_dev_recycler);
        devRecyclerView.addItemDecoration(new MyDividerItemDecoration());
        devRecyclerView.setAdapter(adapter);
        readingRecyclerView = findViewById(R.id.reading_recycler);
        readingRecyclerView.addItemDecoration(new MyDividerItemDecoration());
        readingRecyclerView.setAdapter(readingAdapter);
        if (curConnState) {
            readingBtn.setVisibility(View.VISIBLE);
        }

        ViewUtils.setOutline(devRecyclerView);
        ViewUtils.setOutline(readingView);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bt_search) {
                searchBtDevice();
            } else if (v.getId() == R.id.item_bt_name) {
                if (curConnState) {
                    MyToast.show("已连接：" + curBluetoothDevice.getName());
                    return;
                }
                final BluetoothDevice device = (BluetoothDevice) v.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(BTActivity.this);
                builder.setTitle("蓝牙配对");
                builder.setMessage(device.getName() + "蓝牙配对？");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("配对", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setCurBTDevice(device);
                        connectBT();
                        showLoading("正在配对...");
                    }
                });
                builder.show();
            } else if (v.getId() == R.id.bt_reading) {
                setList();
                send();
            }
        }
    };

    private void setList() {
        readingList.clear();
        readingAdapter.notifyDataSetChanged();
        readingList.add(new ReadingBean("10000206", ReadingBean.STATUS_STANDBY, 0));
        readingList.add(new ReadingBean("10000209", ReadingBean.STATUS_STANDBY, 0));
        readingList.add(new ReadingBean("10000201", ReadingBean.STATUS_STANDBY, 0));
        readingList.add(new ReadingBean("10000202", ReadingBean.STATUS_STANDBY, 0));
        readingList.add(new ReadingBean("10000199", ReadingBean.STATUS_STANDBY, 0));
        readingAdapter.notifyDataSetChanged();
    }

    private void send() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                toSend(bytes2HexString(new SendDataHelper("000000").getData(), false));//首次下发命令，需要发送一条无效指令，先激活设备
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < readingList.size(); i++) {

                    toSend(bytes2HexString(new SendDataHelper(readingList.get(i).getMeterAddress()).getData(), false));
                    notifyItemChanged(i);
                    //handler.handleMessage(handler.obtainMessage(ITEM_REFRESH_WHAT, i, 0));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        //showLoading(list.size(), "抄表中");
    }

    @Override
    protected void addDev(BluetoothDevice bluetoothDevice) {
        adapter.addDev(bluetoothDevice);
    }

    private StringBuilder completeData = new StringBuilder();

    @Override
    protected void receive(String hexStr) {
        hexStr = hexStr.replace(" ", "");//去掉空格
        if (completeData.length() == 0) {
            if (hexStr.contains("68")) {
                completeData.append(hexStr);
            }
        } else {
            completeData.append(hexStr);
            if (hexStr.endsWith("16")) {
                ReceiveDataHelper helper = new ReceiveDataHelper(completeData.toString());
                if (helper.isValidForCJ188()) {
                    Log.i(TAG, "receive 完整帧数据: " + completeData.toString());
                    Log.i(TAG, "receive: 编号：" + helper.getMeterAddress() + " 用水量：" + helper.getYSL());
                    notifyItemChanged(helper);
                    completeData = new StringBuilder();
                }
            }
        }
    }

    private void notifyItemChanged(ReceiveDataHelper helper) {
        for (int i = 0; i < readingList.size(); i++) {
            if (helper.getMeterAddress().contains(readingList.get(i).getMeterAddress())) {
                readingList.get(i).setFlow(helper.getYSL());
                readingList.get(i).setStatus(ReadingBean.STATUS_SUCCESS);
                readingAdapter.notifyItemChanged(i);
            }
        }
    }

    private void notifyItemChanged(final int index) {
        BTActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                readingList.get(index).setStatus(ReadingBean.STATUS_RECEIVE);
                readingAdapter.notifyItemChanged(index);
                readingRecyclerView.scrollToPosition(index);
            }
        });
    }
}