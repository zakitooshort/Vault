package com.example.vault.fake;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vault.crypto.VaultSession;

import javax.crypto.Cipher;

public class FakeVaultSession implements VaultSession {
    private final MutableLiveData<Boolean> unlocked = new MutableLiveData<>(false);
    private boolean hasVault;

    @Override
    public boolean hasVault() {
        return hasVault;
    }

    @Override
    public LiveData<Boolean> isUnlocked() {
        return unlocked;
    }

    @Override
    public void createVault(char[] masterPassword) {
        hasVault = true;
        unlocked.setValue(true);
    }

    @Override
    public boolean unlockWithPassword(char[] masterPassword) {
        unlocked.setValue(true);
        return true;
    }

    @Override
    public boolean unlockWithBiometrics(Cipher cipher) {
        unlocked.setValue(true);
        return true;
    }

    @Override
    public boolean changeMasterPassword(char[] oldPassword, char[] newPassword) {
        return true;
    }

    @Override
    public void lock() {
        unlocked.setValue(false);
    }
}
