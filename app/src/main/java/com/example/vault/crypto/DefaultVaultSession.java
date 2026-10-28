package com.example.vault.crypto;

import android.content.SharedPreferences;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DefaultVaultSession implements VaultSession, VaultKeyProvider {
    static final String PREF_SALT = "vault.salt";
    static final String PREF_ITERATIONS = "vault.iterations";
    static final String PREF_WRAPPED_KEY = "vault.wrapped_key";

    private final AndroidCryptoManager cryptoManager;
    private final SharedPreferences preferences;
    private final MutableLiveData<Boolean> unlocked = new MutableLiveData<>(false);
    private byte[] vaultKeyBytes;

    public DefaultVaultSession(
            AndroidCryptoManager cryptoManager,
            SharedPreferences preferences) {
        this.cryptoManager = cryptoManager;
        this.preferences = preferences;
    }

    @Override
    public LiveData<Boolean> isUnlocked() {
        return unlocked;
    }

    @Override
    public synchronized void createVault(char[] masterPassword) {
        requirePassword(masterPassword);
        SecretKey vaultKey = cryptoManager.generateVaultKey();
        byte[] salt = cryptoManager.generateSalt();
        byte[] vaultKeyBytes = vaultKey.getEncoded();
        try {
            SecretKey wrappingKey = cryptoManager.deriveWrappingKey(
                    masterPassword, salt, AndroidCryptoManager.DEFAULT_ITERATIONS);
            byte[] wrappedKey = cryptoManager.encrypt(vaultKeyBytes, wrappingKey);
            boolean persisted = preferences.edit()
                    .putString(PREF_SALT, Base64Codec.encode(salt))
                    .putInt(PREF_ITERATIONS, AndroidCryptoManager.DEFAULT_ITERATIONS)
                    .putString(PREF_WRAPPED_KEY, Base64Codec.encode(wrappedKey))
                    .commit();
            if (!persisted) {
                throw new IllegalStateException("Vault metadata could not be stored");
            }
            setVaultKey(vaultKeyBytes);
        } catch (GeneralSecurityException exception) {
            Arrays.fill(vaultKeyBytes, (byte) 0);
            throw new IllegalStateException("Vault creation failed", exception);
        } finally {
            Arrays.fill(salt, (byte) 0);
        }
    }

    @Override
    public synchronized boolean unlockWithPassword(char[] masterPassword) {
        requirePassword(masterPassword);
        byte[] salt = readRequiredBytes(PREF_SALT);
        byte[] wrappedKey = readRequiredBytes(PREF_WRAPPED_KEY);
        int iterations = preferences.getInt(
                PREF_ITERATIONS, AndroidCryptoManager.DEFAULT_ITERATIONS);
        try {
            SecretKey wrappingKey = cryptoManager.deriveWrappingKey(
                    masterPassword, salt, iterations);
            byte[] decryptedKey = cryptoManager.decrypt(wrappedKey, wrappingKey);
            if (decryptedKey.length != AndroidCryptoManager.VAULT_KEY_SIZE_BITS / 8) {
                Arrays.fill(decryptedKey, (byte) 0);
                return false;
            }
            setVaultKey(decryptedKey);
            Arrays.fill(decryptedKey, (byte) 0);
            return true;
        } catch (GeneralSecurityException exception) {
            return false;
        } finally {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(wrappedKey, (byte) 0);
        }
    }

    @Override
    public synchronized boolean unlockWithBiometrics(Cipher cipher) {
        if (cipher == null) {
            return false;
        }
        byte[] wrappedKey = readOptionalBytes(
                AndroidKeystoreBiometricKeyStore.PREF_WRAPPED_VAULT_KEY);
        if (wrappedKey == null) {
            return false;
        }
        try {
            byte[] decryptedKey = cipher.doFinal(wrappedKey);
            if (decryptedKey.length != AndroidCryptoManager.VAULT_KEY_SIZE_BITS / 8) {
                Arrays.fill(decryptedKey, (byte) 0);
                return false;
            }
            setVaultKey(decryptedKey);
            Arrays.fill(decryptedKey, (byte) 0);
            return true;
        } catch (GeneralSecurityException exception) {
            return false;
        } finally {
            Arrays.fill(wrappedKey, (byte) 0);
        }
    }

    @Override
    public synchronized boolean changeMasterPassword(
            char[] oldPassword, char[] newPassword) {
        requirePassword(oldPassword);
        requirePassword(newPassword);
        if (!unlockWithPassword(oldPassword) || vaultKeyBytes == null) {
            return false;
        }

        byte[] salt = cryptoManager.generateSalt();
        byte[] currentVaultKey = Arrays.copyOf(vaultKeyBytes, vaultKeyBytes.length);
        try {
            SecretKey wrappingKey = cryptoManager.deriveWrappingKey(
                    newPassword, salt, AndroidCryptoManager.DEFAULT_ITERATIONS);
            byte[] wrappedKey = cryptoManager.encrypt(currentVaultKey, wrappingKey);
            boolean persisted = preferences.edit()
                    .putString(PREF_SALT, Base64Codec.encode(salt))
                    .putInt(PREF_ITERATIONS, AndroidCryptoManager.DEFAULT_ITERATIONS)
                    .putString(PREF_WRAPPED_KEY, Base64Codec.encode(wrappedKey))
                    .commit();
            return persisted;
        } catch (GeneralSecurityException exception) {
            return false;
        } finally {
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(currentVaultKey, (byte) 0);
        }
    }

    @Override
    public synchronized void lock() {
        if (vaultKeyBytes != null) {
            Arrays.fill(vaultKeyBytes, (byte) 0);
            vaultKeyBytes = null;
        }
        publishUnlocked(false);
    }

    @Override
    public synchronized <T> T withVaultKey(VaultKeyOperation<T> operation) throws VaultLockedException {
        if (vaultKeyBytes == null) {
            throw new VaultLockedException();
        }
        SecretKey key = new SecretKeySpec(vaultKeyBytes, "AES");
        try {
            return operation.execute(key);
        } catch (RuntimeException runtimeException) {
            throw runtimeException;
        } catch (Exception exception) {
            throw new IllegalStateException("Vault key operation failed", exception);
        }
    }

    SecretKey currentVaultKey() {
        if (vaultKeyBytes == null) {
            return null;
        }
        return new SecretKeySpec(vaultKeyBytes, "AES");
    }

    private void setVaultKey(byte[] keyBytes) {
        if (vaultKeyBytes != null) {
            Arrays.fill(vaultKeyBytes, (byte) 0);
        }
        vaultKeyBytes = Arrays.copyOf(keyBytes, keyBytes.length);
        publishUnlocked(true);
    }

    private void publishUnlocked(boolean value) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            unlocked.setValue(value);
        } else {
            unlocked.postValue(value);
        }
    }

    private byte[] readRequiredBytes(String key) {
        byte[] value = readOptionalBytes(key);
        if (value == null) {
            throw new IllegalStateException("Vault has not been created");
        }
        return value;
    }

    private byte[] readOptionalBytes(String key) {
        String encoded = preferences.getString(key, null);
        return encoded == null ? null : Base64Codec.decode(encoded);
    }

    private static void requirePassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password must not be empty");
        }
    }
}
