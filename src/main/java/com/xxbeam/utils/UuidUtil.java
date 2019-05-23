package com.xxbeam.utils;

import java.util.UUID;

public class UuidUtil {

    public static String getUuid(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String uuidStr = str.replace("-", "");
        return uuidStr;
    }
}
