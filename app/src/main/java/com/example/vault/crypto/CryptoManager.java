package com.example.vault.crypto;

import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;

public interface CryptoManager {
    byte[] generateSalt();
    SecretKey generateVaultKey();
    SecretKey deriveWrappingKey(char[] password, byte[] salt, int iterations);
    byte[] encrypt(byte[] plain, SecretKey key) throws GeneralSecurityException;
    byte[] decrypt(byte[] data, SecretKey key) throws GeneralSecurityException;
}
