package com.billingplatformapplication.shared.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static String normalize(String v) {
        return v == null ? null : v.trim().toLowerCase();
    }

    public static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    public static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String visible = local.length() > 2 ? local.substring(0, 2) + "***" : "***";
        return visible + "@" + parts[1];
    }
}