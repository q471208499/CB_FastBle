package com.clj.fastble.project.blepro;

import com.clj.fastble.utils.HexUtil;

/**
 * to: 高其如
 * 2021.04.12
 */
public class BleProSend {

    /**
     * 表设置
     * //69 12 20 66 55 44 33 22 11 0F 00 00 00 3C 02 00 4D 16
     * 69:包头固定69
     * 12:长度
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
    public static byte[] getSettingData(String meterAddress, int readNumber, int time, int signaling) {
        byte[] resultBytes = new byte[18];
        resultBytes[0] = BlePro.HEX_HEAD;
        resultBytes[1] = 0x12;
        resultBytes[2] = 0x20;
        byte[] meterAddressBytes = HexUtil.dealStr(meterAddress, 12, true);
        for (int i = 0; i < meterAddressBytes.length; i++) {
            resultBytes[3 + i] = meterAddressBytes[i];
        }
        byte[] numberBytes = HexUtil.dealInt(readNumber, 8);
        for (int i = 0; i < numberBytes.length; i++) {
            resultBytes[9 + i] = numberBytes[i];
        }
        time = time / signaling;
        resultBytes[13] = (byte) time;
        byte[] sBytes = HexUtil.dealInt(signaling, 4);
        for (int i = 0; i < sBytes.length; i++) {
            resultBytes[14 + i] = sBytes[i];
        }
        resultBytes[16] = HexUtil.getCS(resultBytes);
        resultBytes[17] = BlePro.HEX_END;
        return resultBytes;
    }

    public static void main(String[] args) {
        byte[] bytes = getSettingData("112233445566", 15, 120, 10);
        System.out.println(HexUtil.formatHexString(bytes, true));
    }
}
