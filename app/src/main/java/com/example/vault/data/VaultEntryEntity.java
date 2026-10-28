package com.example.vault.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vault_entries")
public class VaultEntryEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String categoryId;
    public boolean favorite;
    public long createdAt;
    public long updatedAt;
    public byte[] encryptedPayload;
}
