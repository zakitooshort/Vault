package com.example.vault;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.vault.data.VaultEntry;
import com.example.vault.databinding.ActivityVaultBinding;
import com.example.vault.util.ServiceLocator;

public class VaultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityVaultBinding binding = ActivityVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EntryAdapter adapter = new EntryAdapter(entry -> {
            Intent intent = new Intent(this, EntryDetailActivity.class);
            intent.putExtra(EntryDetailActivity.EXTRA_ENTRY_ID, entry.getId());
            startActivity(intent);
        });
        binding.entryList.setLayoutManager(new LinearLayoutManager(this));
        binding.entryList.setAdapter(adapter);
        ServiceLocator.getInstance().getVaultRepository().observeEntries()
                .observe(this, adapter::submitList);
        binding.addEntryButton.setOnClickListener(v ->
                startActivity(new Intent(this, EditEntryActivity.class)));
        binding.settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}
