package com.example.vault.crypto;

import androidx.lifecycle.LiveData;

import javax.crypto.Cipher;

public interface VaultSession {
    LiveData<Boolean> isUnlocked();
    void createVault(char[] masterPassword);
    boolean unlockWithPassword(char[] masterPassword);
    boolean unlockWithBiometrics(Cipher cipher);
    boolean changeMasterPassword(char[] oldPassword, char[] newPassword);
    void lock();
}
