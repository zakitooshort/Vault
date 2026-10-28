package com.example.vault.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.vault.crypto.AndroidCryptoManager;
import com.example.vault.crypto.DefaultVaultSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class RoomVaultRepositoryInstrumentedTest {
    private AppDatabase database;
    private DefaultVaultSession vaultSession;
    private RoomVaultRepository repository;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        AndroidCryptoManager cryptoManager = new AndroidCryptoManager();
        vaultSession = new DefaultVaultSession(
                cryptoManager,
                context.getSharedPreferences("repo-test", Context.MODE_PRIVATE));
        context.getSharedPreferences("repo-test", Context.MODE_PRIVATE)
                .edit().clear().commit();

        repository = new RoomVaultRepository(
                database.vaultEntryDao(), vaultSession, cryptoManager);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void encryptsAndDecryptsVaultEntry() throws Exception {
        char[] masterPassword = "master-password-123".toCharArray();
        vaultSession.createVault(masterPassword);

        VaultEntry original = new VaultEntry(
                "entry-1",
                "University Portal",
                "student123",
                "secretP@ssword!",
                "https://portal.university.edu",
                "Important student credentials",
                "default",
                true,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        repository.addEntry(original);

        VaultEntryEntity rawEntity = database.vaultEntryDao().getById("entry-1");
        assertNotNull(rawEntity);
        assertNotNull(rawEntity.encryptedPayload);
        assertTrue(rawEntity.encryptedPayload.length > 0);

        AtomicReference<VaultEntry> resultRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        repository.getEntry("entry-1", entry -> {
            resultRef.set(entry);
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        VaultEntry retrieved = resultRef.get();
        assertNotNull(retrieved);
        assertEquals("University Portal", retrieved.getTitle());
        assertEquals("student123", retrieved.getUsername());
        assertEquals("secretP@ssword!", retrieved.getPassword());
        assertEquals("https://portal.university.edu", retrieved.getUrl());
        assertEquals("Important student credentials", retrieved.getNotes());
    }

    @Test
    public void cannotDecryptEntryWhenVaultIsLocked() throws Exception {
        char[] masterPassword = "master-password-123".toCharArray();
        vaultSession.createVault(masterPassword);

        VaultEntry original = new VaultEntry(
                "entry-2",
                "Email Account",
                "user@example.com",
                "myEmailPass!",
                "https://mail.com",
                "Notes",
                "default",
                false,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        repository.addEntry(original);
        vaultSession.lock();

        AtomicReference<VaultEntry> resultRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        repository.getEntry("entry-2", entry -> {
            resultRef.set(entry);
            latch.countDown();
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNull(resultRef.get());
    }
}
