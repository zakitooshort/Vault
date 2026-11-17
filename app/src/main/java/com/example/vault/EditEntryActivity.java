package com.example.vault;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.data.VaultEntry;
import com.example.vault.databinding.ActivityEditEntryBinding;
import com.example.vault.util.ServiceLocator;

import java.util.UUID;

public class EditEntryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEditEntryBinding binding =
                ActivityEditEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.saveButton.setOnClickListener(v -> {
            String title = binding.titleInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString();
            if (title.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Title and password are required",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            long now = System.currentTimeMillis();
            VaultEntry entry = new VaultEntry(
                    UUID.randomUUID().toString(),
                    title,
                    binding.usernameInput.getText().toString().trim(),
                    password,
                    binding.urlInput.getText().toString().trim(),
                    binding.notesInput.getText().toString().trim(),
                    null,
                    false,
                    now,
                    now);
            ServiceLocator.getInstance().getVaultRepository().addEntry(entry);
            Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
