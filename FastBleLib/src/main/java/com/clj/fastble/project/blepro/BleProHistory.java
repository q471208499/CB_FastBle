package com.clj.fastble.project.blepro;

import com.clj.fastble.utils.HexUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.cb.baselibrary.utils.ABDateUtils;

public class BleProHistory {

    /**
     * 获取历史数据 发送帧
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @param days
     * @return
     */
    public static byte[] getBytesDate(int year, int month, int dayOfMonth, int days) {
        byte[] resultBytes = new byte[19];
        resultBytes[0] = 0x69;
        resultBytes[1] = 0x13;
        resultBytes[2] = 0x30;

        String curTime = ABDateUtils.getCurDateStr("yyyyMMddHHmmss");
        byte[] curTimeBytes = HexUtil.dealStr(curTime, curTime.length(), true);
        for (int i = 0; i < curTimeBytes.length; i++) {
            resultBytes[3 + i] = curTimeBytes[i];
        }

        resultBytes[10] = HexUtil.str2Bcd(String.valueOf(dayOfMonth))[0];
        resultBytes[11] = HexUtil.str2Bcd(String.valueOf(month))[0];
        resultBytes[12] = HexUtil.str2Bcd(String.valueOf(year))[1];
        resultBytes[13] = HexUtil.dealStr(Integer.toHexString(days), 4, false)[0];
        resultBytes[14] = HexUtil.dealStr(Integer.toHexString(days), 4, false)[1];
        resultBytes[15] = 0x00;//填充字节
        resultBytes[16] = 0x00;//填充字节
        resultBytes[17] = HexUtil.getCS(resultBytes);
        resultBytes[18] = 0x16;
        return resultBytes;
    }

    public static class Receive {
        private String hexStr;
        private Map<String, Object> dataMap;
        private final String KEY_INDEX = "index";
        private final String KEY_DATE = "date";
        private final String KEY_START = "start";
        private final String KEY_INCREMENT_ARR = "increment";
        private final String KEY_READ_NUMBER_ARR = "readNumber";

        public Receive(String hexStr) {
            this.hexStr = hexStr.replaceAll(" ", "");
        }

        public boolean isValidForCommon() {
            if (validBasis()) return false;
            return isValidForCommon(hexStr);
        }

        private boolean validBasis() {
            return hexStr == null || hexStr.isEmpty() || hexStr.length() % 2 == 1;
        }

        private boolean isValidForCommon(String hexStr) {
            String hexHead = hexStr.substring(0, 2);
            String hexEnd = hexStr.substring(hexStr.length() - 2);
            if (!"68".equals(hexHead) || !"16".equals(hexEnd)) return false;//首尾判断

            int hexLength = Integer.parseInt(hexStr.substring(2, 4), 16);
            int hexStrLength = hexStr.length() / 2;
            if (hexLength != hexStrLength) return false;//长度判断

            String cmdStr = hexStr.substring(4, 6);
            if (!"30".equals(cmdStr)) return false;//命令判断

            String csHex = hexStr.substring(hexStr.length() - 4, hexStr.length() - 2);
            int csInt = Integer.parseInt(csHex, 16);
            String toCsHexStr = hexStr.substring(0, hexStr.length() - 4);
            return (byte) csInt == HexUtil.getCSByStr(toCsHexStr);//校验合判断
        }

        public Map<String, Object> getDataMap() {
            if (dataMap == null) {
                int index = Integer.parseInt(hexStr.substring(6, 8), 16);
                String datePrefix = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)).substring(0, 2);
                String dateStr = datePrefix + HexUtil.bigOrSmallEndian(hexStr.substring(8, 14));
                int start = Integer.parseInt(HexUtil.bigOrSmallEndian(hexStr.substring(14, 22)), 16);
                String flowListStr = hexStr.substring(22, hexStr.length() - 8);
                int arrSize = flowListStr.length() / 4;
                int[] increment = new int[arrSize];
                int[] readNumber = new int[arrSize];
                for (int i = 0; i < increment.length; i++) {
                    increment[i] = Integer.parseInt(HexUtil.bigOrSmallEndian(flowListStr.substring(i * 4, i * 4 + 4)), 16);
                    if (i == 0)
                        readNumber[i] = start + increment[i];
                    else
                        readNumber[i] = readNumber[i - 1] + increment[i];
                }

                dataMap = new HashMap<>();
                dataMap.put(KEY_INDEX, index);
                dataMap.put(KEY_DATE, dateStr);
                dataMap.put(KEY_START, start);
                dataMap.put(KEY_INCREMENT_ARR, increment);
                dataMap.put(KEY_READ_NUMBER_ARR, readNumber);
            }
            return dataMap;
        }

        /**
         * 帧数 序号 下标
         *
         * @return
         */
        public int getIndex() {
            if (dataMap == null) {
                getDataMap();
            }
            return (int) dataMap.get(KEY_INDEX);
        }

        /**
         * 数据日期
         *
         * @return
         */
        public String getDate() {
            if (dataMap == null) {
                getDataMap();
            }
            return (String) dataMap.get(KEY_DATE);
        }

        /**
         * 起始读数
         *
         * @return
         */
        public int getStart() {
            if (dataMap == null) {
                getDataMap();
            }
            return (int) dataMap.get(KEY_START);
        }

        /**
         * 流量增量
         *
         * @return
         */
        public int[] getIncrement() {
            if (dataMap == null) {
                getDataMap();
            }
            return (int[]) dataMap.get(KEY_INCREMENT_ARR);
        }

        /**
         * 读数
         *
         * @return
         */
        public int[] getReadNumber() {
            if (dataMap == null) {
                getDataMap();
            }
            return (int[]) dataMap.get(KEY_READ_NUMBER_ARR);
        }
    }

    public static void main(String[] args) {
        System.out.println(HexUtil.formatHexString(getBytesDate(2021, 4, 14, 10), true));
        String hex = "68 3D 30 01 15 04 21 15 CD 5B 07 00 08 00 10 00 01 00 05 00 08 00 10 00 01 00 05 00 08 00 10 00 01 00 05 00 08 00 10 00 01 00 05 00 08 00 10 00 01 00 05 00 08 00 10 00 01 AF 16 C8 16";

        BleProHistory.Receive receive = new Receive(hex);
        System.out.println(receive.isValidForCommon());
        System.out.println(receive.getDataMap());
        System.out.println(Arrays.toString(receive.getIncrement()));
        System.out.println(Arrays.toString(receive.getReadNumber()));
    }
}
