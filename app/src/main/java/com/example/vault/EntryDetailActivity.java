package com.example.vault;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.data.VaultEntry;
import com.example.vault.databinding.ActivityEntryDetailBinding;
import com.example.vault.util.ServiceLocator;

public class EntryDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ENTRY_ID = "entry_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEntryDetailBinding binding =
                ActivityEntryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String entryId = getIntent().getStringExtra(EXTRA_ENTRY_ID);
        if (entryId == null) {
            finish();
            return;
        }
        ServiceLocator.getInstance().getVaultRepository().getEntry(entryId, entry -> {
            runOnUiThread(() -> showEntry(binding, entry));
        });
    }

    private void showEntry(ActivityEntryDetailBinding binding, VaultEntry entry) {
        if (entry == null) {
            Toast.makeText(this, "Entry unavailable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.titleText.setText(entry.getTitle());
        binding.usernameText.setText(entry.getUsername());
        binding.passwordText.setText(entry.getPassword());
        binding.urlText.setText(entry.getUrl());
        binding.notesText.setText(entry.getNotes());
    }
}
