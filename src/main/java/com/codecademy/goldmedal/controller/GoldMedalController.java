package com.codecademy.goldmedal.controller;

import com.codecademy.goldmedal.model.*;
import com.codecademy.goldmedal.repository.CountryRepository;
import com.codecademy.goldmedal.repository.GoldMedalsRepository;
import org.apache.commons.text.WordUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/countries")
public class GoldMedalController {
    // TODO: declare references to your repositories
    CountryRepository countryRepository;
    GoldMedalsRepository goldMedalsRepository;

    // TODO: update your constructor to include your repositories
    public GoldMedalController(CountryRepository countryRepository, GoldMedalsRepository goldMedalsRepository) {
        this.countryRepository = countryRepository;
        this.goldMedalsRepository = goldMedalsRepository;
    }

    @GetMapping
    public CountriesResponse getCountries(@RequestParam String sort_by, @RequestParam String ascending) {
        var ascendingOrder = ascending.toLowerCase().equals("y");
        return new CountriesResponse(getCountrySummaries(sort_by.toLowerCase(), ascendingOrder));
    }

    @GetMapping("/{country}")
    public CountryDetailsResponse getCountryDetails(@PathVariable String country) {
        String countryName = WordUtils.capitalizeFully(country);
        return getCountryDetailsResponse(countryName);
    }

    @GetMapping("/{country}/medals")
    public CountryMedalsListResponse getCountryMedalsList(@PathVariable String country, @RequestParam String sort_by, @RequestParam String ascending) {
        String countryName = WordUtils.capitalizeFully(country);
        var ascendingOrder = ascending.toLowerCase().equals("y");
        return getCountryMedalsListResponse(countryName, sort_by.toLowerCase(), ascendingOrder);
    }

    private CountryMedalsListResponse getCountryMedalsListResponse(String countryName, String sortBy, boolean ascendingOrder) {
        List<GoldMedal> medalsList;
        switch (sortBy) {
            case "year":
                medalsList = ascendingOrder ? goldMedalsRepository.getByCountryOrderByYearAsc(countryName) : goldMedalsRepository.getByCountryOrderByYearDesc(countryName);// TODO: list of medals sorted by year in the given order
                break;
            case "season":
                medalsList = ascendingOrder ? goldMedalsRepository.getByCountryOrderBySeasonAsc(countryName): goldMedalsRepository.getByCountryOrderBySeasonDesc(countryName);// TODO: list of medals sorted by season in the given order
                break;
            case "city":
                medalsList = ascendingOrder ? goldMedalsRepository.getByCountryOrderByCityAsc(countryName) : goldMedalsRepository.getByCountryOrderByCityDesc(countryName);// TODO: list of medals sorted by city in the given order
                break;
            case "name":
                medalsList = ascendingOrder ? goldMedalsRepository.getByCountryOrderByNameAsc(countryName) : goldMedalsRepository.getByCountryOrderByNameDesc(countryName);// TODO: list of medals sorted by athlete's name in the given order
                break;
            case "event":
                medalsList = ascendingOrder ? goldMedalsRepository.getByCountryOrderByEventAsc(countryName) : goldMedalsRepository.getByCountryOrderByEventDesc(countryName);// TODO: list of medals sorted by event in the given order
                break;
            default:
                medalsList = new ArrayList<>();
                break;
        }

        return new CountryMedalsListResponse(medalsList);
    }

    private CountryDetailsResponse getCountryDetailsResponse(String countryName) {
        var countryOptional = countryRepository.getByName(countryName); // TODO: get the country; this repository method should return a java.util.Optional
        if (countryOptional.isEmpty()) {
            return new CountryDetailsResponse(countryName);
        }

        var country = countryOptional.get();
        var goldMedalCount = goldMedalsRepository.countByCountry(countryName);// TODO: get the medal count

        var summerWins = goldMedalsRepository.getByCountryAndSeasonOrderByYearAsc(countryName, "Summer");// TODO: get the collection of wins at the Summer Olympics, sorted by year in ascending order
        var numberSummerWins = summerWins.size() > 0 ? summerWins.size() : null;
        var totalSummerEvents = goldMedalsRepository.countBySeason("Summer");// TODO: get the total number of events at the Summer Olympics
        var percentageTotalSummerWins = totalSummerEvents != 0 && numberSummerWins != null ? (float) summerWins.size() / totalSummerEvents : null;
        var yearFirstSummerWin = summerWins.size() > 0 ? summerWins.get(0).getYear() : null;

        var winterWins = goldMedalsRepository.getByCountryAndSeasonOrderByYearAsc(countryName, "Winter");// TODO: get the collection of wins at the Winter Olympics
        var numberWinterWins = winterWins.size() > 0 ? winterWins.size() : null;
        var totalWinterEvents = goldMedalsRepository.countBySeason("Winter");// TODO: get the total number of events at the Winter Olympics, sorted by year in ascending order
        var percentageTotalWinterWins = totalWinterEvents != 0 && numberWinterWins != null ? (float) winterWins.size() / totalWinterEvents : null;
        var yearFirstWinterWin = winterWins.size() > 0 ? winterWins.get(0).getYear() : null;

        var numberEventsWonByFemaleAthletes = goldMedalsRepository.countByCountryAndGender(countryName, "Women");// TODO: get the number of wins by female athletes
        var numberEventsWonByMaleAthletes = goldMedalsRepository.countByCountryAndGender(countryName, "Men");// TODO: get the number of wins by male athletes

        return new CountryDetailsResponse(
                countryName,
                country.getGdp(),
                country.getPopulation(),
                goldMedalCount,
                numberSummerWins,
                percentageTotalSummerWins,
                yearFirstSummerWin,
                numberWinterWins,
                percentageTotalWinterWins,
                yearFirstWinterWin,
                numberEventsWonByFemaleAthletes,
                numberEventsWonByMaleAthletes);
    }

    private List<CountrySummary> getCountrySummaries(String sortBy, boolean ascendingOrder) {
        List<Country> countries;
        switch (sortBy) {
            case "name":
                countries = ascendingOrder ? countryRepository.getAllByOrderByNameAsc() : countryRepository.getAllByOrderByNameDesc();// TODO: list of countries sorted by name in the given order
                break;
            case "gdp":
                countries = ascendingOrder ? countryRepository.getAllByOrderByGdpAsc() : countryRepository.getAllByOrderByGdpDesc();// TODO: list of countries sorted by gdp in the given order
                break;
            case "population":
                countries = ascendingOrder ? countryRepository.getAllByOrderByPopulationAsc() : countryRepository.getAllByOrderByPopulationDesc();// TODO: list of countries sorted by population in the given order
                break;
            case "medals":
            default:
                countries = countryRepository.getAllByOrderByNameAsc();// TODO: list of countries in any order you choose; for sorting by medal count, additional logic below will handle that
                break;
        }

        var countrySummaries = getCountrySummariesWithMedalCount(countries);

        if (sortBy.equalsIgnoreCase("medals")) {
            countrySummaries = sortByMedalCount(countrySummaries, ascendingOrder);
        }

        return countrySummaries;
    }

    private List<CountrySummary> sortByMedalCount(List<CountrySummary> countrySummaries, boolean ascendingOrder) {
        return countrySummaries.stream()
                .sorted((t1, t2) -> ascendingOrder ?
                        t1.getMedals() - t2.getMedals() :
                        t2.getMedals() - t1.getMedals())
                .collect(Collectors.toList());
    }

    private List<CountrySummary> getCountrySummariesWithMedalCount(List<Country> countries) {
        List<CountrySummary> countrySummaries = new ArrayList<>();
        for (var country : countries) {
            var goldMedalCount = goldMedalsRepository.countByCountry(country.getName());// TODO: get count of medals for the given country
            countrySummaries.add(new CountrySummary(country, goldMedalCount));
        }
        return countrySummaries;
    }
}
