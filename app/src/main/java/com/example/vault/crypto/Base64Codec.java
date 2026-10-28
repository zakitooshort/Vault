package com.example.vault.crypto;

import android.util.Base64;

final class Base64Codec {
    private Base64Codec() {
    }

    static String encode(byte[] value) {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }

    static byte[] decode(String value) {
        return Base64.decode(value, Base64.NO_WRAP);
    }
}
