package hu.webuni.airport.web;


import java.util.List;

import javax.validation.Valid;

import com.querydsl.core.types.Predicate;
import hu.webuni.airport.api.model.FlightDto;
import hu.webuni.airport.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.web.bind.annotation.*;

import hu.webuni.airport.mapper.FlightMapper;
import hu.webuni.airport.model.Flight;
import hu.webuni.airport.service.FlightService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
//@RestController .. ne legyen utkozes, kikapcsoljuk
@RequestMapping("/api/flights")
public class _old_FlightController {

    @Autowired
    FlightService flightService;
    @Autowired
    FlightMapper flightMapper;
    @Autowired
    FlightRepository flightRepository;

    @PostMapping
    public FlightDto createFlight(@RequestBody @Valid FlightDto flightDto) {
        Flight flight = flightService.save(flightMapper.dtoToFlight(flightDto));
        return flightMapper.flightToDto(flight);
    }

    @PostMapping("/search")
    public List<FlightDto> searchFlights(@RequestBody FlightDto example){



        return flightMapper.flightsToDtos(flightService.findFlightsByExample(flightMapper.dtoToFlight(example)));
    }

    @GetMapping("/search")
    public List<FlightDto> searchFlights2(@QuerydslPredicate(root = Flight.class) Predicate predicate){


        return flightMapper.flightsToDtos(flightRepository.findAll(predicate));
    }

    @PostMapping("/{flightId}/pollDelay/{rate}")
    public void startDelayPolling(@PathVariable long flightId, @PathVariable long rate) { //uj timert hoz letre, letrehozo jellegu, postmapping emiatt
        flightService.startDelayPollingForFlight(flightId, rate);
    }

    @DeleteMapping("/{flightId}/pollDelay")
    public void startDelayPolling(@PathVariable long flightId) { //torol egy timert
        flightService.stopDelayPollingForFlight(flightId);
    }

}
