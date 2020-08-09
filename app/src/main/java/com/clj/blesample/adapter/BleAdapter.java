package com.clj.blesample.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.blesample.R;
import com.clj.blesample.data.MyBleDevice;
import com.clj.blesample.uitls.ADHelper;
import com.clj.blesample.uitls.BytesScanUtils;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;

import java.util.List;

import cn.cb.baselibrary.utils.ABDateUtils;
import cn.cb.baselibrary.utils.ABTimeUtils;

public class BleAdapter extends RecyclerView.Adapter {
    private final String TAG = getClass().getSimpleName();

    private Context mContext;
    private List<MyBleDevice> mList;

    public BleAdapter(Context context, List<MyBleDevice> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recycler_ble, parent, false);
        return new BleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyBleDevice device = mList.get(position);
        BleViewHolder bleViewHolder = (BleViewHolder) holder;
        bleViewHolder.view.setTag(position);
        bleViewHolder.view.setOnClickListener(listener);

        BytesScanUtils scan = new BytesScanUtils(device.getScanRecord());
        bleViewHolder.devNum.setText("表编号：" + device.getMac().replaceAll(":", ""));
        bleViewHolder.rssi.setText("信号强度：" + device.getRssi());
        bleViewHolder.times.setText(device.getTimes() + "次");
        bleViewHolder.ysl.setText("用水量：" + scan.getYSL());
        bleViewHolder.v.setText("电池电压：" + scan.getV());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                int index = (int) v.getTag();
                showDialog(index);
            }
        }
    };

    private void showDialog(int index) {
        MyBleDevice device = mList.get(index);
        StringBuilder sb = new StringBuilder();
        sb
        .append("mac  :").append(device.getMac()).append("\n")
        .append("rssi   :").append(device.getRssi()).append("\n")
        .append("time  :").append(ABTimeUtils.getTime(device.getTimestampNanos())).append("\n")
        .append("times:").append(device.getTimes()).append("\n")
        .append("data  :").append(HexUtil.encodeHexStr(device.getScanRecord()));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(sb.toString());
        builder.show();
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
                    helper.startAction0x10(dateStr, macStr);
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
        TextView devNum, rssi, ysl, v, times;

        public BleViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            devNum = itemView.findViewById(R.id.item_dev_num);
            rssi = itemView.findViewById(R.id.item_rssi);
            ysl = itemView.findViewById(R.id.item_ysl);
            v = itemView.findViewById(R.id.item_vv);
            times = itemView.findViewById(R.id.item_times);
        }
    }
}
