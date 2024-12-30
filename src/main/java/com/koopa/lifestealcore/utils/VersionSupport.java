package com.koopa.lifestealcore.utils;

import org.bukkit.Bukkit;

public class VersionSupport {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final int MAJOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);

    public static boolean isLegacy() {
        return MAJOR_VERSION < 13;
    }

    public static boolean hasHexColors() {
        return MAJOR_VERSION >= 16;
    }

    public static boolean hasOffhand() {
        return MAJOR_VERSION >= 9;
    }

    public static boolean hasNewMaterials() {
        return MAJOR_VERSION >= 13;
    }

    public static String getServerVersion() {
        return VERSION;
    }

    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public static boolean isSupported() {
        return MAJOR_VERSION >= 13; // Plugin supports 1.13+
    }
} 