package com.example.vault;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.data.VaultEntry;
import com.example.vault.databinding.ActivityEntryDetailBinding;
import com.example.vault.util.ServiceLocator;

public class EntryDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ENTRY_ID = "entry_id";
    private static final long CLIPBOARD_CLEAR_DELAY_MS = 30_000L;

    private final Handler clipboardHandler = new Handler(Looper.getMainLooper());
    private String entryId;
    private String currentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        ActivityEntryDetailBinding binding =
                ActivityEntryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        entryId = getIntent().getStringExtra(EXTRA_ENTRY_ID);
        if (entryId == null) {
            finish();
            return;
        }
        binding.copyPasswordButton.setOnClickListener(v -> copyPassword());
        binding.deleteEntryButton.setOnClickListener(v -> deleteEntry());
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
        currentPassword = entry.getPassword();
    }

    private void copyPassword() {
        if (currentPassword == null || currentPassword.isEmpty()) {
            Toast.makeText(this, "No password to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboardManager =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            Toast.makeText(this, "Clipboard unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipData clipData = ClipData.newPlainText("vault-password", currentPassword);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "Password copied for 30 seconds", Toast.LENGTH_SHORT).show();

        clipboardHandler.removeCallbacksAndMessages(null);
        clipboardHandler.postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboardManager.clearPrimaryClip();
            } else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""));
            }
        }, CLIPBOARD_CLEAR_DELAY_MS);
    }

    private void deleteEntry() {
        if (entryId == null) {
            return;
        }
        ServiceLocator.getInstance().getVaultRepository().deleteEntry(entryId);
        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        clipboardHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
