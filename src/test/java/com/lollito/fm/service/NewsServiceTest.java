package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.lollito.fm.model.News;
import com.lollito.fm.repository.rest.NewsRepository;

@ExtendWith(MockitoExtension.class)
public class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    public void testFindLatest() {
        int limit = 3;
        News news1 = new News("News 1", LocalDateTime.now());
        News news2 = new News("News 2", LocalDateTime.now().minusDays(1));
        News news3 = new News("News 3", LocalDateTime.now().minusDays(2));
        List<News> expectedNews = Arrays.asList(news1, news2, news3);
        Page<News> page = new PageImpl<>(expectedNews);

        when(newsRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<News> result = newsService.findLatest(limit);

        assertEquals(3, result.size());
        verify(newsRepository).findAll(any(Pageable.class));
    }
}
