package com.example.covidtracker.Controller;

import com.example.covidtracker.Service.CountryDataService;
import com.example.covidtracker.Service.CovidDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {


    private final CountryDataService countryDataService;
    private final CovidDataService CovidDataService;


    //The method handles get request for the homepage
    @GetMapping("/")
    public String home(Model model) {

        //Retrieves CovidData and adds it to the model.
        model.addAttribute("CovidData", CovidDataService.getCovidData(1));

        // Returns the "Home" template.
        return "Home";
    }


    //Handles GET request for country-specific data page.
    @GetMapping("/CountryData")
    public String SpecificCountryData(Model model,@RequestParam(value = "country", defaultValue = "") String country){

        //If a country is specified, retrieves data for that country and adds it to the model.
        if(!country.isEmpty()){
            model.addAttribute("allData",countryDataService.getCountry(country));
            System.out.println(countryDataService.getCountry(country));
        }

        //Otherwise, retrieves data for all countries and adds it to the model.
        else {
            model.addAttribute("allData", countryDataService.getAllCountryData());
        }

        //Returns the "CountryData" template.
        return "CountryData";
    }
}
