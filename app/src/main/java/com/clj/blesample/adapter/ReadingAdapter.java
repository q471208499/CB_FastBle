package com.clj.blesample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clj.blesample.R;
import com.clj.blesample.bean.ReadingBean;

import java.util.List;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder> {

    private Context mContext;
    private List<ReadingBean> mList;

    public ReadingAdapter(Context context, List<ReadingBean> list) {
        mContext = context;
        mList = list;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_reading, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        holder.meterAddress.setText("编号：" + mList.get(position).getMeterAddress());
        holder.flow.setText("用量：" + getValueDefault(mList.get(position).getFlow(), ""));
        int status = mList.get(position).getStatus();
        String statusStr = "";
        int statusColorRes = 0;
        switch (status) {
            case -1:
                statusStr = "超时";
                statusColorRes = Color.RED;
                break;
            case 0:
                statusStr = "等待指令下发";
                statusColorRes = Color.GRAY;
                break;
            case 1:
                statusStr = "等待数据返回";
                statusColorRes = Color.BLACK;
                break;
            case 2:
                statusStr = "成功";
                statusColorRes = Color.GREEN;
                break;
        }
        holder.status.setText("状态：" + statusStr);
        holder.status.setTextColor(statusColorRes);
    }

    private Object getValueDefault(Object o, Object defaultValue) {
        if (o != null) return o;
        return defaultValue;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class ReadingViewHolder extends RecyclerView.ViewHolder {
        TextView meterAddress, flow, status;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            meterAddress = itemView.findViewById(R.id.item_reading_meter_address);
            flow = itemView.findViewById(R.id.item_reading_flow);
            status = itemView.findViewById(R.id.item_reading_status);
        }
    }
}
