package com.sohrab.obd.reader.util;

public class StringUtils {

    public static String capitalizeFirstCharacter(String str) {
        if (str == null) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}
