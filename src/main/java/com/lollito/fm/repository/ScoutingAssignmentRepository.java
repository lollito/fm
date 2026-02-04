package com.lollito.fm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.AssignmentStatus;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.ScoutingAssignment;

@Repository
public interface ScoutingAssignmentRepository extends JpaRepository<ScoutingAssignment, Long> {

    @Query("SELECT sa FROM ScoutingAssignment sa WHERE sa.scout.club = :club AND sa.targetPlayer = :player AND sa.status IN ('ASSIGNED', 'IN_PROGRESS')")
    Optional<ScoutingAssignment> findActiveAssignmentForPlayer(@Param("club") Club club, @Param("player") Player player);

    List<ScoutingAssignment> findByStatus(AssignmentStatus status);

    @Query("SELECT sa FROM ScoutingAssignment sa WHERE sa.scout.club.id = :clubId")
    List<ScoutingAssignment> findByClubId(@Param("clubId") Long clubId);

    @Query("SELECT sa FROM ScoutingAssignment sa WHERE sa.scout.club.id = :clubId AND sa.status = :status")
    List<ScoutingAssignment> findByClubIdAndStatus(@Param("clubId") Long clubId, @Param("status") AssignmentStatus status);
}
