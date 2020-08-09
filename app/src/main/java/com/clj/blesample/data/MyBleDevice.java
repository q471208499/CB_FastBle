package com.clj.blesample.data;

import com.clj.fastble.data.BleDevice;

public class MyBleDevice extends BleDevice {
    private int times;

    public MyBleDevice(BleDevice device) {
        super(device.getDevice(), device.getRssi(), device.getScanRecord(), device.getTimestampNanos());
        this.times = 1;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
