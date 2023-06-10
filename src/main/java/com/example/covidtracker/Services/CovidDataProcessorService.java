package com.example.covidtracker.Services;

import com.example.covidtracker.models.CountryData;
import com.example.covidtracker.models.CovidData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;

@Service
public class CovidDataProcessorService {

    @Autowired
    CountryDataService countryDataService;

    @Autowired
    CovidDataService covidDataService;

    public final String VIRUS_CONFIRMED_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    public final String VIRUS_DECEASED_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

    //The @PostConstruct annotation ensures that this method is called after the bean has been initialized
    @PostConstruct
    public void fetchVirusData() throws IOException, InterruptedException {

        //Flushing RedisDB on restart to prevent it from writing multiple copies of the same data
         try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.flushDB();
        }

        // Initializing variables for storing total cases, recoveries and deaths
        int count=1;
        int totalConfirmedCases=0;
        int totalRecoveredCases;
        int totalDeceasedCases=0;
        int dailyCases=0;
        int dailyDeceasedCases=0;

        //Calling the getCsvRecordIterator method the URLs to convert the data into a parsable format.
        // The method returns an iterable which is stored in an Iterator
        Iterator<CSVRecord> confirmedRecords = getCsvRecordIterator(VIRUS_CONFIRMED_URL);
        Iterator<CSVRecord> deceasedRecords = getCsvRecordIterator(VIRUS_DECEASED_URL);

        // Initializing variables for storing current and next record
        CSVRecord currentConfirmedRecord=confirmedRecords.next();
        CSVRecord nextConfirmedRecord;

        CSVRecord currentDeceasedRecord=deceasedRecords.next();
        CSVRecord nextDeceasedRecord;

        CovidData covidData=new CovidData();

        // Iterating through confirmed records
        while (confirmedRecords.hasNext()) {

            nextConfirmedRecord=confirmedRecords.next();
            nextDeceasedRecord=deceasedRecords.next();

            CountryData countryData =new CountryData();

            // Getting the latest cases for the current country
            int latestTotalConfirmedCases = Integer.parseInt(currentConfirmedRecord.get(currentConfirmedRecord.size() - 1));

            int latestTotalDeceasedCases = Integer.parseInt(currentDeceasedRecord.get(currentDeceasedRecord.size() - 1));

            int latestDailyCases=Integer.parseInt(currentConfirmedRecord.get(currentConfirmedRecord.size() - 1))-Integer.parseInt(currentConfirmedRecord.get(currentConfirmedRecord.size() - 2));

            int latestDeceasedCases= Integer.parseInt(currentDeceasedRecord.get(currentDeceasedRecord.size() - 1))- Integer.parseInt(currentDeceasedRecord.get(currentDeceasedRecord.size() - 2));

            String country = currentConfirmedRecord.get("Country/Region");

            //Checking if the next country is also the same and if it is then adding all the covid data till a new country is encountered
            while(currentConfirmedRecord.get("Country/Region").equals(nextConfirmedRecord.get("Country/Region"))){

                latestTotalConfirmedCases =latestTotalConfirmedCases+Integer.parseInt(nextConfirmedRecord.get(currentConfirmedRecord.size() - 1));

                latestTotalDeceasedCases =latestTotalDeceasedCases+Integer.parseInt(currentDeceasedRecord.get(currentDeceasedRecord.size() - 1));

                latestDailyCases=latestDailyCases+Integer.parseInt(currentConfirmedRecord.get(currentConfirmedRecord.size() - 1))-Integer.parseInt(currentConfirmedRecord.get(currentConfirmedRecord.size() - 2));

                latestDeceasedCases=latestDeceasedCases+Integer.parseInt(currentDeceasedRecord.get(currentDeceasedRecord.size() - 1))- Integer.parseInt(currentDeceasedRecord.get(currentDeceasedRecord.size() - 2));


                currentConfirmedRecord=nextConfirmedRecord;
                nextConfirmedRecord=confirmedRecords.next();

                currentDeceasedRecord=nextDeceasedRecord;
                nextDeceasedRecord=deceasedRecords.next();
            }

            //Setting all the obtained data to the CountryData object
            countryData.setId(String.valueOf(count++));
            countryData.setCountry(country);
            countryData.setLatestConfirmedData(latestTotalConfirmedCases);
            countryData.setLatestDeceasedData(latestTotalDeceasedCases);
            countryData.setLatestRecoveredData(latestTotalConfirmedCases-latestTotalDeceasedCases);

            //Saving the CountryData object to the CountryDataRepository
            countryDataService.saveCountryData(countryData);

            currentConfirmedRecord=nextConfirmedRecord;
            currentDeceasedRecord=nextDeceasedRecord;

            //Setting all the obtained data to the CovidData object
            totalConfirmedCases=totalConfirmedCases+latestTotalConfirmedCases;
            totalDeceasedCases=totalDeceasedCases+latestTotalDeceasedCases;
            totalRecoveredCases=totalConfirmedCases-totalDeceasedCases;
            dailyCases=dailyCases+latestDailyCases;
            dailyDeceasedCases=dailyDeceasedCases+latestDeceasedCases;

            covidData.setId(1);
            covidData.setTotalConfirmedData(totalConfirmedCases);
            covidData.setTotalDeceasedData(totalDeceasedCases);
            covidData.setTotalRecoveredData(totalRecoveredCases);
            covidData.setDailyCases(dailyCases);
            covidData.setDailyDeceased(dailyDeceasedCases);

            //Saving the CovidData object to the CovidDataRepository
            covidDataService.saveCovidData(covidData);
        }


    }





    /* This method retrieves a CSV file from the given URL using an HTTP GET request and
     returns an Iterator of CSVRecord objects representing the rows of the CSV file.*/
    public Iterator<CSVRecord> getCsvRecordIterator(String url) throws IOException, InterruptedException {

        // Create a new HttpClient to make the GET request
        HttpClient httpClient= HttpClient.newHttpClient();

        // Build the GET request with a 10-second timeout
        HttpRequest request= HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        // Send the GET request and retrieve the response body as a String
        HttpResponse<String>httpResponse=httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Create a StringReader for the CSV file contents
        StringReader csvReader= new StringReader(httpResponse.body());

        // Create a CSVFormat with header and allowing missing column names
        Iterator<CSVRecord> csvRecord = CSVFormat.Builder.create()
                .setHeader()
                .setAllowMissingColumnNames(true)
                .build()
                .parse(csvReader).iterator();

        // Parse the CSV file and return an Iterator of CSVRecord objects
        return csvRecord;

    }
}

