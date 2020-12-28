package com.clj.bt.common;

public interface ISendHelper {
    /**
     * 抄表报文
     */
    byte[] getData();

    /**
     * 设置表号报文
     * @param newAddress 新地址
     */
    byte[] getSetAddressData(String newAddress, String keyword);

    String fixMeterAddress(String meterAddress);
}
