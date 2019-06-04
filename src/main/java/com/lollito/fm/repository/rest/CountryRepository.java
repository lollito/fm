package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Country;

@Repository
public interface CountryRepository extends PagingAndSortingRepository<Country, Long> {

}