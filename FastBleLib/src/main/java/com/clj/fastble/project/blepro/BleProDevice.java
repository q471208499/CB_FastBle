package com.clj.fastble.project.blepro;

import com.clj.fastble.utils.HexUtil;

import java.util.HashMap;
import java.util.Map;

import cn.cb.baselibrary.utils.ABDateUtils;

/**
 * 设备信息收发处理
 * to:高其如
 * 2021年4月14日
 */
public class BleProDevice {

    /**
     * 获取设备信息
     *
     * @return
     */
    public static byte[] getInfoData() {
        String curTime = ABDateUtils.getCurDateStr("yyyyMMddHHmmss");
        byte[] curTimeBytes = HexUtil.dealStr(curTime, curTime.length(), true);

        byte[] resultBytes = new byte[12];
        resultBytes[0] = BlePro.HEX_HEAD;
        resultBytes[1] = 0x0C;
        resultBytes[2] = 0x21;
        for (int i = 0; i < curTimeBytes.length; i++) {
            resultBytes[3 + i] = curTimeBytes[i];
        }
        resultBytes[10] = HexUtil.getCS(resultBytes);
        resultBytes[11] = BlePro.HEX_END;
        return resultBytes;
    }

    public static class Receive {
        private String hexStr;
        private Map<String, Object> dataMap;
        private final String KEY_METER_ADDRESS = "meterAddress";
        private final String KEY_METER_NUMBER = "meterNumber";
        private final String KEY_TIME = "time";
        private final String KEY_SIGNALING = "signaling";
        private final String KEY_DEVICE_ID = "deviceId";
        private final String KEY_SOFTWARE_DATE = "softwareDate";
        private final String KEY_HARDWARE_DATE = "hardwareDate";

        public Receive(String hexStr) {
            this.hexStr = hexStr.replaceAll(" ", "");
        }

        public boolean isValidForCommon() {
            if (validBasis()) return false;
            return isValidForCommon(hexStr);
        }

        private boolean isValidForCommon(String hexStr) {
            String hexHead = hexStr.substring(0, 2);
            String hexEnd = hexStr.substring(hexStr.length() - 2);
            if (!"68".equals(hexHead) || !"16".equals(hexEnd)) return false;//首尾判断

            int hexLength = Integer.parseInt(hexStr.substring(2, 4), 16);
            int hexStrLength = hexStr.length() / 2;
            if (hexLength != hexStrLength) return false;//长度判断

            String cmdStr = hexStr.substring(4, 6);
            if (!"21".equals(cmdStr)) return false;//命令判断

            String csHex = hexStr.substring(hexStr.length() - 4, hexStr.length() - 2);
            int csInt = Integer.parseInt(csHex, 16);
            String toCsHexStr = hexStr.substring(0, hexStr.length() - 4);
            return (byte) csInt == HexUtil.getCSByStr(toCsHexStr);//校验合判断
        }

        private boolean validBasis() {
            return hexStr == null || hexStr.isEmpty() || hexStr.length() % 2 == 1;
        }

        /**
         * 信号间隔 = 大端 转 小端 然后转 10进制
         * 校时间隔 = 16进制 转 10进制 然后 乘以 信号间隔
         *
         * @return
         */
        public Map<String, Object> getDataMap() {
            if (dataMap == null) {
                String flowHex = HexUtil.bigOrSmallEndian(hexStr.substring(18, 26));
                double flow = Long.valueOf(flowHex, 16) / 1000d;

                String signalingStr = HexUtil.bigOrSmallEndian(hexStr.substring(28, 32));
                int signaling = Integer.parseInt(signalingStr, 16);

                String timeStr = HexUtil.bigOrSmallEndian(hexStr.substring(26, 28));
                int time = Integer.parseInt(timeStr, 16) * signaling;

                dataMap = new HashMap<>();
                dataMap.put(KEY_METER_ADDRESS, HexUtil.bigOrSmallEndian(hexStr.substring(6, 18)));
                dataMap.put(KEY_METER_NUMBER, flow);
                dataMap.put(KEY_TIME, time);
                dataMap.put(KEY_SIGNALING, signaling);
                dataMap.put(KEY_DEVICE_ID, HexUtil.bigOrSmallEndian(hexStr.substring(32, 44)));
                dataMap.put(KEY_SOFTWARE_DATE, HexUtil.bigOrSmallEndian(hexStr.substring(44, 48)));
                dataMap.put(KEY_HARDWARE_DATE, HexUtil.bigOrSmallEndian(hexStr.substring(48, 52)));
            }
            return dataMap;
        }

        /**
         * 获取设备地址
         *
         * @return
         */
        public String getMeterAddress() {
            if (dataMap == null) {
                getDataMap();
            }
            return (String) dataMap.get(KEY_METER_ADDRESS);
        }

        /**
         * 水表底数
         *
         * @return
         */
        public double getMeterNumber() {
            if (dataMap == null) {
                getDataMap();
            }
            return (double) dataMap.get(KEY_METER_NUMBER);
        }

        /**
         * 校时间间隔
         *
         * @return
         */
        public int getTime() {
            if (dataMap == null) {
                getDataMap();
            }
            return (int) dataMap.get(KEY_TIME);
        }

        /**
         * 信号发送间隔时间
         *
         * @return
         */
        public int getSignaling() {
            if (dataMap == null) {
                getDataMap();
            }
            return (int) dataMap.get(KEY_SIGNALING);
        }

        /**
         * 设备序列号
         *
         * @return
         */
        public String getDeviceId() {
            if (dataMap == null) {
                getDataMap();
            }
            return (String) dataMap.get(KEY_DEVICE_ID);
        }

        /**
         * 软件日期
         *
         * @return
         */
        public String getSoftwareDate() {
            if (dataMap == null) {
                getDataMap();
            }
            return (String) dataMap.get(KEY_SOFTWARE_DATE);
        }

        /**
         * 硬件日期
         *
         * @return
         */
        public String getHardwareDate() {
            if (dataMap == null) {
                getDataMap();
            }
            return (String) dataMap.get(KEY_HARDWARE_DATE);
        }
    }

    public static void main(String[] args) {
        //比如收到的发送间隔是 0A，就是10秒，校时间隔是 0c 就是12, 那么就用12*10=120
        BleProDevice.Receive receive = new BleProDevice.Receive("68 1c 21 00 00 00 00 00 00 0a 09 00 00 0c 0a 00 45 4c 42 2d 59 53 01 18 03 21 b7 16");
        System.out.println(receive.isValidForCommon());
        System.out.println(receive.getDataMap());
        System.out.println(receive.getDeviceId());
    }
}
