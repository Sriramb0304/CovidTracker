package com.example.covidtracker.Model;


import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;


//Model class to store current and alltime covid data
@Data
@RedisHash("CovidData")
public class CovidData {
    @Id
    @Indexed
    private int Id;
    private int totalConfirmedData;
    private int totalRecoveredData;
    private int totalDeceasedData;
    private int dailyCases;
    private int dailyDeceased;

}
