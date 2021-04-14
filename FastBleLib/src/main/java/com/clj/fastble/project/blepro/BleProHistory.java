package com.clj.fastble.project.blepro;

import com.clj.fastble.utils.HexUtil;

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
        byte[] resultBytes = new byte[12];
        resultBytes[0] = 0x69;
        resultBytes[1] = 0x0C;
        resultBytes[2] = 0x30;
        resultBytes[3] = HexUtil.str2Bcd(String.valueOf(dayOfMonth))[0];
        resultBytes[4] = HexUtil.str2Bcd(String.valueOf(month))[0];
        resultBytes[5] = HexUtil.str2Bcd(String.valueOf(year))[1];
        resultBytes[6] = HexUtil.dealStr(Integer.toHexString(days), 4, false)[0];
        resultBytes[7] = HexUtil.dealStr(Integer.toHexString(days), 4, false)[1];
        resultBytes[8] = 0x00;//填充字节
        resultBytes[9] = 0x00;//填充字节
        resultBytes[10] = HexUtil.getCS(resultBytes);
        resultBytes[11] = 0x16;
        return resultBytes;
    }

    public static void main(String[] args) {
        System.out.println(HexUtil.formatHexString(getBytesDate(2021, 4, 14, 10), true));
    }
}
