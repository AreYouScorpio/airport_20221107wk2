package hu.webuni.airport.web;

import com.querydsl.core.Query;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import hu.webuni.airport.api.FlightControllerApi;
import hu.webuni.airport.api.model.FlightDto;
import hu.webuni.airport.mapper.FlightMapper;
import hu.webuni.airport.model.Flight;
import hu.webuni.airport.repository.FlightRepository;
import hu.webuni.airport.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FlightController implements FlightControllerApi {


    private final NativeWebRequest nativeWebRequest;

    @Autowired
    FlightService flightService;
    @Autowired
    FlightMapper flightMapper;
    @Autowired
    FlightRepository flightRepository;

    @Autowired
    QuerydslPredicateArgumentResolver predicateResolver;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.of(nativeWebRequest);
    }

    @Override
    public ResponseEntity<FlightDto> createFlight(FlightDto flightDto) {
        Flight flight = flightService.save(flightMapper.dtoToFlight(flightDto));
        return ResponseEntity.ok(flightMapper.flightToDto(flight));
    }

    @Override
    public ResponseEntity<List<FlightDto>> searchFlights(FlightDto example) { //atnevezni example-re, amit bedobott
        return ResponseEntity.ok(flightMapper.flightsToDtos(flightService.findFlightsByExample(flightMapper.dtoToFlight(example))));
    }

    @Override
    public ResponseEntity<Void> startDelayPolling(Long flightId, Long rate) {
        flightService.startDelayPollingForFlight(flightId, rate);
        return ResponseEntity.ok().build(); //ures ok-t buildelni
    }

    @Override
    public ResponseEntity<Void> stopDelayPolling(Long flightId) {
        flightService.stopDelayPollingForFlight(flightId);
        return ResponseEntity.ok().build(); //ures ok-t buildelni

    }


    public void configurePredicate(@QuerydslPredicate(root = Flight.class) Predicate predicate) {

    }

    @Override
    public ResponseEntity<List<FlightDto>> searchFlights2(Long id, String flightNumber, String takeoffIata, List<String> takeoffTime) {
//        return flightMapper.flightsToDtos(flightRepository.findAll(predicate)); // atemeljuk, es legyartjuk a predicate-et
        Predicate predicate = createPredicate("configurePredicate");  // a kamu metodusnevet atadom neki
        return ResponseEntity.ok(flightMapper.flightsToDtos(flightRepository.findAll(predicate))); // atemeljuk, es legyartjuk a predicate-et
    }

    private Predicate createPredicate(String configMethodName) {
        Method method;
        try {
            method = this.getClass().getMethod(configMethodName, Predicate.class); //reflect.method // megetetni vele a legyartott configPageable-t // argumentumlistanak pedig itt a Pageble.class lesz megadva

            //pageable-t viszont letre kell hoznunk, legyartani, ehhez fent injektaljuk a Resolvert:
            MethodParameter methodParameter = new MethodParameter(method, 0); // a Pageable tipusu metodus argumentum melyik controller metodusnak a hanyadik argumentuma, azt irja le .. meg fogja nezni, h az adott argumentumon van-e egyeb annotacio, pl mi ratettuk a @SortDefault("id")-t az old controller @GetMapping-jara, h id szt sort-olja, ehhez kell neki, h tudja, melyik argumentumon kell keresnie az (esetleges) annotaciokat
            // ez egy metodust es egy indexet var
            // ehhez metodust kell gyartani Method method..
            ModelAndViewContainer mavContainer = null; //ezt nem hasznalja tenylegesen a Spring Data, h kinyerje a pageable infokat, ezert nullra allithatjuk
            WebDataBinderFactory binderFactory = null; //ezt nem hasznalja tenylegesen a Spring Data, h kinyerje a pageable infokat, ezert nullra allithatjuk
            return (Predicate) predicateResolver.resolveArgument(methodParameter, mavContainer, nativeWebRequest, binderFactory); // nativeWebRequest egybol hasznalhato az injektalas miatt, a masik 3 le kell gyartani
            //viszont nincs olyan metodusunk, aminek lenne Pageable bemeno parametere, ezert csinalunk egy ilyen metodust configPageable neven
            //org.springframework.data.domain Pageable !
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
