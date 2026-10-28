package com.example.vault.fake;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.vault.data.Callback;
import com.example.vault.data.Category;
import com.example.vault.data.VaultEntry;
import com.example.vault.data.VaultRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FakeVaultRepository implements VaultRepository {
    private final List<VaultEntry> entries = new ArrayList<>();
    private final MutableLiveData<List<VaultEntry>> entriesLiveData;
    private final MutableLiveData<List<Category>> categoriesLiveData =
            new MutableLiveData<>(Collections.emptyList());

    public FakeVaultRepository() {
        long now = System.currentTimeMillis();
        entries.add(new VaultEntry(
                UUID.randomUUID().toString(), "Example entry", "user@example.com",
                "not-a-real-password", null, "Sample data", null, false, now, now));
        entriesLiveData = new MutableLiveData<>(
                Collections.unmodifiableList(new ArrayList<>(entries)));
    }

    @Override
    public LiveData<List<VaultEntry>> observeEntries() {
        return entriesLiveData;
    }

    @Override
    public void getEntry(String id, Callback<VaultEntry> callback) {
        for (VaultEntry entry : entries) {
            if (entry.getId().equals(id)) {
                callback.onResult(entry);
                return;
            }
        }
        callback.onResult(null);
    }

    @Override
    public void addEntry(VaultEntry entry) {
        entries.add(entry);
        publishEntries();
    }

    @Override
    public void updateEntry(VaultEntry entry) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(entry.getId())) {
                entries.set(i, entry);
                publishEntries();
                return;
            }
        }
        addEntry(entry);
    }

    @Override
    public void deleteEntry(String id) {
        entries.removeIf(entry -> entry.getId().equals(id));
        publishEntries();
    }

    @Override
    public LiveData<List<Category>> observeCategories() {
        return categoriesLiveData;
    }

    private void publishEntries() {
        entriesLiveData.postValue(Collections.unmodifiableList(new ArrayList<>(entries)));
    }
}
