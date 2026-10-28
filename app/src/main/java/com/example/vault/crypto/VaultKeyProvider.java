package com.example.vault.crypto;

public interface VaultKeyProvider {
    <T> T withVaultKey(VaultKeyOperation<T> operation) throws VaultLockedException;
}
