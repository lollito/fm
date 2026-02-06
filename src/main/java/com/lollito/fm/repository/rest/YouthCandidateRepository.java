package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.YouthCandidate;

@Repository
public interface YouthCandidateRepository extends JpaRepository<YouthCandidate, Long> {
    List<YouthCandidate> findByYouthAcademyId(Long youthAcademyId);
}
