package com.example.vault.util;

import java.util.List;

public class StrengthResult {
    public enum Level { VERY_WEAK, WEAK, FAIR, STRONG, VERY_STRONG }

    public final Level level;
    public final float score;
    public final List<String> tips;

    public StrengthResult(Level level, float score, List<String> tips) {
        this.level = level;
        this.score = score;
        this.tips = tips;
    }
}
