package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vault.databinding.ActivityMainBinding;
import com.example.vault.crypto.VaultSession;
import com.example.vault.util.ServiceLocator;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private VaultSession vaultSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vaultSession = ServiceLocator.getInstance().getVaultSession();
        if (vaultSession.hasVault()) {
            startActivity(new Intent(this, LockActivity.class));
            finish();
            return;
        }
        binding.continueButton.setOnClickListener(v -> createVault());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void createVault() {
        String passwordValue = binding.passwordInput.getText() == null
                ? "" : binding.passwordInput.getText().toString();
        String confirmationValue = binding.confirmPasswordInput.getText() == null
                ? "" : binding.confirmPasswordInput.getText().toString();

        if (passwordValue.length() < 8) {
            binding.passwordInput.setError("Use at least 8 characters");
            return;
        }
        if (!passwordValue.equals(confirmationValue)) {
            binding.confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        char[] password = passwordValue.toCharArray();
        char[] confirmation = confirmationValue.toCharArray();
        binding.continueButton.setEnabled(false);
        executor.execute(() -> {
            try {
                vaultSession.createVault(password);
                runOnUiThread(() -> {
                    startActivity(new Intent(this, VaultActivity.class));
                    finish();
                });
            } catch (RuntimeException exception) {
                runOnUiThread(() -> {
                    binding.continueButton.setEnabled(true);
                    Toast.makeText(this, "Could not create vault", Toast.LENGTH_LONG).show();
                });
            } finally {
                Arrays.fill(password, '\0');
                Arrays.fill(confirmation, '\0');
            }
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}