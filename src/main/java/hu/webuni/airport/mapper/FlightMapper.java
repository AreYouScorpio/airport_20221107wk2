package hu.webuni.airport.mapper;

import java.util.List;

import hu.webuni.airport.api.model.AirportDto;
import hu.webuni.airport.model.Airport;
import org.mapstruct.Mapper;

import hu.webuni.airport.api.model.FlightDto;
import hu.webuni.airport.model.Flight;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    Flight dtoToFlight(FlightDto flightDto);

    FlightDto flightToDto(Flight flight);
   // List<FlightDto> flightsToDtos(List<Flight> flight);listás nem működött postman-ben, csak ha CSAK az iterable marad benne

    List<FlightDto> flightsToDtos(Iterable<Flight> findAll);

    @Mapping(target = "address", ignore = true) // address lazy esetben ha address-t probalna kitolteni, ignore = true legyen.. ez csak a summary mapper
    @Mapping(target = "departures", ignore = true)
    @Mapping(target = "arrivals", ignore = true)
    AirportDto airportSummaryToDto(Airport airport);

}