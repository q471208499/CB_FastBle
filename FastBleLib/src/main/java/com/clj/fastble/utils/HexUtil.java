package com.clj.fastble.utils;

import android.text.TextUtils;

import java.nio.ByteBuffer;

public class HexUtil {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        if (data == null)
            return null;
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    public static String formatHexString(byte[] data) {
        return formatHexString(data, false);
    }

    public static String formatHexString(byte[] data, boolean addSpace) {
        if (data == null || data.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
            if (addSpace)
                sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.trim();
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte[] hexStr2bytes(String hexStr) {
        if (TextUtils.isEmpty(hexStr)) {
            return null;
        }
        if (hexStr.length() % 2 != 0) {// 长度为单数
            hexStr = "0" + hexStr;// 前面补0
        }
        char[] chars = hexStr.toCharArray();
        int len = chars.length / 2;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            int x = i * 2;
            bytes[i] = (byte) Integer.parseInt(String.valueOf(new char[]{chars[x], chars[x + 1]}), 16);
        }
        return bytes;
    }

    public static byte[] hexStr2Byte(String hex) {
        ByteBuffer bf = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i++) {
            String hexStr = hex.charAt(i) + "";
            i++;
            hexStr += hex.charAt(i);
            byte b = (byte) Integer.parseInt(hexStr, 16);
            bf.put(b);
        }
        return bf.array();
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String extractData(byte[] data, int position) {
        return HexUtil.formatHexString(new byte[]{data[position]});
    }

    public static String intToHex(int n) {
        StringBuffer s = new StringBuffer();
        String a;
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        while (n != 0) {
            s = s.append(b[n % 16]);
            n = n / 16;
        }
        a = s.reverse().toString();
        return a;
    }

    public static byte[] intToByte(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);
        return b;
    }

    public static byte[] intToBytes(int a, int length) {
        byte[] bs = new byte[length];
        for (int i = bs.length - 1; i >= 0; i--) {
            bs[i] = (byte) (a % 255);
            a = a / 255;
        }
        return bs;
    }

    public static int byteToInt(byte b) {
        // System.out.println("byte 是:"+b);
        int x = b & 0xff;
        // System.out.println("int 是:"+x);
        return x;
    }

    /**
     * int到byte[] 由高位到低位
     *
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * byte[]转int
     *
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    public static byte[] hexToByte(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte) intVal);
        }
        return ret;
    }


    /**
     * @功能: BCD码转为10进制串(阿拉伯数据)
     * @参数: BCD码
     * @结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    public static byte[] r(String str) {
        return str2Bcd(Long.toHexString(getLong(string2Bytes(str), true)));
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

    /**
     * 大小端互换
     */
    public static String bigOrSmallEndian(String str) {
        if (str == null || str.isEmpty() || str.length() % 2 == 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() / 2; i > 0; i--) {
            sb.append(str.substring(i * 2 - 2, i * 2));
        }
        return sb.toString();
    }

    /**
     * 左边 / 右边 添加0
     *
     * @param str
     * @param strLength
     * @param isRight
     * @return
     */
    public static String fixStrAdd0ForLength(String str, int strLength, boolean isRight) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                if (isRight) {
                    sb.append(str).append("0");//右补0
                } else {
                    sb.append("0").append(str);// 左补0
                }
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * 不足补0，过多去掉
     *
     * @return
     */
    public static String add0AndRemove(String str, int strLength, boolean isRight) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() > strLength) {
            str = str.substring(0, strLength);
        }
        return fixStrAdd0ForLength(str, strLength, isRight);
    }

    /**
     * 获取校验合
     *
     * @param bytes
     * @return
     */
    public static byte getCS(byte[] bytes) {
        int toBeSum = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            toBeSum += bytes[i];
        }
        int sumDec = toBeSum % 256;
        return (byte) sumDec;
    }

    /**
     * hex 计算校验合
     *
     * @param toCsHexStr
     * @return
     */
    public static byte getCSByStr(String toCsHexStr) {
        if (toCsHexStr == null || toCsHexStr.length() % 2 == 1) {
            throw new RuntimeException(toCsHexStr + " 不符合规则");
        }
        int cs = 0;
        for (int i = 0; i < toCsHexStr.length() / 2; i++) {
            cs += Integer.parseInt(toCsHexStr.substring(2 * i, 2 + 2 * i), 16);
        }
        cs = cs % 256;
        return (byte) cs;
    }

    /**
     * dex 转 hex, 不足补0，然后大小端互换
     *
     * @param num
     * @param length
     * @return
     */
    public static byte[] dealInt(int num, int length) {
        String numberHex = Integer.toHexString(num);
        byte[] bytes = str2Bcd(numberHex);
        String str = formatHexString(bytes);
        return dealStr(str, length, false);
    }

    public static byte[] dealLong(long num, int length) {
        String numberHex = Long.toHexString(num);
        byte[] bytes = str2Bcd(numberHex);
        String str = formatHexString(bytes);
        return dealStr(str, length, false);
    }

    /**
     * 不足补0，然后大小端互换
     *
     * @param str
     * @param strLength
     * @param isRight
     * @return
     */
    public static byte[] dealStr(String str, int strLength, boolean isRight) {
        str = add0AndRemove(str, strLength, isRight);
        str = bigOrSmallEndian(str);
        return str2Bcd(str);
    }

    public static void main(String[] args) {
        System.out.println(HexUtil.formatHexString(dealInt(2, 4)));
    }
}
