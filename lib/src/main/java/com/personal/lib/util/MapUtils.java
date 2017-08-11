package com.personal.lib.util;

import java.util.Map;

public class MapUtils {

    private MapUtils() {}

    public static boolean isEmpty(Map map) {
        return null == map || map.isEmpty();
    }

}
