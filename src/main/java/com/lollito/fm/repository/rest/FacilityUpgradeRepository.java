package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.FacilityUpgrade;
import com.lollito.fm.model.UpgradeStatus;

@Repository
public interface FacilityUpgradeRepository extends JpaRepository<FacilityUpgrade, Long> {
    List<FacilityUpgrade> findByClubAndStatusIn(Club club, List<UpgradeStatus> statuses);
}
