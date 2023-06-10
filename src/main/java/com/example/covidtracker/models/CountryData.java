package com.example.covidtracker.models;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;


//Model class to store covid data according to their country
@Data
@RedisHash(value="CountryData")
public class CountryData {

    @Id
    private String id;
    @Indexed
    private String country;
    private int latestConfirmedData;
    private int latestRecoveredData;
    private int latestDeceasedData;

}
