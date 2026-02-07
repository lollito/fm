package com.lollito.fm.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.AssignmentStatus;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutStatus;
import com.lollito.fm.model.ScoutingAssignment;
import com.lollito.fm.model.ScoutingType;
import com.lollito.fm.repository.ScoutingAssignmentRepository;
import com.lollito.fm.repository.ScoutRepository;
import com.lollito.fm.repository.rest.ClubRepository;
import com.lollito.fm.repository.rest.PlayerRepository;

@SpringBootTest
@Transactional
public class ScoutingServiceReproductionTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private ScoutingService scoutingService;
    @Autowired private ScoutingAssignmentRepository assignmentRepository;
    @Autowired private ClubRepository clubRepository;
    @Autowired private ScoutRepository scoutRepository;
    @Autowired private PlayerRepository playerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testProcessDailyScoutingProgressPerformance() {
        // Setup data
        createTestData();

        // Clear persistence context to ensure we are actually fetching from DB
        entityManager.flush();
        entityManager.clear();

        Session session = entityManager.unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        long start = System.nanoTime();

        // Run the method
        scoutingService.processDailyScoutingProgress();

        long end = System.nanoTime();
        long duration = (end - start) / 1_000_000; // ms

        long queryCount = stats.getPrepareStatementCount();
        logger.info("Duration: {} ms", duration);
        logger.info("Query Count: {}", queryCount);

        // We expect N+1 queries.
        // 1 query to fetch assignments.
        // For 100 assignments, we fetch scout (lazy) and player (lazy).
        // So at least 200 queries if they are accessed.
        // Even if some assignments complete, the base iteration causes N+1 reads.

        // If query count is low, it means N+1 is not happening or data is cached.
        // If query count is high (> 100), we confirmed the issue.
        if (queryCount < 100) {
            logger.warn("Query count is suspiciously low: " + queryCount + ". Is lazy loading disabled or data cached?");
        } else {
             logger.info("Confirmed N+1 query issue with " + queryCount + " queries.");
        }

        // After optimization, the query count should be low.
        // 1 main query + queries for completed assignments (5% of 100 = 5) * writes (~3) = ~16
        // We use a safe upper bound.
        Assertions.assertTrue(queryCount < 50, "Expected low query count after optimization, but got " + queryCount);
    }

    private void createTestData() {
        Club club = new Club();
        club.setName("Test Club");
        club = clubRepository.save(club);

        List<Scout> scouts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Scout scout = new Scout();
            scout.setClub(club);
            scout.setName("Scout " + i);
            scout.setSurname("Surname " + i);
            scout.setStatus(ScoutStatus.ACTIVE);
            scout.setAbility(10);
            scout.setExperience(5);
            scout = scoutRepository.save(scout);
            scouts.add(scout);
        }

        for (int i = 0; i < 100; i++) {
            Player player = new Player();
            player.setName("Player " + i);
            player.setSurname("Surname " + i);
            player.setBirth(LocalDate.of(2000, 1, 1));
            player = playerRepository.save(player);

            Scout scout = scouts.get(i);

            ScoutingAssignment assignment = ScoutingAssignment.builder()
                .scout(scout)
                .targetPlayer(player)
                .status(AssignmentStatus.IN_PROGRESS)
                .type(ScoutingType.PLAYER)
                .assignedDate(LocalDate.now().minusDays(1))
                .expectedCompletionDate(LocalDate.now().plusDays(100))
                .build();
            assignmentRepository.save(assignment);
        }
    }
}
