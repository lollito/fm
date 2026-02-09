package com.lollito.fm.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class RandomUtilsTest {

    @Test
    void testRandomPercentageBounds() {
        // 0% should always be false (statistically, but since randomValue(1,100) returns at least 1, 1 <= 0 is false)
        for (int i = 0; i < 1000; i++) {
            assertFalse(RandomUtils.randomPercentage(0), "0% should never return true");
        }

        // 100% should always be true (since randomValue(1,100) returns at most 100, 100 <= 100 is true)
        for (int i = 0; i < 1000; i++) {
            assertTrue(RandomUtils.randomPercentage(100), "100% should always return true");
        }
    }

    @RepeatedTest(10)
    void testRandomPercentageDistribution() {
        int trials = 10000;
        int trueCount = 0;
        double percentage = 50.0;

        for (int i = 0; i < trials; i++) {
            if (RandomUtils.randomPercentage(percentage)) {
                trueCount++;
            }
        }

        // Allow some variance, e.g., within 45% and 55%
        // Standard deviation for binomial distribution (n=10000, p=0.5) is sqrt(10000*0.5*0.5) = 50.
        // 3 sigma is 150. So 4850 to 5150 is 99.7% confidence.
        // I used 4500 to 5500 which is very safe.
        assertTrue(trueCount > 4500 && trueCount < 5500, "Distribution should be roughly 50%");
    }
}
