package com.example.vault.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AndroidCryptoManager implements CryptoManager {
    public static final int SALT_SIZE_BYTES = 16;
    public static final int VAULT_KEY_SIZE_BITS = 256;
    public static final int GCM_IV_SIZE_BYTES = 12;
    public static final int GCM_TAG_SIZE_BITS = 128;
    public static final int DEFAULT_ITERATIONS = 600_000;

    private final SecureRandom secureRandom;

    public AndroidCryptoManager() {
        secureRandom = new SecureRandom();
    }

    @Override
    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_SIZE_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }

    @Override
    public SecretKey generateVaultKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(VAULT_KEY_SIZE_BITS, secureRandom);
            return generator.generateKey();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("AES key generation is unavailable", exception);
        }
    }

    @Override
    public SecretKey deriveWrappingKey(char[] password, byte[] salt, int iterations) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        if (salt == null || salt.length != SALT_SIZE_BYTES) {
            throw new IllegalArgumentException("Salt must contain 16 bytes");
        }
        if (iterations <= 0) {
            throw new IllegalArgumentException("Iterations must be positive");
        }

        PBEKeySpec specification = new PBEKeySpec(
                password, salt, iterations, VAULT_KEY_SIZE_BITS);
        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] encodedKey = factory.generateSecret(specification).getEncoded();
            return new SecretKeySpec(encodedKey, "AES");
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("PBKDF2 key derivation is unavailable", exception);
        } finally {
            specification.clearPassword();
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public byte[] encrypt(byte[] plain, SecretKey key) throws GeneralSecurityException {
        if (plain == null || key == null) {
            throw new IllegalArgumentException("Plaintext and key are required");
        }

        byte[] iv = new byte[GCM_IV_SIZE_BYTES];
        secureRandom.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_SIZE_BITS, iv));
        byte[] ciphertext = cipher.doFinal(plain);

        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }

    @Override
    public byte[] decrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
        if (data == null || key == null) {
            throw new IllegalArgumentException("Encrypted data and key are required");
        }
        if (data.length < GCM_IV_SIZE_BYTES + (GCM_TAG_SIZE_BITS / 8)) {
            throw new GeneralSecurityException("Encrypted data is too short");
        }

        byte[] iv = Arrays.copyOfRange(data, 0, GCM_IV_SIZE_BYTES);
        byte[] ciphertext = Arrays.copyOfRange(data, GCM_IV_SIZE_BYTES, data.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_SIZE_BITS, iv));
        return cipher.doFinal(ciphertext);
    }
}
