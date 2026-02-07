package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Contract;
import com.lollito.fm.model.ContractStatus;
import com.lollito.fm.model.Foot;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerRole;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.YouthAcademy;
import com.lollito.fm.model.YouthCandidate;
import com.lollito.fm.repository.rest.ContractRepository;
import com.lollito.fm.repository.rest.YouthAcademyRepository;
import com.lollito.fm.repository.rest.YouthCandidateRepository;
import com.lollito.fm.utils.RandomUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class YouthAcademyService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired private YouthAcademyRepository youthAcademyRepository;
    @Autowired private YouthCandidateRepository youthCandidateRepository;
    @Autowired private NameService nameService;
    @Autowired private PlayerService playerService;
    @Autowired private ContractRepository contractRepository;
    @Autowired private ClubService clubService;

    @Value("${fm.youth.generation.count:3}")
    private Integer generationCount;

    @Value("${fm.youth.quality.multiplier:1.0}")
    private Double qualityMultiplier;

    @Scheduled(cron = "${fm.youth.generation.cron:0 0 12 * * MON}")
    @Transactional
    public void generateYouthCandidates() {
        logger.info("Starting scheduled youth candidate generation...");
        List<YouthAcademy> academies = youthAcademyRepository.findAll();

        for (YouthAcademy academy : academies) {
            try {
                if (academy.getMaxYouthPlayers() != null && academy.getCandidates().size() >= academy.getMaxYouthPlayers()) {
                    continue; // Skip if full
                }

                int count = generationCount;

                for (int i = 0; i < count; i++) {
                     createCandidate(academy);
                }
            } catch (Exception e) {
                logger.error("Error generating candidates for academy {}", academy.getId(), e);
            }
        }
        logger.info("Completed youth candidate generation.");
    }

    private YouthCandidate createCandidate(YouthAcademy academy) {
        String name = RandomUtils.randomValueFromList(nameService.getNames());
        String surname = RandomUtils.randomValueFromList(nameService.getSurnames());
        int age = RandomUtils.randomValue(15, 17);
        LocalDate birth = LocalDate.now().minusYears(age).minusDays(RandomUtils.randomValue(0, 364));

        PlayerRole role = PlayerRole.values()[RandomUtils.randomValue(0, PlayerRole.values().length - 1)];
        Foot foot = Foot.values()[RandomUtils.randomValue(0, Foot.values().length - 1)];

        double base = 20.0 + ((academy.getOverallQuality() != null ? academy.getOverallQuality() : 1) * 3.0);
        base *= qualityMultiplier;

        YouthCandidate candidate = YouthCandidate.builder()
            .name(name)
            .surname(surname)
            .birth(birth)
            .role(role)
            .preferredFoot(foot)
            .nationality(academy.getClub().getLeague().getCountry())
            .youthAcademy(academy)
            .build();

        // Generate attributes based on role and quality
        generateAttributes(candidate, base + RandomUtils.randomValue(0.0, 20.0));

        return youthCandidateRepository.save(candidate);
    }

    private void generateAttributes(YouthCandidate c, double avg) {
        // Distribute avg around attributes with bias towards role
        c.setStamina(randomize(avg));
        c.setPlaymaking(randomize(avg));
        c.setScoring(randomize(avg));
        c.setWinger(randomize(avg));
        c.setGoalkeeping(randomize(avg));
        c.setPassing(randomize(avg));
        c.setDefending(randomize(avg));
        c.setSetPieces(randomize(avg));

        // Boost primary skills
        double boost = 10.0;
        switch(c.getRole()) {
            case GOALKEEPER -> c.setGoalkeeping(c.getGoalkeeping() + boost);
            case DEFENDER -> c.setDefending(c.getDefending() + boost);
            case MIDFIELDER -> { c.setPlaymaking(c.getPlaymaking() + boost); c.setPassing(c.getPassing() + boost); }
            case WING -> { c.setWinger(c.getWinger() + boost); c.setPassing(c.getPassing() + boost); }
            case FORWARD -> c.setScoring(c.getScoring() + boost);
        }

        // Cap at 99
        c.setStamina(Math.min(99.0, c.getStamina()));
        c.setPlaymaking(Math.min(99.0, c.getPlaymaking()));
        c.setScoring(Math.min(99.0, c.getScoring()));
        c.setWinger(Math.min(99.0, c.getWinger()));
        c.setGoalkeeping(Math.min(99.0, c.getGoalkeeping()));
        c.setPassing(Math.min(99.0, c.getPassing()));
        c.setDefending(Math.min(99.0, c.getDefending()));
        c.setSetPieces(Math.min(99.0, c.getSetPieces()));
    }

    private Double randomize(double val) {
        return Math.max(1.0, val + RandomUtils.randomValue(-5.0, 5.0));
    }

    @Transactional
    public Player promoteCandidate(Long candidateId) {
        YouthCandidate candidate = youthCandidateRepository.findById(candidateId)
            .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Club club = candidate.getYouthAcademy().getClub();
        Team team = club.getTeam();

        Player player = Player.builder()
            .name(candidate.getName())
            .surname(candidate.getSurname())
            .birth(candidate.getBirth())
            .role(candidate.getRole())
            .preferredFoot(candidate.getPreferredFoot())
            .team(team)
            .stamina(candidate.getStamina())
            .playmaking(candidate.getPlaymaking())
            .scoring(candidate.getScoring())
            .winger(candidate.getWinger())
            .goalkeeping(candidate.getGoalkeeping())
            .passing(candidate.getPassing())
            .defending(candidate.getDefending())
            .setPieces(candidate.getSetPieces())
            .condition(100.0)
            .moral(100.0)
            .build();

        player = playerService.save(player);

        // Create default contract
        Contract contract = Contract.builder()
            .player(player)
            .club(club)
            .weeklySalary(new BigDecimal(1000)) // Default youth salary
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(3)) // 3 years default
            .status(ContractStatus.ACTIVE)
            .hasReleaseClause(false)
            .build();

        contract = contractRepository.save(contract);
        player.setCurrentContract(contract);
        player = playerService.save(player);

        // Delete candidate
        youthCandidateRepository.delete(candidate);

        return player;
    }

    public List<YouthCandidate> getCandidates(Long academyId) {
        return youthCandidateRepository.findByYouthAcademyId(academyId);
    }
}
