package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Module;

@RepositoryRestResource(collectionResourceRel = "module", path = "module")
public interface ModuleRepository extends PagingAndSortingRepository<Module, Long> {

}