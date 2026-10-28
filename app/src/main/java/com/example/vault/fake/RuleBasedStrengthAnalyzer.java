package com.example.vault.fake;

import com.example.vault.util.PasswordStrengthAnalyzer;
import com.example.vault.util.StrengthResult;

import java.util.ArrayList;
import java.util.List;

public class RuleBasedStrengthAnalyzer implements PasswordStrengthAnalyzer {
    @Override
    public StrengthResult analyze(String password) {
        String value = password == null ? "" : password;
        int types = 0;
        if (value.matches(".*[a-z].*")) types++;
        if (value.matches(".*[A-Z].*")) types++;
        if (value.matches(".*\\d.*")) types++;
        if (value.matches(".*[^a-zA-Z\\d].*")) types++;

        float score = Math.min(1.0f, (value.length() / 20.0f) * 0.6f + (types / 4.0f) * 0.4f);
        List<String> tips = new ArrayList<>();
        if (value.length() < 12) tips.add("Use at least 12 characters");
        if (types < 3) tips.add("Mix lowercase, uppercase, digits, and symbols");

        StrengthResult.Level level;
        if (score < 0.2f) level = StrengthResult.Level.VERY_WEAK;
        else if (score < 0.4f) level = StrengthResult.Level.WEAK;
        else if (score < 0.6f) level = StrengthResult.Level.FAIR;
        else if (score < 0.8f) level = StrengthResult.Level.STRONG;
        else level = StrengthResult.Level.VERY_STRONG;
        return new StrengthResult(level, score, tips);
    }
}
