package com.lollito.fm.repository.rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.AdminAction;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Long>, JpaSpecificationExecutor<AdminAction> {
}
