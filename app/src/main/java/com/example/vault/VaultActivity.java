package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

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
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ServiceLocator.getInstance().getVaultRepository().observeEntries()
                .observe(this, adapter::submitList);
        binding.addEntryButton.setOnClickListener(v ->
                startActivity(new Intent(this, EditEntryActivity.class)));
        binding.settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        binding.lockButton.setOnClickListener(v -> {
            ServiceLocator.getInstance().getVaultSession().lock();
            startActivity(new Intent(this, LockActivity.class));
            finish();
        });
    }
}
