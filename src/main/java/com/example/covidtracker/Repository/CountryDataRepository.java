package com.example.covidtracker.Repository;

import com.example.covidtracker.Model.CountryData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

//Repository class to store CountryData to the Redis database.
@Repository
public interface CountryDataRepository extends CrudRepository<CountryData,String> {

    //Leveraging findBy provided by spring data to get an individual country
    List<CountryData> findByCountry(@Param("country") String country);


}
