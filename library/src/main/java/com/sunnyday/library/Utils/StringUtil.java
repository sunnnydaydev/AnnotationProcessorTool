package com.sunnyday.library.Utils;

/**
 * Created by zb on 2020/6/12 16:10
 */
public class StringUtil {
    /**
     * 字符串的首字母小写
     * @param targetStr 目标字符串
     * */
    public static String toLowerCaseFirstChar(String targetStr) {
        String temp = targetStr;
        if (null == temp || "".equals(temp)) {
            temp = "";
        }
        String first = temp.substring(0,1).toLowerCase();
        temp= first+temp.substring(1);
        return temp;
    }
}
