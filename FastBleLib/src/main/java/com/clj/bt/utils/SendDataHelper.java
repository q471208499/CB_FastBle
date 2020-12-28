package com.clj.bt.utils;

import com.clj.bt.common.ISendHelper;
import com.clj.fastble.utils.HexUtil;

/**
 * 发送数据 工具助手
 */
public class SendDataHelper implements ISendHelper {
    private String meterAddress;

    public SendDataHelper(String meterAddress) {
        this.meterAddress = meterAddress;
        fixMeterAddress();
    }

    private void fixMeterAddress() {
        meterAddress = fixMeterAddress(meterAddress);
        meterAddress = HexUtil.bigOrSmallEndian(meterAddress);
    }

    @Override
    public String fixMeterAddress(String meterAddress) {
        return fixMeterAddress(meterAddress, 14);
    }

    private String fixMeterAddress(String meterAddress, int length) {
        if (meterAddress == null || meterAddress.isEmpty()) {
            try {
                throw new Exception("fixMeterAddress is null or empty.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        meterAddress = meterAddress.replaceAll(" ", "");
        if (meterAddress.length() < length) {
            meterAddress = HexUtil.fixStrAdd0ForLengthPrefix(meterAddress, 12);
        } else if (meterAddress.length() > length) {
            try {
                throw new Exception("fixMeterAddress length is too long.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meterAddress;
    }

    /**
     * 前面三个字节：FE FE FE 必须填写，188协议必须
     * 完整数据例子：FE FE FE 68 10 AA AA AA AA AA AA AA 01 03 90 1F 01 D2 16
     *
     * @return
     */
    @Override
    public byte[] getData() {
        byte[] bytes = new byte[19];
        bytes[0] = (byte) 0xFE;
        bytes[1] = (byte) 0xFE;
        bytes[2] = (byte) 0xFE;
        bytes[3] = 0x68;
        bytes[4] = 0x10;
        for (int i = 0; i < (meterAddress.length() / 2); i++) {
            bytes[5 + i] = HexUtil.str2Bcd(meterAddress.substring(i * 2, (i + 1) * 2))[0];
        }
        bytes[12] = 0x01;
        bytes[13] = 0x03;
        bytes[14] = (byte) 0x90;
        bytes[15] = 0x1F;
        bytes[16] = 0x01;
        bytes[17] = getCS(bytes);
        bytes[18] = 0x16;
        return bytes;
    }

    @Override
    public byte[] getSetAddressData(String newAddress, String keyword) {
        return getSetAddressData(newAddress);
    }

    public byte[] getSetAddressData(String newAddress){
        newAddress = fixMeterAddress(newAddress);
        newAddress = HexUtil.bigOrSmallEndian(newAddress);
        byte[] bytes = new byte[26];
        bytes[0] = (byte) 0xFE;
        bytes[1] = (byte) 0xFE;
        bytes[2] = (byte) 0xFE;
        bytes[3] = 0x68;
        bytes[4] = (byte) 0xAA;
        for (int i = 0; i < (meterAddress.length() / 2); i++) {
            bytes[5 + i] = HexUtil.str2Bcd(meterAddress.substring(i * 2, (i + 1) * 2))[0];
        }
        bytes[12] = 0x15;
        bytes[13] = 0x0A;
        bytes[14] = (byte) 0xA0;
        bytes[15] = 0x18;
        bytes[16] = 0x00;
        for (int i = 0; i < (newAddress.length() / 2); i++) {
            bytes[17 + i] = HexUtil.str2Bcd(newAddress.substring(i * 2, (i + 1) * 2))[0];
        }
        bytes[24] = getCS(bytes);
        bytes[25] = 0x16;
        return bytes;
    }

    /**
     * 默认FE 不纳入校验计算，所以开始i = 3
     *
     * @param bytes
     * @return
     */
    private byte getCS(byte[] bytes) {
        int toBeSum = 0;
        for (int i = 3; i < bytes.length - 1; i++) {
            toBeSum += bytes[i];
        }
        int sumDec = toBeSum % 256;
        return (byte) sumDec;
    }

    public static String bytes2HexString(byte[] b, boolean space) {
        return bytes2HexString(b, b.length, space);
    }

    public static String bytes2HexString(byte[] b, int length, boolean space) {
        StringBuilder result = new StringBuilder();
        String hex;
        for (int i = 0; i < length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
            if (space)
                result.append(" ");
        }
        return result.toString();
    }

    public static void main(String[] args) {
        SendDataHelper helper = new SendDataHelper("10000192");
        System.out.println(bytes2HexString(helper.getData(), true));
        System.out.println(bytes2HexString(helper.getSetAddressData("10000192"), true));
        //68 10 10 00 01 91 00 00 00 03 03 0A 81 00 49 16
        //System.out.println(hexCS("68 10 01 00 00 05 08 00 00 01 03 90 1F 01"));
    }
}
