package com.example.vault;

import static org.junit.Assert.assertEquals;

import com.example.vault.fake.RuleBasedStrengthAnalyzer;
import com.example.vault.util.StrengthResult;

import org.junit.Test;

public class RuleBasedStrengthAnalyzerTest {
    @Test
    public void shortPasswordIsVeryWeak() {
        StrengthResult result = new RuleBasedStrengthAnalyzer().analyze("abc");
        assertEquals(StrengthResult.Level.VERY_WEAK, result.level);
    }
}
