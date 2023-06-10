package com.example.covidtracker.Repository;

import com.example.covidtracker.models.CovidData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


//Repository class to store CovidData to the Redis database.
@Repository
public interface CovidDataRepository extends CrudRepository<CovidData,Integer> {

        //Leveraging findBy provided by spring data to get CovidData
        CovidData findById();
}
