package com.clj.blesample.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.blesample.R;

import java.util.ArrayList;
import java.util.List;

public class BTAdapter extends RecyclerView.Adapter<BTAdapter.BTViewHolder> {

    private Context mContext;
    private View.OnClickListener clickListener;
    private List<BluetoothDevice> mList = new ArrayList<>();

    public BTAdapter(Context context, View.OnClickListener clickListener) {
        mContext = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_bt_dev, parent, false);
        return new BTViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BTViewHolder holder, int position) {
        holder.name.setText(mList.get(position).getName());
        holder.name.setTag(mList.get(position));
        holder.name.setOnClickListener(this.clickListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addDev(BluetoothDevice device) {
        if (device.getName() == null || device.getName().isEmpty())
            return;
        for (BluetoothDevice bluetoothDevice : mList) {
            if (bluetoothDevice.getAddress().equals(device.getAddress())) {
                return;
            }
        }
        mList.add(device);
        notifyItemInserted(mList.size());
    }

    class BTViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        public BTViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_bt_name);
        }
    }
}
