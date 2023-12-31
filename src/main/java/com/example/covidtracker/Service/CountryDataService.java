package com.example.covidtracker.Service;

import com.example.covidtracker.Model.CountryData;
import com.example.covidtracker.Repository.CountryDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CountryDataService{

    @Autowired
    CountryDataRepository countryDataRepo;

    //Saving the CountryData object to the database
    public void saveCountryData(CountryData countryData) {
        countryDataRepo.save(countryData);
    }

    //Fetching all country data from database
    public List<CountryData> getAllCountryData(){
        return (List<CountryData>) countryDataRepo.findAll();

         }
    //Fetching individual country data from database
    public List<CountryData> getCountry(String country){
        return  countryDataRepo.findByCountry(country);
    }
}
