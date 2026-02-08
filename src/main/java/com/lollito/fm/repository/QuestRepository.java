package com.lollito.fm.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Quest;
import com.lollito.fm.model.QuestFrequency;
import com.lollito.fm.model.QuestStatus;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByUserIdAndStatusAndExpirationDateAfter(Long userId, QuestStatus status, LocalDateTime now);
    List<Quest> findByUserIdAndFrequency(Long userId, QuestFrequency frequency);
    void deleteByExpirationDateBefore(LocalDateTime now);
}
