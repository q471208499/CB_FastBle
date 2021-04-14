package com.clj.blesample.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.clj.blesample.R;
import com.clj.blesample.listener.AdBtnCallback;
import com.clj.fastble.project.hk.BytesADUtils;

public class AdDialog extends AlertDialog.Builder {
    private RadioGroup radioGroup;
    private AdBtnCallback adBtnCallback;
    private Context context;
    private ViewInterface anInterface;
    private String macAddress;

    public AdDialog(@NonNull Context context, RadioGroup radioGroup, AdBtnCallback adBtnCallback, String macAddress) {
        super(context);
        this.context = context;
        this.radioGroup = radioGroup;
        this.adBtnCallback = adBtnCallback;
        this.macAddress = macAddress;
        setView();
    }

    private void setView() {
        setView(getView());
        setPositiveButton("确定", clickListener);
        setNegativeButton("取消", clickListener);
    }

    private DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                adBtnCallback.positive(anInterface.getHexDate(), anInterface.broadcastData(), anInterface.order());
            } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                adBtnCallback.negative();
            }
        }
    };

    private View getView() {
        int radioBtnId = radioGroup.getCheckedRadioButtonId();
        if (radioBtnId == R.id.radio_0x11) {
            anInterface = new Hex11();
            setTitle("0x11 响应广播包，设置时间");
        } else if (radioBtnId == R.id.radio_0x12) {
            anInterface = new Hex12();
            setTitle("0x12 设置蓝牙表的参数");
        } else if (radioBtnId == R.id.radio_0x13) {
            anInterface = new Hex13();
            setTitle("0x13 设置蓝牙地址和初始数据");
        } else if (radioBtnId == R.id.radio_0x14) {
            anInterface = new Hex14();
            setTitle("0x14 读取蓝牙表工作参数");
        } else if (radioBtnId == R.id.radio_0x15) {
            anInterface = new Hex15();
            setTitle("0x15 读取蓝牙表序列号和版本号");
        }
        return anInterface.getView();
    }

    private String bigEndian(String str) {
        if (str == null || str.isEmpty() || str.length() % 2 == 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() / 2; i > 0; i--) {
            sb.append(str.substring(i * 2 - 2, i * 2));
        }
        return sb.toString();
    }

    private class Hex11 implements ViewInterface {
        EditText meterAddress, timeET;

        @Override
        public View getView() {
            View view = LayoutInflater.from(context).inflate(R.layout.view_0x11, null);
            meterAddress = view.findViewById(R.id.meter_address_value);
            timeET = view.findViewById(R.id.view_0x11_time_value);
            meterAddress.setText(macAddress);
            return view;
        }

        @Override
        public String getHexDate() {
            StringBuilder sb = new StringBuilder();
            sb.append(bigEndian(meterAddress.getText().toString()));
            sb.append(bigEndian(timeET.getText().toString()));
            return sb.toString();
        }

        @Override
        public byte[] broadcastData() {
            return new BytesADUtils(getHexDate(), "").get0x11Bytes();
        }

        @Override
        public String order() {
            return "11";
        }
    }

    private class Hex12 implements ViewInterface {
        EditText noReplyInterval, ReplyCnt, SendPointNum, StartAdvTime1, StoptAdvTime1, StartAdvTime2,
                StoptAdvTime2, AdvTimes, AdvGapTimer, ReadIntInterval, ReplyTimeout, SampleMode,
                IrScanGap, IrBaud, meterAddress;

        @Override
        public View getView() {
            View view = LayoutInflater.from(context).inflate(R.layout.view_0x12, null);
            noReplyInterval = view.findViewById(R.id.noReplyInterval_value);
            ReplyCnt = view.findViewById(R.id.ReplyCnt_value);
            SendPointNum = view.findViewById(R.id.SendPointNum_value);
            StartAdvTime1 = view.findViewById(R.id.StartAdvTime1_value);
            StoptAdvTime1 = view.findViewById(R.id.StoptAdvTime1_value);
            StartAdvTime2 = view.findViewById(R.id.StartAdvTime2_value);
            StoptAdvTime2 = view.findViewById(R.id.StoptAdvTime2_value);
            AdvTimes = view.findViewById(R.id.AdvTimes_value);
            AdvGapTimer = view.findViewById(R.id.AdvGapTimer_value);
            ReadIntInterval = view.findViewById(R.id.ReadIntInterval_value);
            ReplyTimeout = view.findViewById(R.id.ReplyTimeout_value);
            SampleMode = view.findViewById(R.id.SampleMode_value);
            IrScanGap = view.findViewById(R.id.IrScanGap_value);
            IrBaud = view.findViewById(R.id.IrBaud_value);
            meterAddress = view.findViewById(R.id.meter_address_value);
            meterAddress.setText(macAddress);
            return view;
        }

        @Override
        public String getHexDate() {
            StringBuilder sb = new StringBuilder();
            sb.append(bigEndian(meterAddress.getText().toString()));
            sb.append(String.format("%02x", Integer.parseInt(noReplyInterval.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(ReplyCnt.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(SendPointNum.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(StartAdvTime1.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(StoptAdvTime1.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(StoptAdvTime2.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(StartAdvTime2.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(AdvTimes.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(AdvGapTimer.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(ReadIntInterval.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(ReplyTimeout.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(SampleMode.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(IrScanGap.getText().toString())));
            sb.append(String.format("%02x", Integer.parseInt(IrBaud.getText().toString())));
            return sb.toString();
        }

        @Override
        public byte[] broadcastData() {
            return new BytesADUtils(getHexDate(), "").get0x12Bytes();
        }

        @Override
        public String order() {
            return "12";
        }
    }

    private class Hex13 implements ViewInterface {
        EditText meterAddress, newMeterAddress, meterCountNumber;

        @Override
        public View getView() {
            View view = LayoutInflater.from(context).inflate(R.layout.view_0x13, null);
            meterAddress = view.findViewById(R.id.meter_address_value);
            newMeterAddress = view.findViewById(R.id.view_0x13_new_meter_address_value);
            meterCountNumber = view.findViewById(R.id.view_0x13_meter_count_number_value);
            meterAddress.setText(macAddress);
            return view;
        }

        @Override
        public String getHexDate() {
            StringBuilder sb = new StringBuilder();
            sb.append(bigEndian(meterAddress.getText().toString()));
            sb.append("69");
            sb.append(bigEndian(newMeterAddress.getText().toString()));
            sb.append("69");
            sb.append(bigEndian(meterCountNumber.getText().toString().split("\\.")[0]));
            sb.append(bigEndian(meterCountNumber.getText().toString().split("\\.")[1]));
            return sb.toString();
        }

        @Override
        public byte[] broadcastData() {
            return new BytesADUtils(getHexDate(), "").get0x13Bytes();
        }

        @Override
        public String order() {
            return "13";
        }
    }

    private class Hex14 implements ViewInterface {
        EditText meterAddress;

        @Override
        public View getView() {
            View view = LayoutInflater.from(context).inflate(R.layout.view_0x14, null);
            meterAddress = view.findViewById(R.id.meter_address_value);
            meterAddress.setText(macAddress);
            return view;
        }

        @Override
        public String getHexDate() {
            StringBuilder sb = new StringBuilder();
            sb.append(bigEndian(meterAddress.getText().toString()));
            return sb.toString();
        }

        @Override
        public byte[] broadcastData() {
            return new BytesADUtils(getHexDate(), "").get0x14Bytes();
        }

        @Override
        public String order() {
            return "14";
        }
    }

    private class Hex15 implements ViewInterface {
        EditText meterAddress;

        @Override
        public View getView() {
            View view = LayoutInflater.from(context).inflate(R.layout.view_0x15, null);
            meterAddress = view.findViewById(R.id.meter_address_value);
            meterAddress.setText(macAddress);
            return view;
        }

        @Override
        public String getHexDate() {
            StringBuilder sb = new StringBuilder();
            sb.append(bigEndian(meterAddress.getText().toString()));
            return sb.toString();
        }

        @Override
        public byte[] broadcastData() {
            return new BytesADUtils(getHexDate(), "").get0x15Bytes();
        }

        @Override
        public String order() {
            return "15";
        }
    }

    interface ViewInterface {
        View getView();

        String getHexDate();

        byte[] broadcastData();

        String order();
    }
}
