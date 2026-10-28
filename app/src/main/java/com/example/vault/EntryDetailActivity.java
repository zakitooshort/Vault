package com.example.vault;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vault.databinding.ActivityEntryDetailBinding;

public class EntryDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEntryDetailBinding binding =
                ActivityEntryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
