package com.example.vault;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.vault.crypto.AndroidCryptoManager;
import com.example.vault.crypto.DefaultVaultSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DefaultVaultSessionInstrumentedTest {
    private DefaultVaultSession session;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        session = new DefaultVaultSession(
                new AndroidCryptoManager(),
                context.getSharedPreferences("crypto-test", Context.MODE_PRIVATE));
        context.getSharedPreferences("crypto-test", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void createsLocksAndUnlocksVault() {
        session.createVault("correct".toCharArray());
        waitForLiveData();
        assertTrue(session.isUnlocked().getValue());

        session.lock();
        waitForLiveData();
        assertFalse(session.isUnlocked().getValue());
        assertTrue(session.unlockWithPassword("correct".toCharArray()));
        waitForLiveData();
        assertTrue(session.isUnlocked().getValue());
    }

    @Test
    public void wrongPasswordDoesNotUnlockVault() {
        session.createVault("correct".toCharArray());
        session.lock();

        assertFalse(session.unlockWithPassword("wrong".toCharArray()));
        waitForLiveData();
        assertFalse(session.isUnlocked().getValue());
    }

    @Test
    public void changingPasswordKeepsVaultUnlockable() {
        session.createVault("old-password".toCharArray());
        session.lock();

        assertTrue(session.changeMasterPassword(
                "old-password".toCharArray(), "new-password".toCharArray()));
        session.lock();
        assertFalse(session.unlockWithPassword("old-password".toCharArray()));
        assertTrue(session.unlockWithPassword("new-password".toCharArray()));
    }

    private void waitForLiveData() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
}
