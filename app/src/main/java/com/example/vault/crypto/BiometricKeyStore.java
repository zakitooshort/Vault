package com.example.vault.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public interface BiometricKeyStore {
    boolean isAvailable();
    void enable(SecretKey vaultKey);
    void disable();
    Cipher cipherForUnlock();
}
