package com.clj.fastble.utils;

import cn.cb.baselibrary.utils.ABDateUtils;

/**
 * 数据格式：0x73,0x6A,0x0D,0X10,0x66,0x55,0x44,0x33,0x22,0x11,0x00,CS,0x16
 * 包头,包头,整个返回数据长度,指令,数据,数据,....,CS校验,包尾 CS校验 = (0x73+0x6A+....+0x36+0x00) % 256;
 */
public class BytesADUtils {
    // private byte[] begin = {0x73, 0x6A};
    private final byte end = 0x16;
    //private final int beginSum = 115 + 106;
    //private final int order0x10Int = 16;
    //private final int order0x11Int = 17;
    //private final int order0x12Int = 18;
    //private final int order0x21Int = 33;
    //private final int order0x22Int = 34;
    private final byte order0x01 = 0x01;
    private final byte order0x02 = 0x02;
    private final byte order0x10 = 0x10;
    private final byte order0x11 = 0x11;
    private final byte order0x12 = 0x12;
    private final byte order0x21 = 0x21;
    private final byte order0x22 = 0x22;

    private String str, mac;

    public BytesADUtils(String str) {
        this.str = str;
        this.mac = "665544332211";
    }

    public BytesADUtils(String str, String mac) {
        this.str = str;
        this.mac = mac;
    }

    public byte[] get0x01Bytes() {
        this.mac = "";
        this.str = ABDateUtils.getCurDateStr("yyyyMMddHHmms");
        return getBytes(order0x01);
    }

    public byte[] get0x02Bytes() {
        this.mac = "";
        this.str = ABDateUtils.getCurDateStr("yyyyMMddHHmms");
        return getBytes(order0x02);
    }

    public byte[] get0x10Bytes() {
        return getBytes(order0x10);
    }

    public byte[] get0x11Bytes() {
        return getBytes(order0x11);
    }

    public byte[] get0x12Bytes() {
        return getBytes(order0x12);
    }

    public byte[] get0x21Bytes() {
        return getBytes(order0x21);
    }

    public byte[] get0x22Bytes() {
        return getBytes(order0x22);
    }

    private byte[] getBytes(byte order) {
        //byte[] data = str2Bcd(str);
        byte[] data = HexUtil.r(str);
        byte[] result = new byte[data.length + 6 + 6];//6代表头尾等信息，6代表表地址
        result[0] = 0x73;
        result[1] = 0x6A;
        result[2] = (byte) (data.length + 6 + 6);
        result[3] = order;
        byte[] macB = HexUtil.str2Bcd(mac);
        for (int i = 0; i < macB.length; i++) {
            result[i + 4] = macB[i];
        }
        for (int i = 0; i < data.length; i++) {
            result[i + 10] = data[i];
        }
        result[result.length - 2] = getCS2(result);
        result[result.length - 1] = end;
        return result;
    }

    private byte getCS2(byte[] result) {
        int toBeSum = 0;
        for (int i = 0; i < result.length - 2; i++) {
            toBeSum += result[i];
        }
        int sumDec = toBeSum % 256;
        return (byte) sumDec;
    }

    public static void main(String[] args) {
        String s = "202005261130";
        BytesADUtils utils = new BytesADUtils(s);

        byte[] a = utils.get0x10Bytes();
        System.out.println(HexUtil.encodeHexStr(a));
    }

}
