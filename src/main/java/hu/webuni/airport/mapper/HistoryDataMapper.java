package hu.webuni.airport.mapper;

import hu.webuni.airport.api.model.HistoryDataAirportDto;
import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.HistoryData;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryDataMapper {
     HistoryDataAirportDto airportHistoryDataToDto(HistoryData<Airport> airportHistoryData);
}
