package com.clj.fastble.project.hk;

import com.clj.fastble.utils.HexUtil;

import cn.cb.baselibrary.utils.ABDateUtils;

/**
 * to: 华科/捷先 数字秒通表
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
    private final byte order0x13 = 0x13;
    private final byte order0x14 = 0x14;
    private final byte order0x15 = 0x15;
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

    public byte[] get0x13Bytes() {
        return getBytes(order0x13);
    }

    public byte[] get0x14Bytes() {
        return getBytes(order0x14);
    }

    public byte[] get0x15Bytes() {
        return getBytes(order0x15);
    }

    public byte[] get0x21Bytes() {
        return getBytes(order0x21);
    }

    public byte[] get0x22Bytes() {
        return getBytes(order0x22);
    }

    private byte[] getBytes(byte order) {
        //byte[] data = str2Bcd(str);
        byte[] data = HexUtil.str2Bcd(mac + str);//HexUtil.r(mac + str);
        byte[] result = new byte[data.length + 6];//6代表头尾等信息
        result[0] = 0x73;
        result[1] = 0x6A;
        result[2] = (byte) (data.length + 6);
        result[3] = order;
        //if (mac != null || !mac.isEmpty())
        /*byte[] macB = HexUtil.str2Bcd(mac);
        for (int i = 0; i < macB.length; i++) {
            result[i + 4] = macB[i];
        }*/
        for (int i = 0; i < data.length; i++) {
            result[i + 4] = data[i];
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

    private String getMeterAddress(String meterAddress) {
        String str = meterAddress;
        if (str == null || str.isEmpty() || str.length() % 2 == 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() / 2; i > 0; i--) {
            sb.append(str.substring(i * 2 - 2, i * 2));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        BytesADUtils adUtils = new BytesADUtils("665544332211b405140609151201640502006401", "");
        byte[] hex12 = adUtils.get0x12Bytes();
        System.out.println(HexUtil.encodeHexStr(hex12));

        //System.out.println(adUtils.getMeterAddress("112233445566"));
        /*String s = "202005261130";
        BytesADUtils utils = new BytesADUtils(s);

        byte[] a = utils.get0x10Bytes();
        System.out.println(HexUtil.encodeHexStr(a));*/
    }

}
