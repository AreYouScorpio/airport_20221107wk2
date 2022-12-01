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
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
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

    //pageable generalasa springgel, ehhez van egy metodusa springnek:
    private final PageableHandlerMethodArgumentResolver pageableResolver;


    public void configPageable(@SortDefault("id") Pageable pageable) { //org.springframework.data.domain Pageable ! //id szt rendezzuk
        //itt hazudjuk be, h hanyadik argumentum a Pageable, minidg csinalunk neki egy metodust
        //ha kesz van, letrehozhatjuk a methodParametert

    }

    @Override
    public ResponseEntity<List<AirportDto>> getAll(Boolean full, Integer page, Integer size, String sort) {

        // Optional bool helyett Boolean van, emiatt ezt valtoztatni:
//        boolean isFull = full.orElse(false);
        boolean isFull = full == null ? false : full; //ha null lenne, akk false-nak tekintjuk

        // kotelezo kiv kezelest ker: Method method = this.getClass().getMethod("configPageable", Pageable.class); //reflect.method // megetetni vele a legyartott configPageable-t // argumentumlistanak pedig itt a Pageble.class lesz megadva
        //igy ugyanez try/catch-be pakolva:
        String pageableConfigurerMethodName = "configPageable";
        //kitesszuk egy metodusba:
//        Method method;
//        try {
//            method = this.getClass().getMethod(pageableConfigurerMethodName, Pageable.class); //reflect.method // megetetni vele a legyartott configPageable-t // argumentumlistanak pedig itt a Pageble.class lesz megadva
//        } catch (NoSuchMethodException | SecurityException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//        //pageable-t viszont letre kell hoznunk, legyartani, ehhez fent injektaljuk a Resolvert:
//        MethodParameter methodParameter = new MethodParameter(null, 0); // a Pageable tipusu metodus argumentum melyik controller metodusnak a hanyadik argumentuma, azt irja le .. meg fogja nezni, h az adott argumentumon van-e egyeb annotacio, pl mi ratettuk a @SortDefault("id")-t az old controller @GetMapping-jara, h id szt sort-olja, ehhez kell neki, h tudja, melyik argumentumon kell keresnie az (esetleges) annotaciokat
//        // ez egy metodust es egy indexet var
//        // ehhez metodust kell gyartani Method method..
//        ModelAndViewContainer mavContainer = null; //ezt nem hasznalja tenylegesen a Spring Data, h kinyerje a pageable infokat, ezert nullra allithatjuk
//        WebDataBinderFactory binderFactory = null; //ezt nem hasznalja tenylegesen a Spring Data, h kinyerje a pageable infokat, ezert nullra allithatjuk
//        Pageable pageable = pageableResolver.resolveArgument(methodParameter, mavContainer, nativeWebRequest, binderFactory); // nativeWebRequest egybol hasznalhato az injektalas miatt, a masik 3 le kell gyartani
//        //viszont nincs olyan metodusunk, aminek lenne Pageable bemeno parametere, ezert csinalunk egy ilyen metodust configPageable neven
//        //org.springframework.data.domain Pageable !
        Pageable pageable = createPageable(pageableConfigurerMethodName);


        // eredetileg ennyi: return airportMapper.airportsToDtos(airportService.findAll());
        // de most kíváncsiak vagyunk, mi toltodik be mar automatikusan DB-bol
        //List<Airport> airports = airportService.findAll(); //--> ehelyett meg a repobol mar az uj lekerest hivjuk meg es nem a service-bol, igy gyorsabb, ugyis csak athivna
        // tesztelesnek volt ez:
        // List<Airport> airports = airportRepository.findAll();

        List<Airport> airports =
                isFull

//                ? airportRepository.findAllWithAddressAndDepartures()  // ez N*M sort küldene vissza, ha N arrival es M departure van, ezert inkabb airportservice findallwithrelationshipet irunk
                        ? airportService.findaAllWithRelationships(pageable)
                        : airportRepository.findAll(pageable).getContent(); // ezt atallitjuk lazy-re Airportban - @ManyToOne(fetch=FetchType.LAZY)
//getContent kell h lista legyen, m a Page of Entity, entitas egy oldalat adna vissza, plusz csomo mas infot, pl hogy hany db van osszesen, stb, ezert kell csak ez a content

        // es mi akkor kenyszerul betoltodni, amikor a mapstruct mar a gettereket hivogatja:

        // tesztelesnek volt ez: return airportMapper.airportsToDtos(airports);


        List<AirportDto> resultList = isFull
                ? airportMapper.airportsToDtos(airports) // full
                : airportMapper.airportSummariesToDtos(airports);
        //return resultList; // "summary" verzio  // kiemelni es akkor tudjuk szinten wrappelni egy ok-ba
        return ResponseEntity.ok(resultList); // "summary" verzio


    }

    private Pageable createPageable(String pageableConfigurerMethodName) {
        Method method;
        try {
            method = this.getClass().getMethod(pageableConfigurerMethodName, Pageable.class); //reflect.method // megetetni vele a legyartott configPageable-t // argumentumlistanak pedig itt a Pageble.class lesz megadva
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //pageable-t viszont letre kell hoznunk, legyartani, ehhez fent injektaljuk a Resolvert:
        MethodParameter methodParameter = new MethodParameter(method, 0); // a Pageable tipusu metodus argumentum melyik controller metodusnak a hanyadik argumentuma, azt irja le .. meg fogja nezni, h az adott argumentumon van-e egyeb annotacio, pl mi ratettuk a @SortDefault("id")-t az old controller @GetMapping-jara, h id szt sort-olja, ehhez kell neki, h tudja, melyik argumentumon kell keresnie az (esetleges) annotaciokat
        // ez egy metodust es egy indexet var
        // ehhez metodust kell gyartani Method method..
        ModelAndViewContainer mavContainer = null; //ezt nem hasznalja tenylegesen a Spring Data, h kinyerje a pageable infokat, ezert nullra allithatjuk
        WebDataBinderFactory binderFactory = null; //ezt nem hasznalja tenylegesen a Spring Data, h kinyerje a pageable infokat, ezert nullra allithatjuk
        Pageable pageable = pageableResolver.resolveArgument(methodParameter, mavContainer, nativeWebRequest, binderFactory); // nativeWebRequest egybol hasznalhato az injektalas miatt, a masik 3 le kell gyartani
        //viszont nincs olyan metodusunk, aminek lenne Pageable bemeno parametere, ezert csinalunk egy ilyen metodust configPageable neven
        //org.springframework.data.domain Pageable !
        return pageable;
    }

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
