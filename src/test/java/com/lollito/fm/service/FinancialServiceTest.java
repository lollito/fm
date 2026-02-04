package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FinancialServiceTest {

    @Autowired
    private FinancialService financialService;

    @Test
    public void contextLoads() {
        assertNotNull(financialService);
    }
}
