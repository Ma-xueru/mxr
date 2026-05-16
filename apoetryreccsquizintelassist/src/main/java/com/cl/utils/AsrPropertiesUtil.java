package com.cl.utils;

import java.util.Properties;

public class AsrPropertiesUtil {
    private static Properties props = new Properties();
    static {
        try { props.load(AsrPropertiesUtil.class.getClassLoader().getResourceAsStream("asr.properties")); }
        catch (Exception e) { e.printStackTrace(); }
    }
    public static String get(String key, String def) { return props.getProperty(key, def); }
}
