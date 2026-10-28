package com.example.vault;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.databinding.ActivityLockBinding;

public class LockActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLockBinding binding = ActivityLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.unlockButton.setOnClickListener(v ->
                startActivity(new Intent(this, VaultActivity.class)));
    }
}
