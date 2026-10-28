package com.example.vault.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.vault.crypto.CryptoManager;
import com.example.vault.crypto.VaultKeyProvider;
import com.example.vault.crypto.VaultLockedException;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

public class RoomVaultRepository implements VaultRepository {
    private final VaultEntryDao vaultEntryDao;
    private final VaultKeyProvider keyProvider;
    private final CryptoManager cryptoManager;
    private final Executor executor;

    public RoomVaultRepository(
            VaultEntryDao vaultEntryDao,
            VaultKeyProvider keyProvider,
            CryptoManager cryptoManager) {
        this(vaultEntryDao, keyProvider, cryptoManager, Executors.newSingleThreadExecutor());
    }

    public RoomVaultRepository(
            VaultEntryDao vaultEntryDao,
            VaultKeyProvider keyProvider,
            CryptoManager cryptoManager,
            Executor executor) {
        this.vaultEntryDao = vaultEntryDao;
        this.keyProvider = keyProvider;
        this.cryptoManager = cryptoManager;
        this.executor = executor;
    }

    @Override
    public LiveData<List<VaultEntry>> observeEntries() {
        return Transformations.map(vaultEntryDao.observeAll(), this::decryptEntities);
    }

    @Override
    public void getEntry(String id, Callback<VaultEntry> callback) {
        executor.execute(() -> {
            try {
                VaultEntryEntity entity = vaultEntryDao.getById(id);
                if (entity == null) {
                    callback.onResult(null);
                    return;
                }
                VaultEntry entry = decryptEntity(entity);
                callback.onResult(entry);
            } catch (Exception exception) {
                callback.onResult(null);
            }
        });
    }

    @Override
    public void addEntry(VaultEntry entry) {
        executor.execute(() -> {
            try {
                VaultEntryEntity entity = encryptEntry(entry);
                vaultEntryDao.insert(entity);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to add entry", exception);
            }
        });
    }

    @Override
    public void updateEntry(VaultEntry entry) {
        addEntry(entry);
    }

    @Override
    public void deleteEntry(String id) {
        executor.execute(() -> vaultEntryDao.delete(id));
    }

    @Override
    public LiveData<List<Category>> observeCategories() {
        androidx.lifecycle.MutableLiveData<List<Category>> categories = new androidx.lifecycle.MutableLiveData<>();
        categories.postValue(Collections.singletonList(
                new Category("default", "General")
        ));
        return categories;
    }

    private List<VaultEntry> decryptEntities(List<VaultEntryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return keyProvider.withVaultKey(vaultKey -> {
                List<VaultEntry> result = new ArrayList<>();
                for (VaultEntryEntity entity : entities) {
                    VaultEntry entry = decryptEntityWithKey(entity, vaultKey);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
                return result;
            });
        } catch (VaultLockedException exception) {
            return Collections.emptyList();
        }
    }

    private VaultEntry decryptEntity(VaultEntryEntity entity) throws VaultLockedException {
        return keyProvider.withVaultKey(vaultKey -> decryptEntityWithKey(entity, vaultKey));
    }

    private VaultEntry decryptEntityWithKey(VaultEntryEntity entity, SecretKey vaultKey) {
        try {
            byte[] decryptedPayload = cryptoManager.decrypt(entity.encryptedPayload, vaultKey);
            String jsonString = new String(decryptedPayload, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonString);

            String title = json.optString("title", "");
            String username = json.optString("username", "");
            String password = json.optString("password", "");
            String url = json.optString("url", "");
            String notes = json.optString("notes", "");

            return new VaultEntry(
                    entity.id,
                    title,
                    username,
                    password,
                    url,
                    notes,
                    entity.categoryId,
                    entity.favorite,
                    entity.createdAt,
                    entity.updatedAt
            );
        } catch (Exception exception) {
            return null;
        }
    }

    private VaultEntryEntity encryptEntry(VaultEntry entry) throws VaultLockedException {
        return keyProvider.withVaultKey(vaultKey -> {
            JSONObject json = new JSONObject();
            json.put("title", entry.getTitle() != null ? entry.getTitle() : "");
            json.put("username", entry.getUsername() != null ? entry.getUsername() : "");
            json.put("password", entry.getPassword() != null ? entry.getPassword() : "");
            json.put("url", entry.getUrl() != null ? entry.getUrl() : "");
            json.put("notes", entry.getNotes() != null ? entry.getNotes() : "");

            byte[] plainBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            byte[] encryptedPayload = cryptoManager.encrypt(plainBytes, vaultKey);

            VaultEntryEntity entity = new VaultEntryEntity();
            entity.id = entry.getId();
            entity.categoryId = entry.getCategoryId();
            entity.favorite = entry.isFavorite();
            entity.createdAt = entry.getCreatedAt();
            entity.updatedAt = entry.getUpdatedAt();
            entity.encryptedPayload = encryptedPayload;

            return entity;
        });
    }
}
