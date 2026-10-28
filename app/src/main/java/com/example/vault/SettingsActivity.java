package com.example.vault;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
