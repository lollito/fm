package com.lollito.fm.repository.rest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lollito.fm.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
}