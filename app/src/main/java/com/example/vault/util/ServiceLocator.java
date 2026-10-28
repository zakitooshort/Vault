package com.example.vault.util;

import com.example.vault.crypto.BiometricKeyStore;
import com.example.vault.crypto.VaultSession;
import com.example.vault.data.VaultRepository;
import com.example.vault.fake.FakeBiometricKeyStore;
import com.example.vault.fake.FakeVaultRepository;
import com.example.vault.fake.FakeVaultSession;

public final class ServiceLocator {
    private static final ServiceLocator INSTANCE = new ServiceLocator();

    private final VaultRepository vaultRepository = new FakeVaultRepository();
    private final VaultSession vaultSession = new FakeVaultSession();
    private final BiometricKeyStore biometricKeyStore = new FakeBiometricKeyStore();
    private final PasswordStrengthAnalyzer strengthAnalyzer = new com.example.vault.fake.RuleBasedStrengthAnalyzer();

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        return INSTANCE;
    }

    public VaultRepository getVaultRepository() { return vaultRepository; }
    public VaultSession getVaultSession() { return vaultSession; }
    public BiometricKeyStore getBiometricKeyStore() { return biometricKeyStore; }
    public PasswordStrengthAnalyzer getStrengthAnalyzer() { return strengthAnalyzer; }
}
