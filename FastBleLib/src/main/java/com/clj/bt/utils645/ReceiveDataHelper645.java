package com.clj.bt.utils645;

import com.clj.bt.common.IReceiveDataHelper;

/**
 * 645 协议 接收报文助手
 * FE FE FE 68 92 01 00 10 00 00 68 81 06 43 C3 33 BB 33 33 54 16
 * 接收数据域 需要解密 减去33
 * 2020年12月16日 chenb
 */
public class ReceiveDataHelper645 implements IReceiveDataHelper {
    private final String DATA_CJ645_HEAD = "68";
    /** 数据头 */
    private final String DATA_CJ645_HEAD_DATA = "6881";
    private final String DATA_CJ645_END = "16";

    private String hexStr;

    public ReceiveDataHelper645(String hexStr) {
        this.hexStr = hexStr.replaceAll(" ", "");
    }

    /**
     * 倒数第二个字节为校验和
     *
     * @return
     */
    @Override
    public boolean isValidForCommon() {
        if (validBasis()) return false;
        return isValidForCommon(hexStr);
    }

    private boolean isValidForCommon(String hexStr) {
        int calcValueCount = 0;
        for (int i = 0; i < hexStr.length() / 2 - 2; i++) {
            calcValueCount += Integer.valueOf(hexStr.substring(i * 2, (i + 1) * 2), 16);
        }
        int calcValue = calcValueCount % 256;
        //System.out.println("calcValueCount: " + calcValue);
        int compareValue = Integer.valueOf(hexStr.substring(hexStr.length() - 4, hexStr.length() - 2), 16);
        //System.out.println("compareValue: " + compareValue);
        return calcValue == compareValue;
    }

    /**
     * 基础信息校验
     *
     * @return true 无效； false 有效
     */
    private boolean validBasis() {
        return hexStr == null || hexStr.isEmpty() || hexStr.length() % 2 == 1;
    }

    @Override
    public String getMeterAddress() {
        String myHexStr = hexStr.substring(hexStr.indexOf(DATA_CJ645_HEAD));
        StringBuilder sb = new StringBuilder();
        sb.append(myHexStr.substring(12, 14))
                .append(myHexStr.substring(10, 12))
                .append(myHexStr.substring(8, 10))
                .append(myHexStr.substring(6, 8))
                .append(myHexStr.substring(4, 6))
                .append(myHexStr.substring(2, 4));
        return sb.toString();
    }

    /**
     * 累计水量
     *
     * @return
     */
    @Override
    public String getYSL() {
        String myHexStr = hexStr.substring(hexStr.indexOf(DATA_CJ645_HEAD_DATA));
        StringBuilder sb = new StringBuilder();
        sb.append(decrypt645(myHexStr.substring(16, 18)))
                .append(decrypt645(myHexStr.substring(14, 16)))
                .append(decrypt645(myHexStr.substring(12, 14)))
                .append(".")
                .append(decrypt645(myHexStr.substring(10, 12)));
        return String.valueOf(Double.parseDouble(sb.toString()));//去掉前面可能存在0
    }


    private String decrypt645(String hexStr) {
        int srcI = Integer.parseInt(hexStr, 16);
        int keyI = Integer.parseInt("33", 16);
        //return Integer.toHexString(srcI - keyI);
        return String.format("%02x", srcI - keyI);
    }

    @Override
    public boolean isValid() {
        if (hexStr.contains(DATA_CJ645_HEAD) && hexStr.lastIndexOf(DATA_CJ645_END) > -1) {
            String myHexStr = hexStr.substring(hexStr.indexOf(DATA_CJ645_HEAD));
            if (validBasis()) return false;
            return isValidForCommon(myHexStr);
        }
        return false;
    }

    public static void main(String[] args) {
        ReceiveDataHelper645 helper645 = new ReceiveDataHelper645("6802020010000068810643C3333534334016");
        System.out.println(helper645.getMeterAddress());
        System.out.println(helper645.isValid());
        System.out.println(helper645.validBasis());
        System.out.println(helper645.getYSL());

        System.out.println(String.format("%02x", 10));
        /*int i = Integer.parseInt("3333bb33", 16);
        System.out.println(i);
        int a = i - Integer.parseInt("33333333", 16);
        System.out.println(a);
        System.out.println(Integer.toHexString(34816));*/
    }
}
