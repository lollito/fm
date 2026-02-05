package com.lollito.fm.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Staff;
import com.lollito.fm.model.StaffRole;
import com.lollito.fm.model.StaffStatus;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByClub(Club club);
    List<Staff> findByStatus(StaffStatus status);
    Page<Staff> findByStatusAndRole(StaffStatus status, StaffRole role, Pageable pageable);
    Page<Staff> findByStatus(StaffStatus status, Pageable pageable);

    // Find staff without a club (available)
    Page<Staff> findByClubIsNullAndStatus(StaffStatus status, Pageable pageable);
    Page<Staff> findByClubIsNullAndStatusAndRole(StaffStatus status, StaffRole role, Pageable pageable);
}
