package com.example.vault.data;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface VaultRepository {
    LiveData<List<VaultEntry>> observeEntries();
    void getEntry(String id, Callback<VaultEntry> callback);
    void addEntry(VaultEntry entry);
    void updateEntry(VaultEntry entry);
    void deleteEntry(String id);
    LiveData<List<Category>> observeCategories();
}
