package com.example.vault;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.databinding.ActivityVaultBinding;

public class VaultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityVaultBinding binding = ActivityVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.entryButton.setOnClickListener(v ->
                startActivity(new Intent(this, EntryDetailActivity.class)));
        binding.addEntryButton.setOnClickListener(v ->
                startActivity(new Intent(this, EditEntryActivity.class)));
        binding.settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}
