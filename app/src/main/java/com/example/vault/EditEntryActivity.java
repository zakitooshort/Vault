package com.example.vault;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.databinding.ActivityEditEntryBinding;

public class EditEntryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEditEntryBinding binding =
                ActivityEditEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
