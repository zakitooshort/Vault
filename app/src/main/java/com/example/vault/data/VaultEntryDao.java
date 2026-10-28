package com.example.vault.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VaultEntryDao {
    @Query("SELECT * FROM vault_entries ORDER BY updatedAt DESC")
    LiveData<List<VaultEntryEntity>> observeAll();

    @Query("SELECT * FROM vault_entries WHERE id = :id")
    VaultEntryEntity getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VaultEntryEntity entry);

    @Query("DELETE FROM vault_entries WHERE id = :id")
    void delete(String id);
}
