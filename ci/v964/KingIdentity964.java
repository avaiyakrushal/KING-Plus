package com.kingplus.social;

/**
 * Distinguishes the six-digit legacy display code from the exact Firebase UID.
 * A short code is not an authorization token and is NOT guaranteed globally unique.
 */
public final class KingIdentity964 {
    private KingIdentity964() {}
    public static boolean sixDigits(String value) {
        return value != null && value.matches("[0-9]{6}");
    }
    public static boolean firebaseUid(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{18,128}");
    }
    public static boolean joinCode(String value) { return sixDigits(value); }
}
