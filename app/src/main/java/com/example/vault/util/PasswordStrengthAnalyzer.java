package com.example.vault.util;

public interface PasswordStrengthAnalyzer {
    StrengthResult analyze(String password);
}
