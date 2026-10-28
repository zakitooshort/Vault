package com.example.vault;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.vault.crypto.BiometricKeyStore;
import com.example.vault.crypto.VaultSession;
import com.example.vault.databinding.ActivityLockBinding;
import com.example.vault.util.ServiceLocator;

import javax.crypto.Cipher;

public class LockActivity extends AppCompatActivity {
    private ActivityLockBinding binding;
    private VaultSession vaultSession;
    private BiometricKeyStore biometricKeyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vaultSession = ServiceLocator.getInstance().getVaultSession();
        biometricKeyStore = ServiceLocator.getInstance().getBiometricKeyStore();

        setupPasswordUnlock();
        setupBiometricUnlock();
    }

    private void setupPasswordUnlock() {
        binding.unlockButton.setOnClickListener(v -> {
            String password = binding.passwordInput.getText() != null ?
                    binding.passwordInput.getText().toString() : "";
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter master password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (vaultSession.unlockWithPassword(password.toCharArray())) {
                navigateToVault();
            } else {
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBiometricUnlock() {
        if (!biometricKeyStore.isAvailable()) {
            binding.biometricUnlockButton.setVisibility(View.GONE);
            return;
        }

        binding.biometricUnlockButton.setVisibility(View.VISIBLE);
        binding.biometricUnlockButton.setOnClickListener(v -> showBiometricPrompt());
    }

    private void showBiometricPrompt() {
        try {
            Cipher cipher = biometricKeyStore.cipherForUnlock();
            if (cipher == null) {
                Toast.makeText(this, "Biometrics not set up for this vault", Toast.LENGTH_SHORT).show();
                return;
            }

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Vault")
                    .setSubtitle("Authenticate using your biometric credential")
                    .setNegativeButtonText("Cancel")
                    .build();

            BiometricPrompt biometricPrompt = new BiometricPrompt(
                    this,
                    ContextCompat.getMainExecutor(this),
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            if (result.getCryptoObject() != null &&
                                    result.getCryptoObject().getCipher() != null) {
                                boolean unlocked = vaultSession.unlockWithBiometrics(
                                        result.getCryptoObject().getCipher());
                                if (unlocked) {
                                    navigateToVault();
                                } else {
                                    Toast.makeText(LockActivity.this,
                                            "Biometric unlock failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(LockActivity.this,
                                    "Biometric error: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
        } catch (Exception exception) {
            Toast.makeText(this, "Biometric setup error", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToVault() {
        startActivity(new Intent(this, VaultActivity.class));
        finish();
    }
}
