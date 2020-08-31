package com.clj.fastble.utils;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * 杭州莱智科技 蓝牙助手
 * 2020年8月27日
 * mAddress: D8:A9:8B:A4:9F:FA
 * getName: name = XC-LINK-8BA49FFA
 */
public class LzBleHelper {
    private static final int RADIX = 16;
    private static final String BLE_HEAD_STR = "7E";
    private static final String BLE_END_STR = "0D";
    private static final String DATA_HEAD_STR = "AA";
    private static final String DATA_END_STR = "55";
    private static final String CMD_8300 = "8300";
    private static final String CMD_C200 = "C200";
    private static final String CMD_91 = "91";
    private static final String CMD_AUTH = "F0AAAAAA";
    private static final int DATA_SIZE_IN_FRAME = 30;

    /**
     * 蓝牙主服务UUID: '0000FF12-0000-1000-8000-00805F9B34FB'
     */
    private static final String UUID_SERVER = "0000FF12-0000-1000-8000-00805F9B34FB";
    /**
     * 写数据的特征值 0000FF01-0000-1000-8000-00805F9B34FB
     */
    private static final String UUID_WRITER = "0000FF01-0000-1000-8000-00805F9B34FB";
    /**
     * 订阅的特征值 0000FF02-0000-1000-8000-00805F9B34FB
     */
    private static final String UUID_NOTIFY = "0000FF02-0000-1000-8000-00805F9B34FB";

    /**************************【控制码 START】*****************************/
    public static final int RESPONSE_FAIL = 0xC100;//响应失败
    public static final int RESPONSE_SUCCESS = 0xC200;//响应成功

    public static final int RESPONSE_MSG = 0x4000;//响应有数据
    public static final int RESPONSE_OPERATION_FAIL = 0x8100;//响应成功，操作失败
    public static final int RESPONSE_OPERATION_SUCCESS = 0x8200;//响应成功，操作成功
    public static final int RESPONSE_OPERATION_MSG = 0x8300;//响应成功，有返回数据

    public static final int RESPONSE_INNER_READ_METER = 0x91;//响应抄表
    public static final int REQUEST_INNER_READ_METER = 0x11;//请求抄表
    public static final int RESPONSE_INNER_CLOSE_VALUE = 0x0A;//请求关阀
    public static final int REQUEST_INNER_CLOSE_VALUE = 0x8A;//请求关阀
    public static final int RESPONSE_INNER_OPEN_VALUE = 0x09;//请求开阀
    public static final int REQUEST_INNER_OPEN_VALUE = 0x89;//请求开阀

    /**************************【控制码 FINISH】*****************************/

    /******************************【判断蓝牙广播是否是莱智 START】*********************************/
    private static String LZ_BLE_NAME_PREFIX = "XC-LINK";//莱智蓝牙名称前缀

    public static boolean isLzBle(BluetoothDevice device) {
        if (device == null || device.getName() == null || device.getName().isEmpty()) {
            return false;
        }
        //String miniMac = getMiniMac(device.getAddress()).substring(6);
        return device.getName().startsWith(LZ_BLE_NAME_PREFIX);
    }

    private static String getMiniMac(String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }
        return address.replaceAll(":", "");
    }

    /******************************【判断蓝牙广播是否是莱智 FINISH】*********************************/

    /**
     * 校验值 总和
     *
     * @param hexData 需要求和的数据
     * @return
     */
    private static int getAllCheckValue(String hexData) {
        int sum = 0;
        for (int i = 0; i < hexData.length() / 2; i++) {
            sum += Integer.parseInt(hexData.substring(i * 2, (i + 1) * 2), RADIX);
        }
        return sum;
    }

    /**
     * 单个字节的 16 进制 转换成 8 位二进制
     *
     * @param hex
     * @return
     */
    private static String byte2Binary(String hex) {
        Integer num = Integer.parseInt(hex, 16);
        return String.format("%08d", Integer.valueOf(Integer.toBinaryString(num)));
    }

    public static class Send {
        private String meterAddressStr;

        public Send(String meterAddress) {
            meterAddressStr = meterAddress;
        }

        /**
         * 单表操作 0xF111
         *
         * @return
         */
        public List<byte[]> signRead() {
            String innerStr = "0511" + meterAddressStr;
            int innerV = getAllCheckValue(innerStr) % 256;
            String innerVHex = Integer.toHexString(innerV);
            String middleStr = "0FF111" + meterAddressStr + DATA_HEAD_STR + innerStr + innerVHex + DATA_END_STR;
            int middleV = getAllCheckValue(middleStr) % 256;
            String middleVHex = Integer.toHexString(middleV);
            String dataStr = DATA_HEAD_STR + middleStr + middleVHex + DATA_END_STR;
            return cutBackage(dataStr);
        }

        /**
         * 设置发送功率 0xF12A
         *
         * @return
         */
        public List<byte[]> setSendPower(int l) {
            String dataStr = "0CF12A" + meterAddressStr + "01" + CMD_AUTH + String.format("%02x", l);
            int v = getAllCheckValue(dataStr) % 256;
            String vHex = Integer.toHexString(v);
            dataStr = DATA_HEAD_STR + dataStr + vHex + DATA_END_STR;
            return cutBackage(dataStr);
        }

        /**
         * 设置手持机ID 0x1600
         *
         * @return
         */
        public List<byte[]> setHandheldId(String handheldId) {
            String dataStr = "061600" + handheldId;
            int v = getAllCheckValue(dataStr) % 256;
            String vHex = Integer.toHexString(v);
            dataStr = DATA_HEAD_STR + dataStr + vHex + DATA_END_STR;
            return cutBackage(dataStr);
        }

        /**
         * 设置RF功率 0x1100
         *
         * @return
         */
        public List<byte[]> setRF(int l) {
            String dataStr = "031100" + String.format("%02x", l);
            int v = getAllCheckValue(dataStr) % 256;
            String vHex = Integer.toHexString(v);
            dataStr = DATA_HEAD_STR + dataStr + vHex + DATA_END_STR;
            return cutBackage(dataStr);
        }

        /**
         * 设置信息保存 0x1900
         *
         * @return
         */
        public List<byte[]> setSetting() {
            String dataStr = DATA_HEAD_STR + "0219001B" + DATA_END_STR;
            return cutBackage(dataStr);
        }

        /**
         * 操作信息保存 0x1A00
         *
         * @return
         */
        public List<byte[]> setOperation() {
            String dataStr = DATA_HEAD_STR + "021A001C" + DATA_END_STR;
            return cutBackage(dataStr);
        }

        /**
         * 测试信号强度 0x0DFF
         *
         * @return
         */
        public List<byte[]> testRssi() {
            return null;
        }

        /**
         * 设置手持机信号通道 0x1200
         *
         * @return
         */
        public List<byte[]> setHandheldChannel() {
            return null;
        }

        /**
         * 设置表信号通道 0xF120
         *
         * @return
         */
        public List<byte[]> setMeterChannel() {
            return null;
        }

        private List<byte[]> cutBackage(String dataStr) {
            int listSize = dataStr.length() / DATA_SIZE_IN_FRAME + (dataStr.length() % DATA_SIZE_IN_FRAME == 0 ? 0 : 1);
            List<byte[]> result = new ArrayList<byte[]>(listSize);
            for (int i = 0; i < listSize; i++) {
                int endIndex = (i + 1) * DATA_SIZE_IN_FRAME > dataStr.length() ? dataStr.length() : (i + 1) * DATA_SIZE_IN_FRAME;
                String dataTemp = dataStr.substring(i * DATA_SIZE_IN_FRAME, endIndex);
                String dataTempLengthHex = String.format("%02x", dataTemp.length() / 2);
                if (dataTemp.length() < DATA_SIZE_IN_FRAME)
                    dataTemp = fill(dataTemp, DATA_SIZE_IN_FRAME, "0");
                int tempV = getAllCheckValue(dataTemp) % 256;
                String tempVHex = Integer.toHexString(tempV);
                dataTemp = BLE_HEAD_STR + listSize + (i + 1) + dataTempLengthHex + dataTemp + tempVHex + BLE_END_STR;
                System.out.println(dataTemp);
                byte[] bs = HexUtil.str2Bcd(dataTemp);
                result.add(bs);
            }
            return result;
        }

        private String fill(String str, int length, String fillKey) {
            if (str.length() >= length) {
                return str;
            }
            StringBuilder sb = new StringBuilder(str);
            while (sb.length() < length) {
                sb.append(fillKey);
            }
            return sb.toString();
        }
    }

    public static class Receive {
        private List<byte[]> bleList = new ArrayList<>();
        private StringBuilder sb = new StringBuilder();//最外层有效数据
        private boolean hasNext = false;
        private boolean conformOuter = false;
        private String cmd;
        private String meterInfoHex;
        private String meterAddress;

        public Receive(byte[] bytes) {
            this.bleList.add(bytes);
            conformOuter = checkOuter();
        }

        private boolean checkOuter() {
            byte[] bytes = bleList.get(bleList.size() - 1);
            String hexStr = HexUtil.formatHexString(bytes).toUpperCase();
            int bleHeadIndex = hexStr.indexOf(BLE_HEAD_STR);
            int bleEndIndex = hexStr.lastIndexOf(BLE_END_STR);
            if (bleHeadIndex >= bleEndIndex) {
                return false;
            }
            String next = hexStr.substring(bleHeadIndex + 2, bleHeadIndex + 2 + 2);
            int[] nextInts = new int[]{Integer.valueOf(next.substring(0, 1)), Integer.valueOf(next.substring(1, 2))};
            hasNext = nextInts[0] > nextInts[1];
            int dataLengthInFrame = Integer.parseInt(hexStr.substring(bleHeadIndex + 4, bleHeadIndex + 4 + 2), RADIX);//一帧数据中，有效数据长度
            int sum = getAllCheckValue(hexStr.substring(6, hexStr.length() - 4)) % 256;
            int sumByteValue = Integer.parseInt(hexStr.substring(hexStr.length() - 4, hexStr.length() - 2), RADIX);
            if (sum == sumByteValue) {
                sb.append(hexStr, 6, dataLengthInFrame * 2 + 6);
                return true;
            }
            return false;
        }

        public boolean addFrame(byte[] bytes) {
            bleList.add(bytes);
            conformOuter = checkOuter();
            if (!isConformOuter()) {
                bleList.remove(bytes);
                return false;
            }
            return true;
        }

        public boolean checkMiddle() {
            String hexStr = sb.toString();
            int bleHeadIndex = hexStr.indexOf(DATA_HEAD_STR);
            int bleEndIndex = hexStr.lastIndexOf(DATA_END_STR);
            if (bleHeadIndex >= bleEndIndex) {
                return false;
            }
            int dataLength = Integer.parseInt(hexStr.substring(bleHeadIndex + 2, bleHeadIndex + 4), RADIX);
            cmd = hexStr.substring(bleHeadIndex + 4, bleHeadIndex + 8);
            if (CMD_C200.equals(cmd) && dataLength == 4) return true;
            if (CMD_8300.equals(cmd)) {
                String meterAddressStr = hexStr.substring(bleHeadIndex + 12, bleHeadIndex + 20);
                boolean sameMeterAddress = meterAddressStr.equals(meterAddress);
                int sum = getAllCheckValue(hexStr.substring(2, hexStr.length() - 4)) % 256;
                int sumByteValue = Integer.parseInt(hexStr.substring(hexStr.length() - 4, hexStr.length() - 2), RADIX);
                boolean validValue = sum == sumByteValue;
                boolean validLength = dataLength == hexStr.substring(4, hexStr.length() - 4).length() / 2;
                if (sameMeterAddress && validValue && validLength) {
                    meterInfoHex = hexStr.substring(20, hexStr.length() - 4);
                    return true;
                }
            }
            return false;
        }

        private boolean checkMeterInfo() {
            String hexStr = meterInfoHex;
            int bleHeadIndex = hexStr.indexOf(DATA_HEAD_STR);
            int bleEndIndex = hexStr.lastIndexOf(DATA_END_STR);
            if (bleHeadIndex >= bleEndIndex) {
                return false;
            }
            int dataLength = Integer.parseInt(hexStr.substring(bleHeadIndex + 2, bleHeadIndex + 4), RADIX);
            String cmd = hexStr.substring(bleHeadIndex + 4, bleHeadIndex + 6);
            String meterAddressStr = hexStr.substring(bleHeadIndex + 6, bleHeadIndex + 14);
            boolean sameMeterAddress = meterAddressStr.equals(meterAddress);
            int sum = getAllCheckValue(hexStr.substring(2, hexStr.length() - 4)) % 256;
            int sumByteValue = Integer.parseInt(hexStr.substring(hexStr.length() - 4, hexStr.length() - 2), RADIX);
            boolean validValue = sum == sumByteValue;
            boolean validLength = dataLength == hexStr.substring(4, hexStr.length() - 4).length() / 2;
            if (validValue && validLength && sameMeterAddress && CMD_91.equals(cmd)) {
                return true;
            }
            return false;
        }

        public Bean getBean() {
            return checkMiddle() && checkMeterInfo() ? new Bean(meterInfoHex) : null;
        }

        public boolean isConformOuter() {
            return conformOuter;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public String getData() {
            return sb.toString();
        }

        public void setMeterAddress(String meterAddress) {
            this.meterAddress = meterAddress;
        }

        public int getCmd() {
            checkMiddle();
            return Integer.parseInt(cmd, 16);
        }
    }

    public static class Bean {
        private String meterCumuUsage;//表底数（累计用量）
        private String status0L;//表状态 低位0：阀门，0开阀；1关阀
        private String status1L;//表状态 低位1：角度，0正常，1异常
        private String status2L;//表状态 低位2：高温，0正常，1异常
        private String status3L;//表状态 低位3：磁攻击，0正常，1异常
        private String status4L;//表状态 低位4：过流，0正常，1过流
        private String status5L;//表状态 低位5：电压低，0正常，1电压低
        private String status6L;//表状态 低位6：阀门，0正常，1异常
        private String status7L;//表状态 低位7：存储，0正常，1异常
        private String status0H;//表状态 高位0：传感器，0正常，1异常
        private String voltage;//电池电压
        private String temp;//温度
        private String angle;//角度

        public Bean(String meterCumuUsage, String status0L, String status1L, String status2L,
                    String status3L, String status4L, String status5L, String status6L,
                    String status7L, String status0H, String voltage, String temp, String angle) {
            this.meterCumuUsage = meterCumuUsage;
            this.status0L = status0L;
            this.status1L = status1L;
            this.status2L = status2L;
            this.status3L = status3L;
            this.status4L = status4L;
            this.status5L = status5L;
            this.status6L = status6L;
            this.status7L = status7L;
            this.status0H = status0H;
            this.voltage = voltage;
            this.temp = temp;
            this.angle = angle;
        }

        public Bean() {
        }

        public Bean(String hexStr) {
            String meterCumuUsageHex = hexStr.substring(14, 22);
            String statusHex = hexStr.substring(22, 26);
            String voltageHex = hexStr.substring(26, 30);
            String tempHex = hexStr.substring(30, 32);
            String angleHex = hexStr.substring(32, 34);

            meterCumuUsageHex = Long.toHexString(getLong(string2Bytes(meterCumuUsageHex), true));
            //statusHex = Long.toHexString(getLong(string2Bytes(statusHex), true));
            voltageHex = Long.toHexString(getLong(string2Bytes(voltageHex), true));
            tempHex = Long.toHexString(getLong(string2Bytes(tempHex), true));
            angleHex = Long.toHexString(getLong(string2Bytes(angleHex), true));

            setStatus(statusHex, true);
            setMeterCumuUsage(meterCumuUsageHex, true);
            setVoltage(voltageHex, true);
        }

        private void setStatus(String statusHex, boolean myself) {
            String statusBinLow = byte2Binary(statusHex.substring(0, 2));
            String statusBinHigh = byte2Binary(statusHex.substring(2, 4));
            status0L = statusBinLow.substring(7, 8);
            status1L = statusBinLow.substring(6, 7);
            status2L = statusBinLow.substring(5, 6);
            status3L = statusBinLow.substring(4, 5);
            status4L = statusBinLow.substring(3, 4);
            status5L = statusBinLow.substring(2, 3);
            status6L = statusBinLow.substring(1, 2);
            status7L = statusBinLow.substring(0, 1);
            status0H = statusBinHigh.substring(7, 8);
        }

        private void setVoltage(String voltageHex, boolean myself) {
            String m = String.valueOf(Integer.parseInt(voltageHex, 16));
            voltage = new StringBuilder(m).insert(m.length() - 2, ".").toString();
        }

        private void setMeterCumuUsage(String meterCumuUsageHex, boolean myself) {
            String m = String.valueOf(Integer.parseInt(meterCumuUsageHex, 16));
            meterCumuUsage = new StringBuilder(m).insert(m.length() - 2, ".").toString();
        }

        public void setMeterCumuUsage(String meterCumuUsage) {
            this.meterCumuUsage = meterCumuUsage;
        }

        public void setStatus0L(String status0L) {
            this.status0L = status0L;
        }

        public void setStatus1L(String status1L) {
            this.status1L = status1L;
        }

        public void setStatus2L(String status2L) {
            this.status2L = status2L;
        }

        public void setStatus3L(String status3L) {
            this.status3L = status3L;
        }

        public void setStatus4L(String status4L) {
            this.status4L = status4L;
        }

        public void setStatus5L(String status5L) {
            this.status5L = status5L;
        }

        public void setStatus6L(String status6L) {
            this.status6L = status6L;
        }

        public void setStatus7L(String status7L) {
            this.status7L = status7L;
        }

        public void setStatus0H(String status0H) {
            this.status0H = status0H;
        }

        public void setVoltage(String voltage) {
            this.voltage = voltage;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public void setAngle(String angle) {
            this.angle = angle;
        }

        public String getMeterCumuUsage() {
            return meterCumuUsage;
        }

        public String getStatus0L() {
            return status0L;
        }

        public String getStatus1L() {
            return status1L;
        }

        public String getStatus2L() {
            return status2L;
        }

        public String getStatus3L() {
            return status3L;
        }

        public String getStatus4L() {
            return status4L;
        }

        public String getStatus5L() {
            return status5L;
        }

        public String getStatus6L() {
            return status6L;
        }

        public String getStatus7L() {
            return status7L;
        }

        public String getStatus0H() {
            return status0H;
        }

        public String getVoltage() {
            return voltage;
        }

        public String getTemp() {
            return temp;
        }

        public String getAngle() {
            return angle;
        }

        @Override
        public String toString() {
            return "Bean{" +
                    "meterCumuUsage='" + meterCumuUsage + '\'' +
                    ", status0L='" + status0L + '\'' +
                    ", status1L='" + status1L + '\'' +
                    ", status2L='" + status2L + '\'' +
                    ", status3L='" + status3L + '\'' +
                    ", status4L='" + status4L + '\'' +
                    ", status5L='" + status5L + '\'' +
                    ", status6L='" + status6L + '\'' +
                    ", status7L='" + status7L + '\'' +
                    ", status0H='" + status0H + '\'' +
                    ", voltage='" + voltage + '\'' +
                    ", temp='" + temp + '\'' +
                    ", angle='" + angle + '\'' +
                    '}';
        }
    }

    public static byte[] string2Bytes(String str) {

        if (str == null || str.equals("")) {
            return null;
        }

        str = str.toUpperCase();
        int length = str.length() / 2;
        char[] strChar = str.toCharArray();
        byte[] bt = new byte[length];

        for (int i = 0; i < length; i++) {
            int index = i * 2;
            bt[i] = (byte) (char2Byte(strChar[index]) << 4 | char2Byte(strChar[index + 1]));
        }

        return bt;
    }

    private static byte char2Byte(char ch) {
        return (byte) "0123456789ABCDEF".indexOf(ch);
    }

    public final static long getLong(byte[] bt, boolean isAsc) {
        //BIG_ENDIAN
        if (bt == null) {
            throw new IllegalArgumentException("byte array is null.");
        }
        if (bt.length > 8) {
            throw new IllegalArgumentException("byte array size more than 8.");
        }
        long result = 0;
        if (isAsc)
            for (int i = bt.length - 1; i >= 0; i--) {
                result <<= 8;
                result |= (bt[i] & 0x00000000000000ff);
            }
        else
            for (int i = 0; i < bt.length; i++) {
                result <<= 8;
                result |= (bt[i] & 0x00000000000000ff);
            }
        return result;
    }

    public static void main(String[] args) {

        /*LzBleHelper.Receive helper = new LzBleHelper.Receive("7e310faa1b8300434410000002aa0f9110003b0d");
        helper.addFrame("7e320f0002c800000040004e010000095548ff0d");
        helper.addFrame("7e330155aaaaaaaaaaaaaaaaaaaaaaaaaaaaa10d");
        helper.setMeterAddress("10000002");
        System.out.println(helper.getBean().getMeterCumuUsage() + " V");
        System.out.println(helper.getBean().getVoltage() + " m³");*/

        //LzBleHelper.Receive helper = new LzBleHelper.Receive("7E1108AA04C2004F4B6055AAAAAAAAAAAAAA650D");
    	/*if (helper.getCmd() == RESPONSE_SUCCESS) {
			System.out.println("ok");
		}*/
        //helper.addFrame("7E320F001200000000010068010000FFFFFF790D");
        //helper.addFrame("7E3305FF4555ED55AAAAAAAAAAAAAAAAAAAA7F0D");
        //System.out.println(helper.getData());

        //helper.checkMiddle("10190012");

        /*
         * byte[] bt = string2Bytes("10190012");
         * System.out.println(Long.toHexString(getLong(bt, true)));
         */
        //Integer num = Integer.parseInt("a10", 16);
        //System.out.println(Integer.parseInt("4000", 16));
        //System.out.println(0x4000);
        //System.out.println(String.format("%08d", Integer.valueOf(Integer.toBinaryString(num))));
        //System.out.println("10190012".substring(7, 8));
        //System.out.println(new StringBuilder("a10").insert("a10".length() - 2, ".").toString());
        //System.out.println(helper.getBean().getVoltage());
        //System.out.println(Integer.toHexString(FILL_VALUE));
    	/*String a = "01234567891";
    	int listSize = a.length() / 5 + (a.length() % 5 == 0 ? 0 : 1);
    	for (int i = 0; i < listSize; i++) {
    		int endIndex = (i + 1) * 5 > a.length() ? a.length() : (i + 1) * 5;
			String dataTemp = a.substring(i * 5, endIndex);
			System.out.println(dataTemp);
    	}*/
    	/*LzBleHelper.Send send = new LzBleHelper.Send("10190012");
    	List<byte[]> result = send.signRead();
    	for (int i = 0; i < result.size(); i++) {
			System.out.println(HexUtil.formatHexString(result.get(i), true));
		}*/
        //System.out.println(String.format("%02x", 15));

    	/*LzBleHelper.Send send = new LzBleHelper.Send("10190012");
    	send.setHandheldId("70000002");
    	send.setRF(20);
    	send.setSetting();
    	send.setOperation();*/
    	/*LzBleHelper.Send send = new LzBleHelper.Send("100002");
    	send.setSendPower(20);*/

    }
}
