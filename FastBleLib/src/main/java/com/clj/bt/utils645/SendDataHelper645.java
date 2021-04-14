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
    /**
     * 数据标识编码：当前总水量
     */
    private final String DATA_CODE_ALL_FLOW = "9010";
    /**
     * 数据标识编码：设置表编号
     */
    private final String DATA_CODE_SET_METER_NUMBER = "C032";
    /**
     * 数据标识编码：关阀
     */
    private final String DATA_CODE_CLOSE_VALVE = "C03A";
    /**
     * 数据标识编码：开阀
     */
    private final String DATA_CODE_OPEN_VALVE = "C03B";

    private String meterAddress;

    public SendDataHelper645(String meterAddress) {
        this.meterAddress = meterAddress;
        fixMeterAddress();
    }

    private void fixMeterAddress() {
        meterAddress = fixMeterAddress(meterAddress);
        meterAddress = HexUtil.bigOrSmallEndian(meterAddress);
    }

    @Override
    public String fixMeterAddress(String meterAddress) {
        return fixMeterAddress(meterAddress, 12);
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
            meterAddress = HexUtil.fixStrAdd0ForLength(meterAddress, 12, false);
        } else if (meterAddress.length() > length) {
            try {
                throw new Exception("fixMeterAddress length is too long.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meterAddress;
    }

    @Override
    public byte[] getData() {
        byte[] bytes = new byte[17];
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
        encryptionAll(DATA_CODE_ALL_FLOW, bytes, 13, false);
        bytes[15] = getCS(bytes);
        bytes[16] = 0x16;
        return bytes;
    }

    /**
     * 将字符串加密并填充到整条数据中
     *
     * @param s     待加密字符串
     * @param bytes 整条数据
     * @param index 加密后的第一个数据在整条数据中的下标
     * @param esc   顺序：正序
     */
    private void encryptionAll(String s, byte[] bytes, int index, boolean esc) {
        byte[] mBytes = new byte[s.length() / 2];
        if (esc) {
            for (int i = 0; i < mBytes.length; i++) {
                bytes[index + i] = encryption(s.substring(i * 2, (i + 1) * 2));
            }
        } else {
            for (int i = 0; i < mBytes.length; i++) {
                bytes[index + i] = encryption(s.substring((mBytes.length - i - 1) * 2, (mBytes.length - i) * 2));
            }
        }
    }

    /**
     * 645协议中，数据项采用+33HEX加密
     *
     * @param s 单个字节16进制
     * @return
     */
    private byte encryption(String s) {
        return (byte) (Integer.parseInt(s, 16) + Integer.parseInt("33", 16));
    }

    /**
     * 68 92 01 00 10 00 00 68 04 0C 65 F3 99 99 BB BB C6 34 33 43 33 33 59 16
     * FE FE FE 68 93 01 00 10 00 00 68 84 00 F8 16
     *
     * @param newAddress 新地址
     * @return
     */
    @Override
    public byte[] getSetAddressData(String newAddress, String keyword) {
        newAddress = fixMeterAddress(newAddress);
        keyword = fixMeterAddress(keyword, 8);
        byte[] bytes = new byte[27];
        bytes[0] = (byte) 0xFE;
        bytes[1] = (byte) 0xFE;
        bytes[2] = (byte) 0xFE;
        bytes[3] = 0x68;
        for (int i = 0; i < (meterAddress.length() / 2); i++) {
            bytes[4 + i] = HexUtil.str2Bcd(meterAddress.substring(i * 2, (i + 1) * 2))[0];
        }
        bytes[10] = 0x68;
        bytes[11] = 0x04;//控制码
        bytes[12] = 0x0C;//长度
        encryptionAll(DATA_CODE_SET_METER_NUMBER, bytes, 13, false);//指令标识码
        encryptionAll(keyword, bytes, 15, true);//密钥
        encryptionAll(newAddress, bytes, 19, false);//新地址
        bytes[25] = getCS(bytes);
        bytes[26] = 0x16;
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
        System.out.println(bytes2HexString(helper.getSetAddressData("10000192", "66668888"), true));
    }
}
