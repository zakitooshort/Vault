package com.example.vault.crypto;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AndroidKeystoreBiometricKeyStore implements BiometricKeyStore {
    static final String PREF_WRAPPED_VAULT_KEY = "biometric.wrapped_vault_key";
    static final String PREF_VAULT_KEY_IV = "biometric.vault_key_iv";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "vault_biometric_key";

    private final SharedPreferences preferences;

    public AndroidKeystoreBiometricKeyStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean isAvailable() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
            return keyStore.containsAlias(KEY_ALIAS) || createKey() != null;
        } catch (GeneralSecurityException | java.io.IOException exception) {
            return false;
        }
    }

    @Override
    public void enable(SecretKey vaultKey) {
        if (vaultKey == null) {
            throw new IllegalArgumentException("Vault key is required");
        }
        try {
            createKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] wrappedKey = cipher.doFinal(vaultKey.getEncoded());
            boolean persisted = preferences.edit()
                    .putString(PREF_WRAPPED_VAULT_KEY, Base64Codec.encode(wrappedKey))
                    .putString(PREF_VAULT_KEY_IV, Base64Codec.encode(cipher.getIV()))
                    .commit();
            if (!persisted) {
                throw new IllegalStateException("Biometric key data could not be stored");
            }
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Biometric key setup failed", exception);
        }
    }

    @Override
    public void disable() {
        preferences.edit()
                .remove(PREF_WRAPPED_VAULT_KEY)
                .remove(PREF_VAULT_KEY_IV)
                .apply();
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS);
            }
        } catch (GeneralSecurityException | java.io.IOException exception) {
            throw new IllegalStateException("Biometric key removal failed", exception);
        }
    }

    @Override
    public Cipher cipherForUnlock() {
        try {
            String wrappedKey = preferences.getString(PREF_WRAPPED_VAULT_KEY, null);
            if (wrappedKey == null) {
                return null;
            }
            String encodedIv = preferences.getString(PREF_VAULT_KEY_IV, null);
            if (encodedIv == null) {
                throw new GeneralSecurityException("Biometric key data is invalid");
            }
            byte[] iv = Base64Codec.decode(encodedIv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(),
                    new javax.crypto.spec.GCMParameterSpec(
                            AndroidCryptoManager.GCM_TAG_SIZE_BITS,
                            iv));
            return cipher;
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Biometric unlock setup failed", exception);
        }
    }

    private SecretKey createKey() throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(0)
                .setInvalidatedByBiometricEnrollment(true)
                .build());
        return generator.generateKey();
    }

    private SecretKey getKey() throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);
            java.security.Key key = keyStore.getKey(KEY_ALIAS, null);
            if (!(key instanceof SecretKey)) {
                throw new GeneralSecurityException("Biometric key is unavailable");
            }
            return (SecretKey) key;
        } catch (java.io.IOException exception) {
            throw new GeneralSecurityException("Biometric keystore is unavailable", exception);
        }
    }
}
