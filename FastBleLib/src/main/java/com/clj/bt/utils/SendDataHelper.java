package com.clj.bt.utils;

import com.clj.fastble.utils.HexUtil;

/**
 * 发送数据 工具助手
 */
public class SendDataHelper {
    private String meterAddress;

    public SendDataHelper(String meterAddress) {
        this.meterAddress = meterAddress;
        fixMeterAddress();
    }

    private void fixMeterAddress() {
        if (meterAddress == null || meterAddress.isEmpty()) {
            new Throwable("meterAddress is null or empty");
        }
        meterAddress = meterAddress.replaceAll(" ", "");
        meterAddress = HexUtil.bigOrSmallEndian(meterAddress);
        if (meterAddress.length() < 14) {
            meterAddress = HexUtil.fixStrAdd0ForLength(meterAddress, 14);
        } else if (meterAddress.length() > 14) {
            // do something
        }
    }

    //68 10 AA AA AA AA AA AA AA 01 03 90 1F 01 D2 16
    public byte[] getData() {
        byte[] bytes = new byte[16];
        bytes[0] = 0x68;
        bytes[1] = 0x10;
        for (int i = 0; i < (meterAddress.length() / 2); i++) {
            bytes[2 + i] = HexUtil.str2Bcd(meterAddress.substring(i * 2, (i + 1) * 2))[0];
        }
        bytes[9] = 0x01;
        bytes[10] = 0x03;
        bytes[11] = (byte) 0x90;
        bytes[12] = 0x1F;
        bytes[13] = 0x01;
        bytes[14] = getCS(bytes);
        bytes[15] = 0x16;
        return bytes;
    }

    //68 10 AA AA AA AA AA AA AA 01 03 90 1F 01 D2 16
    public static byte[] testMeterAddress() {
        byte[] bytes = new byte[16];
        bytes[0] = 0x68;
        bytes[1] = (byte) 0x10;
        bytes[2] = (byte) 0xAA;
        bytes[3] = (byte) 0xAA;
        bytes[4] = (byte) 0xAA;
        bytes[5] = (byte) 0xAA;
        bytes[6] = (byte) 0xAA;
        bytes[7] = (byte) 0xAA;
        bytes[8] = (byte) 0xAA;
        bytes[9] = (byte) 0x01;
        bytes[10] = (byte) 0x03;
        bytes[11] = (byte) 0x90;
        bytes[12] = (byte) 0x1F;
        bytes[13] = (byte) 0x01;
        bytes[14] = (byte) 0xD2;
        bytes[15] = (byte) 0x16;
        return bytes;
    }

    private byte getCS(byte[] bytes) {
        int toBeSum = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            toBeSum += bytes[i];
        }
        int sumDec = toBeSum % 256;
        return (byte) sumDec;
    }

    private static String hexCS(String hex) {
        int calcValueCount = 0;
        hex = hex.replaceAll(" ", "");
        for (int i = 0; i < hex.length() / 2; i++) {
            calcValueCount += Integer.valueOf(hex.substring(i * 2, (i + 1) * 2), 16);
        }
        return Integer.toHexString(calcValueCount % 256);
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
        /*SendDataHelper helper = new SendDataHelper("01000005080000");
        System.out.println(bytes2HexString(helper.getData(), false));*/
        //68 10 10 00 01 91 00 00 00 03 03 0A 81 00 49 16
        //System.out.println(hexCS("68 10 01 00 00 05 08 00 00 01 03 90 1F 01"));
    }
}
