package com.example.vault.crypto;

import javax.crypto.SecretKey;

@FunctionalInterface
public interface VaultKeyOperation<T> {
    T execute(SecretKey vaultKey) throws Exception;
}
