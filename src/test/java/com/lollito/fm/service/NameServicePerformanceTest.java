package com.lollito.fm.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NameServicePerformanceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NameService nameService;

    @Test
    public void testGetNamesPerformance() {
        int iterations = 1000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            List<String> names = nameService.getNames();
            if (names.isEmpty()) {
                throw new RuntimeException("Names list is empty");
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // ms
        logger.info("getNames() 1000 times took: {} ms", duration);
    }

    @Test
    public void testGetSurnamesPerformance() {
        int iterations = 1000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            List<String> surnames = nameService.getSurnames();
            if (surnames.isEmpty()) {
                throw new RuntimeException("Surnames list is empty");
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // ms
        logger.info("getSurnames() 1000 times took: {} ms", duration);
    }

    @Test
    public void testGetCountryFileLanesPerformance() {
        int iterations = 1000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            List<String> countries = nameService.getCountryFileLanes();
            if (countries.isEmpty()) {
                throw new RuntimeException("Country list is empty");
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // ms
        logger.info("getCountryFileLanes() 1000 times took: {} ms", duration);
    }
}
