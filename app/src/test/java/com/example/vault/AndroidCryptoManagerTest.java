package com.example.vault;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import com.example.vault.crypto.AndroidCryptoManager;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.SecretKey;

public class AndroidCryptoManagerTest {
    @Test
    public void encryptThenDecryptReturnsOriginalPlaintext() throws Exception {
        AndroidCryptoManager manager = new AndroidCryptoManager();
        SecretKey key = manager.generateVaultKey();
        byte[] plaintext = "secret entry".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = manager.encrypt(plaintext, key);

        assertArrayEquals(plaintext, manager.decrypt(encrypted, key));
    }

    @Test
    public void encryptionUsesDifferentIvEachTime() throws Exception {
        AndroidCryptoManager manager = new AndroidCryptoManager();
        SecretKey key = manager.generateVaultKey();
        byte[] plaintext = "same plaintext".getBytes(StandardCharsets.UTF_8);

        byte[] first = manager.encrypt(plaintext, key);
        byte[] second = manager.encrypt(plaintext, key);

        assertFalse(Arrays.equals(first, second));
    }

    @Test
    public void tamperedCiphertextFailsAuthentication() throws Exception {
        AndroidCryptoManager manager = new AndroidCryptoManager();
        SecretKey key = manager.generateVaultKey();
        byte[] encrypted = manager.encrypt("secret".getBytes(StandardCharsets.UTF_8), key);
        encrypted[encrypted.length - 1] ^= 1;

        assertThrows(GeneralSecurityException.class, () -> manager.decrypt(encrypted, key));
    }

    @Test
    public void passwordDerivationIsDeterministicForSameInputs() {
        AndroidCryptoManager manager = new AndroidCryptoManager();
        byte[] salt = manager.generateSalt();

        SecretKey first = manager.deriveWrappingKey("master".toCharArray(), salt,
                AndroidCryptoManager.DEFAULT_ITERATIONS);
        SecretKey second = manager.deriveWrappingKey("master".toCharArray(), salt,
                AndroidCryptoManager.DEFAULT_ITERATIONS);

        assertArrayEquals(first.getEncoded(), second.getEncoded());
    }

    @Test
    public void saltsAreRandom() {
        AndroidCryptoManager manager = new AndroidCryptoManager();

        assertNotEquals(
                Arrays.toString(manager.generateSalt()),
                Arrays.toString(manager.generateSalt()));
    }
}
