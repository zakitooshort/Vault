package com.example.vault.crypto;

public class VaultLockedException extends Exception {
    public VaultLockedException() {
        super("Vault is currently locked. Vault key is unavailable.");
    }

    public VaultLockedException(String message) {
        super(message);
    }
}
