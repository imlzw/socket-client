package com.ndasec.socket.utils;

import java.util.Stack;

public class NumericalUtil {


    /***
     * 将10进制转换为任意进制
     * @param intVal
     * @param destHex 目标进制大小
     * @return
     */
    public static String int2OtherHex(long intVal, int destHex) {
        StringBuilder sb = new StringBuilder();
        Stack<String> s = new Stack<String>();
        while (true) {
            s.push((char)((int)(intVal % destHex)+48) + "");
            intVal /= destHex;
            if (intVal == 0) {
                break;
            }
        }
        while (!s.empty()) {
            sb.append(s.pop());
        }
        return sb.toString();
    }

    /***
     *  任何进制转换,
     * @param srcHexString 待转换的进制字符串
     * @param srcHex 源进制大小
     * @param destHex 目标进制大小
     * @return
     */
    public static String BaseConvert(String srcHexString, int srcHex, int destHex) {
        if (srcHex == destHex) {
            return srcHexString;
        }
        char[] chars = srcHexString.toCharArray();
        int len = chars.length;
        if (destHex != 10) {//目标进制不是十进制 先转化为十进制
            srcHexString = BaseConvert(srcHexString, srcHex, 10);
        } else {
            long n = 0;
            for (int i = len - 1; i >= 0; i--) {
                n += ((int)chars[i]-48) * Math.pow(srcHex, len - i - 1);
            }
            return String.valueOf(n);
        }
        return int2OtherHex(Integer.valueOf(srcHexString), destHex);
    }

    public static void main(String[] args) {
        System.out.println((int)'0');
    }

}