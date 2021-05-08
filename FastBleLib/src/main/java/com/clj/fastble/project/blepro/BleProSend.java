package com.clj.fastble.project.blepro;

import com.clj.fastble.utils.HexUtil;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * to: 高其如
 * 2021.04.12
 */
public class BleProSend {

    /**
     * 表设置
     * //69 13 20 66 55 44 33 22 11 0F 00 00 00 3C 00 02 00 4D 16
     * 69:包头固定69
     * 13:长度
     * 20:命令字，设置水表参数
     * 66 55 44 33 22 11:设置水表地址
     * 0F 00 00 00:设置水表底数
     * 3C:校时间间隔，3C=60  60*2=120秒 每120秒校时一次
     * 02 00:信号发送间隔时间单位为秒，每2秒发送一次
     * 4D:校验和
     * 16:包尾，固定为16
     * <p>
     * 信号间隔 = 大端 转 小端 然后转 10进制
     * 校时间隔 = 16进制 转 10进制 然后 乘以 信号间隔
     *
     * @param meterAddress 表地址
     * @param readNumber   表读数
     * @param time         校时 间隔
     * @param signaling    信号 间隔
     */
    public static byte[] getSettingData(String meterAddress, long readNumber, int time, int signaling) {
        byte[] resultBytes = new byte[19];
        resultBytes[0] = BlePro.HEX_HEAD;
        resultBytes[1] = 0x13;
        resultBytes[2] = 0x20;
        byte[] meterAddressBytes = HexUtil.dealStr(meterAddress, 12, true);
        for (int i = 0; i < meterAddressBytes.length; i++) {
            resultBytes[3 + i] = meterAddressBytes[i];
        }
        byte[] numberBytes = HexUtil.dealLong(readNumber, 8);
        for (int i = 0; i < numberBytes.length; i++) {
            resultBytes[9 + i] = numberBytes[i];
        }
        //time = time / signaling;
        //resultBytes[13] = (byte) time;
        byte[] tBytes = HexUtil.dealInt(time, 4);
        for (int i = 0; i < tBytes.length; i++) {
            resultBytes[13 + i] = tBytes[i];
        }
        byte[] sBytes = HexUtil.dealInt(signaling, 4);
        for (int i = 0; i < sBytes.length; i++) {
            resultBytes[15 + i] = sBytes[i];
        }
        resultBytes[17] = HexUtil.getCS(resultBytes);
        resultBytes[18] = BlePro.HEX_END;
        return resultBytes;
    }

    private static long getInitNumber(String dStr) {
        double d = Double.parseDouble(dStr);
        DecimalFormat df = new DecimalFormat("#.000");
        return Long.parseLong(df.format(d).replace(".", ""));
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    public static void main(String[] args) {
        byte[] bytes = getSettingData("112233445566", getInitNumber("3900000.293"), 10, 150);
        System.out.println(HexUtil.formatHexString(bytes, true));
    }
}
