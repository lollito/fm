package com.lollito.fm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PageSerializationTest {

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Test
    public void testPageSerialization() throws IOException {
        Page<String> page = new PageImpl<>(List.of("test"), PageRequest.of(0, 10), 1);
        String json = objectMapper.writeValueAsString(page);

        System.out.println("Serialized Page: " + json);

        // With VIA_DTO, the structure should include "content" and a "page" object for metadata
        assertThat(json).contains("\"content\":[\"test\"]");
        assertThat(json).contains("\"page\":{");
        assertThat(json).contains("\"size\":10");
        assertThat(json).contains("\"totalElements\":1");
        assertThat(json).contains("\"totalPages\":1");
        assertThat(json).contains("\"number\":0");
    }
}
