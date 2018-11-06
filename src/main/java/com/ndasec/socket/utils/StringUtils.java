package com.ndasec.socket.utils;

import java.util.List;
import java.util.Map;

/**
 * 模板字符串工具类
 */
public class StringUtils {


    /**
     * 字符串定长处理
     * @param src
     * @param count
     * @param padChar
     * @return
     */
    public static String padString(String src, int count, char padChar) {
        String result = src;
        if (result == null) {
            result = "";
        }
        int length = result.length();
        if (length < count) {
            for (int i = 0; i < count - length; i++) {
                result = padChar + result;
            }
        }
        return result;
    }

    /**
     * 判断数组或者对象中是否存在指定的值
     *
     * @param object
     * @param value
     * @return
     */
    public static boolean include(Object object, String value) {
        String[] array = null;
        if (object instanceof String[]) {
            array = (String[]) object;
        } else if (object instanceof String) {
            array = new String[] {object.toString()};
        }
        if (array == null || array.length <= 0) {
            return false;
        }
        if (!notBlank(value)) {
            return false;
        }
        for (String v : array) {
            if (value.equals(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断非空
     *
     * @param obj
     * @return
     */
    public static boolean notBlank(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass().isArray()) {
            return  ((Object[]) obj).length>0;
        } else if (obj instanceof List) {
            return ((List) obj).size() > 0;
        } else if (obj instanceof Map) {
            return ((Map) obj).size() > 0;
        }
        String str = obj.toString();
        str = str.trim();
        if (str.length() == 0) {
            return false;
        }
        return true;
    }

    /**
     * not empty Array
     *
     * @param objs
     * @return
     */
    public static boolean notBlankArray(Object [] objs){
        if (objs==null||objs.length<=0) {
            return false;
        }
        boolean existNoBlank = false;
        for (Object obj : objs) {
            if(obj instanceof String) {
                if (obj!=null&&obj.toString().trim().length()>0) {
                    existNoBlank = true;
                    break;
                }
            } else {
                if (obj!=null){
                    existNoBlank = true;
                    break;
                }
            }
        }
        return existNoBlank;
    }

    /**
     * 连接数组为字符串
     *
     * @param objects
     * @param joinSplit
     * @return
     */
    public static String join(Object objects, String joinSplit) {
        Object[] objs = null;
        if (objects.getClass().isArray()) {
            objs = (Object[]) objects;
        } else {
            objs = new Object[]{objects};
        }
        return join(objs, joinSplit, null, null);
    }

    /**
     * 连接数组为字符串
     *
     * @param objects
     * @param joinSplit
     * @param startFlag
     * @param endFlag
     * @return
     */
    public static String join(Object objects, String joinSplit, String startFlag, String endFlag) {
        Object[] objs = null;
        if (objects.getClass().isArray()) {
            objs = (Object[]) objects;
        } else {
            objs = new Object[]{objects};
        }
        startFlag = notBlank(startFlag) ? startFlag : "";
        endFlag = notBlank(endFlag) ? endFlag : "";
        joinSplit = notBlank(joinSplit) ? joinSplit : " ";
        StringBuilder stringBuilder = new StringBuilder();
        if (objs.length > 0) {
            boolean isFirst = true;
            for (int i = 0; i < objs.length; i++) {
                Object obj = objs[i];
                if (obj != null && obj.toString().trim().length() > 0) {
                    if (!isFirst) {
                        stringBuilder.append(joinSplit);
                    } else {
                        isFirst = false;
                    }
                    stringBuilder.append(startFlag).append(obj.toString()).append(endFlag);
                }
            }
        }
        return stringBuilder.toString();
    }


}
