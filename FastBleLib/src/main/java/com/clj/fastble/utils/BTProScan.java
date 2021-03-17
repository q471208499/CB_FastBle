package com.clj.fastble.utils;

import java.util.HashMap;
import java.util.Map;

public class BTProScan {

    public static final String HEX_HEAD = "68";
    public static final String HEX_END = "16";
    /**
     * 指令
     */
    public static final String DATA_KEY_CMD = "cmd";
    /**
     * 数据包的时间(手机APP上需要显示蓝牙表的时间偏差，如果偏差超过10分钟就用红色显示，否则用正常颜色显示。)
     */
    public static final String DATA_KEY_TIME = "time";
    /**
     * 累计读数
     */
    public static final String DATA_KEY_FLOW = "flow";
    /**
     * 反向累计数
     */
    public static final String DATA_KEY_CONTRARY = "contrary";
    /**
     * 瞬时流量值
     */
    public static final String DATA_KEY_INSTANTANEOUS = "instantaneous";
    /**
     * 连续用水时长
     */
    public static final String DATA_KEY_USAGE_TIME = "usageTime";
    /**
     * 电池电压
     */
    public static final String DATA_KEY_VOLTAGE = "voltage";
    /**
     * 水表状态
     */
    public static final String DATA_KEY_WARNINGS = "warnings";
    /**
     * 流向状态：1:	倒行  			0:	正常
     */
    public static final String DATA_KEY_FLOW_STATUS = "flowStatus";
    /**
     * 1: 	校时失败		0:	校时成功
     */
    public static final String DATA_KEY_TIME_STATUS = "timeStatus";
    /**
     * 1:	无磁异常		0:	正常
     */
    public static final String DATA_KEY_MAG_STATUS = "magStatus";
    /**
     * 1:	电池欠压		0:	电池正常
     */
    public static final String DATA_KEY_VOLTAGE_STATUS = "voltageStatus";
    /**
     * 无磁传感信号强度值：0-15
     */
    public static final String DATA_KEY_SIGNAL = "signal";

    private final int RADIX_16 = 16;


    private String adHex;
    private Map<String, Object> dataMap = new HashMap<>();

    public BTProScan(String adHex) {
        this.adHex = adHex;
    }

    public BTProScan(byte[] bytes) {
        this.adHex = HexUtil.encodeHexStr(bytes);
    }

    public boolean verify() {
        if (adHex == null || adHex.trim().isEmpty()) return false;//字符串异常

        if (adHex.contains(HEX_HEAD) && adHex.contains(HEX_END)) {//校验包头
            if (adHex.indexOf(HEX_HEAD) - 2 > -1)
                adHex = adHex.substring(adHex.indexOf(HEX_HEAD) - 2);
            else
                return false;
        } else
            return false;

        adHex = adHex.replace(" ", "");
        String head = adHex.substring(2, 4);
        String end = adHex.substring(adHex.length() - 2);
        if (!HEX_HEAD.equals(head) || !HEX_END.equals(end)) return false;//标识头/尾 不匹配

        int lengthCal = Integer.valueOf(adHex.substring(0, 2), RADIX_16);
        int length = (adHex.length() - 2) / 2;
        if (lengthCal != length) return false;// 长度 不匹配

        String csHex = adHex.substring(adHex.length() - 4, adHex.length() - 2);
        String toCalHex = adHex.substring(2, adHex.length() - 4);
        int csCalInt = calCs(toCalHex);

        if (Integer.valueOf(csHex, RADIX_16) == csCalInt % 256) {
            packageDada();
            return true;
        }
        return false;
    }

    /**
     * 校验和
     */
    private int calCs(String toCalHex) {
        int sumDec = 0;//十进制 校验和
        for (int i = 0; i < toCalHex.length() / 2; i++) {
            String itemHex = toCalHex.substring(i * 2, (i + 1) * 2);
            int item = Integer.parseInt(itemHex, RADIX_16);
            sumDec += item;
        }
        return sumDec;
    }

    private void packageDada() {
        String cmdHex = adHex.substring(4, 6);
        String time = HexUtil.bigOrSmallEndian(adHex.substring(6, 10));
        String flowHex = HexUtil.bigOrSmallEndian(adHex.substring(10, 18));
        double flow = Long.valueOf(flowHex, RADIX_16) / 1000d;
        String contraryHex = HexUtil.bigOrSmallEndian(adHex.substring(18, 26));
        double contrary = Long.valueOf(contraryHex, RADIX_16) / 1000d;
        String instantaneousHex = HexUtil.bigOrSmallEndian(adHex.substring(26, 30));
        long instantaneous = Long.valueOf(instantaneousHex, RADIX_16);
        String usageTimeHex = HexUtil.bigOrSmallEndian(adHex.substring(30, 34));
        long usageTime = Long.valueOf(usageTimeHex, RADIX_16);
        String voltageHex = HexUtil.bigOrSmallEndian(adHex.substring(34, 36));
        double voltage = (Integer.parseInt(voltageHex, RADIX_16) + 250) / 100d;
        String warningHex = adHex.substring(36, 38);
        String warningStr = Integer.toBinaryString(Integer.parseInt(warningHex, 16));
        String warnings = warningStr.length() > 4 ? warningStr.substring(0, warningStr.length() - 4) : "0";
        warnings = String.format("%04d", Integer.parseInt(warnings));
        int flowStatus = Integer.parseInt(warnings.substring(0, 1));
        int timeStatus = Integer.parseInt(warnings.substring(1, 2));
        int magStatus = Integer.parseInt(warnings.substring(2, 3));
        int voltageStatus = Integer.parseInt(warnings.substring(3, 4));
        int signal = Integer.parseInt(warningHex, 16) % 16;
        dataMap.put(DATA_KEY_CMD, cmdHex);
        dataMap.put(DATA_KEY_TIME, time);
        dataMap.put(DATA_KEY_FLOW, flow);
        dataMap.put(DATA_KEY_CONTRARY, contrary);
        dataMap.put(DATA_KEY_INSTANTANEOUS, instantaneous);
        dataMap.put(DATA_KEY_USAGE_TIME, usageTime);
        dataMap.put(DATA_KEY_VOLTAGE, voltage);
        dataMap.put(DATA_KEY_WARNINGS, warnings);
        dataMap.put(DATA_KEY_FLOW_STATUS, flowStatus);
        dataMap.put(DATA_KEY_TIME_STATUS, timeStatus);
        dataMap.put(DATA_KEY_MAG_STATUS, magStatus);
        dataMap.put(DATA_KEY_VOLTAGE_STATUS, voltageStatus);
        dataMap.put(DATA_KEY_SIGNAL, signal);
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    /**
     * 指令
     */
    public String getCmd() {
        return (String) dataMap.get(DATA_KEY_CMD);
    }

    /**
     * 数据包的时间(手机APP上需要显示蓝牙表的时间偏差，如果偏差超过10分钟就用红色显示，否则用正常颜色显示。)
     */
    public String getTime() {
        return (String) dataMap.get(DATA_KEY_TIME);
    }

    /**
     * 累计读数
     */
    public double getFlow() {
        return (double) dataMap.get(DATA_KEY_FLOW);
    }

    /**
     * 反向累计数
     */
    public double getContrary() {
        return (double) dataMap.get(DATA_KEY_CONTRARY);
    }

    /**
     * 瞬时流量值
     */
    public long getInstantaneous() {
        return (long) dataMap.get(DATA_KEY_INSTANTANEOUS);
    }

    /**
     * 连续用水时长
     */
    public long getUsageTime() {
        return (long) dataMap.get(DATA_KEY_USAGE_TIME);
    }

    /**
     * 电池电压
     */
    public double getVoltage() {
        return (double) dataMap.get(DATA_KEY_VOLTAGE);
    }

    /**
     * 水表状态
     */
    public String getWarnings() {
        return (String) dataMap.get(DATA_KEY_WARNINGS);
    }

    /**
     * 流向状态：1:	倒行  			0:	正常
     */
    public int getFlowStatus() {
        return (int) dataMap.get(DATA_KEY_FLOW_STATUS);
    }

    /**
     * 1: 	校时失败		0:	校时成功
     */
    public int getTimeStatus() {
        return (int) dataMap.get(DATA_KEY_TIME_STATUS);
    }

    /**
     * 1:	无磁异常		0:	正常
     */
    public int getMagStatus() {
        return (int) dataMap.get(DATA_KEY_MAG_STATUS);
    }

    /**
     * 1:	电池欠压		0:	电池正常
     */
    public int getVoltageStatus() {
        return (int) dataMap.get(DATA_KEY_VOLTAGE_STATUS);
    }

    /**
     * 无磁传感信号强度值：0-15
     */
    public int getSignal() {
        return (int) dataMap.get(DATA_KEY_SIGNAL);
    }

    public static void main(String[] args) {
        String hex = "136801007874D75D5E5098CEB7AEA42E78004C16";
        System.out.println(hex.substring(hex.indexOf(HEX_HEAD) - 2));
        BTProScan proScan = new BTProScan(hex);
        if (proScan.verify()) {
            System.out.println(proScan.getDataMap());
        }
    }
}
