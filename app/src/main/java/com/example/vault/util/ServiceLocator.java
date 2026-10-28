package com.example.vault.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.vault.crypto.AndroidCryptoManager;
import com.example.vault.crypto.AndroidKeystoreBiometricKeyStore;
import com.example.vault.crypto.BiometricKeyStore;
import com.example.vault.crypto.DefaultVaultSession;
import com.example.vault.crypto.VaultSession;
import com.example.vault.data.AppDatabase;
import com.example.vault.data.RoomVaultRepository;
import com.example.vault.data.VaultRepository;
import com.example.vault.fake.FakeBiometricKeyStore;
import com.example.vault.fake.FakeVaultRepository;
import com.example.vault.fake.FakeVaultSession;

public final class ServiceLocator {
    private static volatile ServiceLocator INSTANCE;

    private final VaultRepository vaultRepository;
    private final VaultSession vaultSession;
    private final BiometricKeyStore biometricKeyStore;
    private final PasswordStrengthAnalyzer strengthAnalyzer;

    private ServiceLocator(Context context) {
        if (context != null) {
            Context appContext = context.getApplicationContext();
            SharedPreferences preferences = appContext.getSharedPreferences(
                    "vault_preferences", Context.MODE_PRIVATE);

            AndroidCryptoManager cryptoManager = new AndroidCryptoManager();
            DefaultVaultSession defaultSession = new DefaultVaultSession(
                    cryptoManager, preferences);

            AppDatabase database = AppDatabase.getInstance(appContext);

            this.vaultSession = defaultSession;
            this.biometricKeyStore = new AndroidKeystoreBiometricKeyStore(preferences);
            this.vaultRepository = new RoomVaultRepository(
                    database.vaultEntryDao(), defaultSession, cryptoManager);
            this.strengthAnalyzer = new com.example.vault.fake.RuleBasedStrengthAnalyzer();
        } else {
            this.vaultRepository = new FakeVaultRepository();
            this.vaultSession = new FakeVaultSession();
            this.biometricKeyStore = new FakeBiometricKeyStore();
            this.strengthAnalyzer = new com.example.vault.fake.RuleBasedStrengthAnalyzer();
        }
    }

    public static void init(Context context) {
        if (INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocator(context);
                }
            }
        }
    }

    public static ServiceLocator getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceLocator.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceLocator(null);
                }
            }
        }
        return INSTANCE;
    }

    public VaultRepository getVaultRepository() { return vaultRepository; }
    public VaultSession getVaultSession() { return vaultSession; }
    public BiometricKeyStore getBiometricKeyStore() { return biometricKeyStore; }
    public PasswordStrengthAnalyzer getStrengthAnalyzer() { return strengthAnalyzer; }
}
