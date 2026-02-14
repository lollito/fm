package com.lollito.fm.repository.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.League;
import com.lollito.fm.model.ManagerPerk;
import com.lollito.fm.model.ManagerProfile;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.User;

@DataJpaTest
public class RankingRepositoryPerformanceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testFindBySeason_NPlusOne() {
        // 1. Setup Data
        League league = new League();
        league.setName("Test League");
        entityManager.persist(league);

        Season season = new Season();
        season.setName("Test Season");
        season.setLeague(league);
        league.setCurrentSeason(season);
        entityManager.persist(season);

        int n = 5;
        for (int i = 0; i < n; i++) {
            User user = new User();
            user.setUsername("user" + i);
            entityManager.persist(user);

            Club club = new Club();
            club.setName("Club " + i);
            club.setUser(user);
            club.setLeague(league);
            user.setClub(club);
            entityManager.persist(club);

            ManagerProfile mp = new ManagerProfile();
            mp.setUser(user);
            mp.getUnlockedPerks().add(ManagerPerk.VIDEO_ANALYST);
            if (i == 0) {
                 mp.getUnlockedPerks().add(ManagerPerk.MOTIVATOR); // Add 2nd perk to first user
            }
            user.setManagerProfile(mp);
            entityManager.persist(mp);

            Ranking ranking = new Ranking();
            ranking.setClub(club);
            ranking.setSeason(season);
            // Set points to verify ordering (i=0 -> 0 points, i=4 -> 40 points)
            ranking.setPoints(i * 10);
            season.getRankingLines().add(ranking);
            entityManager.persist(ranking);
        }

        entityManager.flush();
        entityManager.clear();

        // 2. Measure
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        List<Ranking> rankings = rankingRepository.findDistinctBySeasonOrderByPointsDesc(season);

        // 3. Trigger Lazy Loading
        // We simulate what RankingMapper -> ClubMapper does: accesses club.getUser()
        for (Ranking r : rankings) {
            Club c = r.getClub();
            User u = c.getUser();
            if (u != null) {
                u.getUsername();
                if (u.getManagerProfile() != null) {
                    u.getManagerProfile().getUnlockedPerks().size();
                }
            }
        }

        long queryCount = statistics.getPrepareStatementCount();
        System.out.println("Query Count: " + queryCount);

        // With N=5, if N+1 exists:
        // 1 query for rankings (with join fetch club)
        // 5 queries for users (accessed from club)
        // Total ~6 queries.

        // If optimized:
        // 1 query for rankings + club + user
        // Total 1 query.

        // We assert based on expectation.
        // Now it should be 1.
        assertThat(queryCount).isEqualTo(1);

        // Assert size (should be 5, no duplicates)
        assertThat(rankings).hasSize(5);

        // Assert order (descending by points)
        assertThat(rankings).extracting(Ranking::getPoints)
            .isSortedAccordingTo((p1, p2) -> p2.compareTo(p1));
    }
}
