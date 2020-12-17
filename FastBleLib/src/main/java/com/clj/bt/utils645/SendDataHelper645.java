package com.clj.bt.utils645;

import com.clj.bt.common.ISendHelper;
import com.clj.fastble.utils.HexUtil;

/**
 * 645 协议 发送报文助手
 * 广播方式：FE FE FE 68 99 99 99 99 99 99 68 01 02 43 C3 6F 16
 * 指定地址：FE FE FE 68 92 01 00 10 00 00 68 01 02 43 C3 7C 16
 * 发送数据域 43 C3 加密添加33后结果
 * 接收数据域 需要解密 减去33
 * 2020年12月16日 chenb
 */
public class SendDataHelper645 implements ISendHelper {
    private String meterAddress;

    public SendDataHelper645(String meterAddress) {
        this.meterAddress = meterAddress;
        fixMeterAddress();
    }

    private void fixMeterAddress() {
        if (meterAddress == null || meterAddress.isEmpty()) {
            new Throwable("meterAddress is null or empty");
        }
        meterAddress = meterAddress.replaceAll(" ", "");
        meterAddress = HexUtil.bigOrSmallEndian(meterAddress);
        if (meterAddress.length() < 12) {
            meterAddress = HexUtil.fixStrAdd0ForLength(meterAddress, 12);
        } else if (meterAddress.length() > 12) {
            // do something
        }
    }

    @Override
    public byte[] getData() {
        byte[] bytes = new byte[19];
        bytes[0] = (byte) 0xFE;
        bytes[1] = (byte) 0xFE;
        bytes[2] = (byte) 0xFE;
        bytes[3] = 0x68;
        for (int i = 0; i < (meterAddress.length() / 2); i++) {
            bytes[4 + i] = HexUtil.str2Bcd(meterAddress.substring(i * 2, (i + 1) * 2))[0];
        }
        bytes[10] = 0x68;
        bytes[11] = 0x01;
        bytes[12] = 0x02;
        bytes[13] = 0x43;//10 加密33后 43
        bytes[14] = (byte) 0xC3;//90 加密33后 C3
        bytes[15] = getCS(bytes);
        bytes[16] = 0x16;
        return bytes;
    }

    /**
     * 默认FE 不纳入校验计算，所以开始i = 3
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

    public static String bytes2HexString(byte[] b, boolean space) {
        return bytes2HexString(b, b.length, space);
    }

    public static void main(String[] args) {
        SendDataHelper645 helper = new SendDataHelper645("10000192");
        System.out.println(bytes2HexString(helper.getData(), true));
    }
}
