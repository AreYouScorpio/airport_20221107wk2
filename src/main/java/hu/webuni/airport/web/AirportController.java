package hu.webuni.airport.web;

import hu.webuni.airport.api.AirportControllerApi;
import hu.webuni.airport.api.model.AirportDto;
import hu.webuni.airport.api.model.HistoryDataAirportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor //szeretnenk bele injektalni final mezoket, emiatt kell
public class AirportController implements AirportControllerApi {

    private final NativeWebRequest nativeWebRequest;


    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.of(nativeWebRequest);
    }

//    @Override
//    public ResponseEntity<AirportDto> createAirport(AirportDto airportDto) {
//        return AirportControllerApi.super.createAirport(airportDto);
//    }
//
//    @Override
//    public ResponseEntity<Void> deleteEmployee(Long id) {
//        return AirportControllerApi.super.deleteEmployee(id);
//    }
//
//    @Override
//    public ResponseEntity<AirportDto> getById(Long id) {
//        return AirportControllerApi.super.getById(id);
//    }
//
//    @Override
//    public ResponseEntity<List<HistoryDataAirportDto>> getHistoryById(Long id) {
//        return AirportControllerApi.super.getHistoryById(id);
//    }
//
//    @Override
//    public ResponseEntity<AirportDto> modifyAirport(Long id, AirportDto airportDto) {
//        return AirportControllerApi.super.modifyAirport(id, airportDto);
//    }
}
