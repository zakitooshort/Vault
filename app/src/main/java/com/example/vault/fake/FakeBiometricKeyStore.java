package com.example.vault.fake;

import com.example.vault.crypto.BiometricKeyStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class FakeBiometricKeyStore implements BiometricKeyStore {
    private boolean enabled;

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void enable(SecretKey vaultKey) {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public Cipher cipherForUnlock() {
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
