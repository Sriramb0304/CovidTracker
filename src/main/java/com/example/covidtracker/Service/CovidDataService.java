package com.example.covidtracker.Service;

import com.example.covidtracker.Model.CovidData;
import com.example.covidtracker.Repository.CovidDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CovidDataService {

    @Autowired
    CovidDataRepository CovidDataRepository;

    //Saving CovidData object to the database
    public void saveCovidData(CovidData CovidData){
     CovidDataRepository.save(CovidData);
    }

    //Fetching the CoviddDta from the database
    public CovidData getCovidData(int i) {
        return CovidDataRepository.findById(i).get();
    }
}
