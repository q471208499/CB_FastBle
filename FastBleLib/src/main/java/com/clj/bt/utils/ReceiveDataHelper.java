package com.clj.bt.utils;

import com.clj.bt.common.IReceiveDataHelper;

/**
 * 接收数据 工具助手
 */
public class ReceiveDataHelper implements IReceiveDataHelper {
    private final String DATA_CJ188_HEAD = "68";
    private final String DATA_CJ188_END = "16";

    private String hexStr;

    public ReceiveDataHelper(String hexStr) {
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

    @Override
    public boolean isValid() {
        if (hexStr.contains(DATA_CJ188_HEAD) && hexStr.lastIndexOf(DATA_CJ188_END) > -1) {
            String myHexStr = hexStr.substring(hexStr.indexOf(DATA_CJ188_HEAD));
            if (validBasis()) return false;
            return isValidForCommon(myHexStr);
        }
        return false;
    }

    /**
     * 累计水量
     *
     * @return
     */
    @Override
    public String getYSL() {
        String myHexStr = hexStr.substring(hexStr.indexOf(DATA_CJ188_HEAD));
        StringBuilder sb = new StringBuilder();
        sb.append(myHexStr.substring(34, 36))
                .append(myHexStr.substring(32, 34))
                .append(myHexStr.substring(30, 32));
        return String.valueOf(Integer.parseInt(sb.toString()));
    }

    @Override
    public String getMeterAddress() {
        String myHexStr = hexStr.substring(hexStr.indexOf(DATA_CJ188_HEAD));
        StringBuilder sb = new StringBuilder();
        sb.append(myHexStr.substring(16, 18))
                .append(myHexStr.substring(14, 16))
                .append(myHexStr.substring(12, 14))
                .append(myHexStr.substring(10, 12))
                .append(myHexStr.substring(8, 10))
                .append(myHexStr.substring(6, 8))
                .append(myHexStr.substring(4, 6));
        return sb.toString();
    }

    /**
     * 基础信息校验
     *
     * @return true 无效； false 有效
     */
    private boolean validBasis() {
        return hexStr == null || hexStr.isEmpty() || hexStr.length() % 2 == 1;
    }

    public static void main(String[] args) {
        //ReceiveDataHelper helper = new ReceiveDataHelper("68AA010005080081161F9000007698002C999999992C9999999999999900005F16");
        //ReceiveDataHelper helper = new ReceiveDataHelper("68AA0100000508000083030A81003116");
        //ReceiveDataHelper helper = new ReceiveDataHelper("68 10 92 01 00 10 00 00 00 81 16 90 1F 01 00 22 22 00 2C 00 00 00 00 2C 00 00 00 00 00 00 00 00 00 FE 16 ");
        //ReceiveDataHelper helper = new ReceiveDataHelper("68 10 01 00 23 09 18 20 00 81 16 90 1F 01 60 00 00 00 2C 60 00 00 00 2C 60 20 20 00 00 00 00 00 00 DC 16");
        ReceiveDataHelper helper = new ReceiveDataHelper("6810090200100000008116901F0100222200000000000000000016");
        //ReceiveDataHelper helper = new ReceiveDataHelper("6810090200100000008116901F01002222002C000000002C0000000000000000007616");
        System.out.println(helper.isValidForCommon());
        System.out.println(helper.isValid());
        if (helper.isValid()) {
            System.out.println("累计水量：" + helper.getYSL());
            System.out.println("水表编号：" + helper.getMeterAddress());
        }
    }
}
