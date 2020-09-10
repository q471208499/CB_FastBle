package com.clj.fastble.utils;

import java.math.BigInteger;

public class BytesScanUtils {
    public static final String ORDER_01 = "01";
    public static final String ORDER_02 = "02";
    public static final String ORDER_03 = "03";

    private final String DATA_HEADER = "736a";
    private final String DATA_END = "16";

    private String hexStr;

    public BytesScanUtils(String hexStr) {
        this.hexStr = hexStr;
    }

    public BytesScanUtils(byte[] bytes) {
        this.hexStr = HexUtil.encodeHexStr(bytes);
    }

    public boolean isValid() {
        if (hexStr.contains(DATA_HEADER)) {//校验包头
            int dataStart = hexStr.indexOf(DATA_HEADER);
            //System.out.println("数据开始下标：" + dataStart);
            String dateByetsLengthHex = hexStr.substring(dataStart + DATA_HEADER.length(), dataStart + DATA_HEADER.length() + 2);
            //System.out.println("数据字节长度：0x" + dateByetsLengthHex);
            int dateBytesLength = Integer.parseInt(dateByetsLengthHex, 16);
            //System.out.println("数据字节长度：" + dateBytesLength);
            String allADData = hexStr.substring(dataStart, dataStart + dateBytesLength * 2);
            //System.out.println("广播数据：0x" + allADData);
            if (allADData.endsWith(DATA_END)) {//校验包尾
                //String validData = allADData.substring(4 * 2, (dateBytesLength - 2) * 2);//有效数据
                //System.out.println("有效数据：" + validData);
                String toBeSum = allADData.substring(0, (dateBytesLength - 2) * 2);
                //System.out.println("待校验：0x" + toBeSum);
                int sumDec = 0;//十进制 校验和
                for (int i = 0; i < toBeSum.length() / 2; i++) {
                    String itemHex = toBeSum.substring(i * 2, (i + 1) * 2);
                    int item = Integer.parseInt(itemHex, 16);
                    sumDec += item;
                }
                //System.out.println("十进制 校验和：" + sumDec);
                String cs = allADData.substring((dateBytesLength - 2) * 2, (dateBytesLength - 1) * 2);
                //System.out.println("校验匹配：0x" + cs);
                int csInt = Integer.parseInt(cs, 16);
                //System.out.println("校验匹配值：" + csInt);
                int v = sumDec % 256;
                //System.out.println("待校验匹配值：" + v);
                return v == csInt;
            }
        }
        return false;
    }

    public String getOrder() {
        int dataStart = hexStr.indexOf(DATA_HEADER);
        //System.out.println("数据开始下标：" + dataStart);
        String dateByetsLengthHex = hexStr.substring(dataStart + DATA_HEADER.length(), dataStart + DATA_HEADER.length() + 2);
        //System.out.println("数据字节长度：0x" + dateByetsLengthHex);
        int dateByetsLength = Integer.parseInt(dateByetsLengthHex, 16);
        //System.out.println("数据字节长度：" + dateByetsLength);
        String allADData = hexStr.substring(dataStart, dataStart + dateByetsLength * 2);
        //System.out.println("广播数据：0x" + allADData);
        return allADData.substring(6, 8);
    }

    public String getValidData() {
        int dataStart = hexStr.indexOf(DATA_HEADER);
        //System.out.println("数据开始下标：" + dataStart);
        String dateByetsLengthHex = hexStr.substring(dataStart + DATA_HEADER.length(), dataStart + DATA_HEADER.length() + 2);
        //System.out.println("数据字节长度：0x" + dateByetsLengthHex);
        int dateByetsLength = Integer.parseInt(dateByetsLengthHex, 16);
        //System.out.println("数据字节长度：" + dateByetsLength);
        String allADData = hexStr.substring(dataStart, dataStart + dateByetsLength * 2);
        //System.out.println("广播数据：0x" + allADData);
        String validData = allADData.substring(4 * 2, (dateByetsLength - 2) * 2);//有效数据
        //System.out.println("有效数据：0x" + validData);
        return validData;
    }

    public String getYSL() {
        //return getInteger() + "." + getDecimal();
        BigInteger bigintI = new BigInteger(getInteger(), 16);
        int i = bigintI.intValue();
        BigInteger bigintD = new BigInteger(getDecimal(), 16);
        int d = bigintD.intValue() / 100;
        return i + "." + d;
    }

    /**
     * 表用量整数
     *
     * @return
     */
    public String getInteger() {
        int dataStart = hexStr.indexOf(DATA_HEADER);
        String dataStr = hexStr.substring(dataStart + DATA_HEADER.length() + 4, dataStart + DATA_HEADER.length() + 10);
        return HexUtil.formatHexString(HexUtil.r(dataStr));
    }

    /**
     * 表用量小数
     *
     * @return
     */
    public String getDecimal() {
        int dataStart = hexStr.indexOf(DATA_HEADER);
        String dataStr = hexStr.substring(dataStart + DATA_HEADER.length() + 10, dataStart + DATA_HEADER.length() + 14);
        return HexUtil.formatHexString(HexUtil.r(dataStr));
    }

    /**
     * 电压
     *
     * @return
     */
    public String getV() {
        int dataStart = hexStr.indexOf(DATA_HEADER);
        String dataStr = hexStr.substring(dataStart + DATA_HEADER.length() + 14, dataStart + DATA_HEADER.length() + 16);
        StringBuffer sb = new StringBuffer(dataStr);
        sb.insert(1, ".");
        sb.append(" V");
        return sb.toString();
    }

    public static void main(String[] args) {
        String a = "0000736a0d01563412890736024f1688888888";
        BytesScanUtils utils = new BytesScanUtils(a);
        System.out.println("校验结果：" + utils.isValid());
        System.out.println("指令：0x" + utils.getOrder());
        System.out.println("有效数据：0x" + utils.getValidData());
        System.out.println("表用量整数：" + utils.getInteger());
        System.out.println("表用量小数：" + utils.getDecimal());
        System.out.println("电压：" + utils.getV());

        System.out.println(String.format("%02x", 256));
    }

}
