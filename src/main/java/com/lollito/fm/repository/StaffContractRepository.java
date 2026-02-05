package com.lollito.fm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.ContractStatus;
import com.lollito.fm.model.Staff;
import com.lollito.fm.model.StaffContract;

@Repository
public interface StaffContractRepository extends JpaRepository<StaffContract, Long> {
    Optional<StaffContract> findByStaffAndStatus(Staff staff, ContractStatus status);
}
