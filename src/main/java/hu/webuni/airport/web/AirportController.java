package hu.webuni.airport.web;

import hu.webuni.airport.api.AirportControllerApi;
import hu.webuni.airport.api.model.AirportDto;
import hu.webuni.airport.api.model.HistoryDataAirportDto;
import hu.webuni.airport.mapper.AirportMapper;
import hu.webuni.airport.mapper.HistoryDataMapper;
import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.HistoryData;
import hu.webuni.airport.repository.AirportRepository;
import hu.webuni.airport.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor //szeretnenk bele injektalni final mezoket, emiatt kell
public class AirportController implements AirportControllerApi {

    private final NativeWebRequest nativeWebRequest;
    private final AirportService airportService;
    private final AirportRepository airportRepository;
    private final AirportMapper airportMapper;
    private final HistoryDataMapper historyDataMapper;


    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.of(nativeWebRequest);
    }

    @Override
    public ResponseEntity<AirportDto> createAirport(AirportDto airportDto) {
        Airport airport = airportService.save(airportMapper.dtoToAirport(airportDto));
        return ResponseEntity.ok(airportMapper.airportToDto(airport)); //wrapping into ResponseEntity.ok()
    }

    @Override
    public ResponseEntity<Void> deleteEmployee(Long id) {
        airportService.delete(id);
        return ResponseEntity.ok().build(); // ha nincs body, kell build() is a vegere
    }

    @Override
    public ResponseEntity<AirportDto> getById(Long id) {
        Airport airport = airportService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(airportMapper.airportSummaryToDto(airport));
    }

    @Override
    public ResponseEntity<List<HistoryDataAirportDto>> getHistoryById(Long id) {

        // ki kell szedni a history miatt, kulonben a DEL history nem latszik, m ertelemszeruen nem lesz ilyen ID, ami mar torolt, mikozben a historyban ugye kell, h legyen..
       // Airport airport = airportService.findById(id)
         //       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<HistoryData<Airport>> airports = airportService.getAirportHistory(id);

        //ezt is ResponsEntity-be:
        //List<HistoryData<AirportDto>> airportDtosWithHistory = new ArrayList<>();
        List<HistoryDataAirportDto> airportDtosWithHistory = new ArrayList<>(); //name changed

        airports.forEach(airportHistoryData -> {
            airportDtosWithHistory.add(
                    historyDataMapper.airportHistoryDataToDto(airportHistoryData));
                    //ez mar nem kell, helyette keszult egy historyDataMapper
//                    new HistoryData<>(
//                    //airportMapper.airportSummaryToDto(airportHistoryData.getData()), ---kapcs betolteshez teszteljuk--->
//                    airportMapper.airportToDto(airportHistoryData.getData()),
//                    airportHistoryData.getRevType(),
//                    airportHistoryData.getRevision(),
//                    airportHistoryData.getDate()
//            ));
        });


        return ResponseEntity.ok(airportDtosWithHistory);
    }

    @Override
    public ResponseEntity<AirportDto> modifyAirport(Long id, AirportDto airportDto) {
        Airport airport = airportMapper.dtoToAirport(airportDto);
        airport.setId(id); // hogy tudjunk módosítani azonos iata-jút a uniqecheck ellenére
        try {
            hu.webuni.airport.api.model.AirportDto savedAirportDto = airportMapper.airportToDto(airportService.update(airport));

            return ResponseEntity.ok(savedAirportDto);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
