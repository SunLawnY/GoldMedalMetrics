package com.codecademy.goldmedal.repository;

import com.codecademy.goldmedal.model.Country;
import com.codecademy.goldmedal.model.GoldMedal;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends CrudRepository<Country, Long>  {
    Optional<Country> getByName(String name);
    List<Country> getAllByOrderByNameAsc();
    List<Country> getAllByOrderByGdpAsc();
    List<Country> getAllByOrderByPopulationAsc();
    List<Country> getAllByOrderByNameDesc();
    List<Country> getAllByOrderByGdpDesc();
    List<Country> getAllByOrderByPopulationDesc();
}
