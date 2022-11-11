package hu.webuni.airport.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import hu.webuni.airport.aspect.LogCall;
import hu.webuni.airport.model.QFlight;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import hu.webuni.airport.model.Airport;
import hu.webuni.airport.model.Flight;
import hu.webuni.airport.repository.AirportRepository;
import hu.webuni.airport.repository.FlightRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@LogCall
public class FlightService {

    // vagy autowired, vagy
    // 	private final AirportRepository airportRepository;
    //	private final FlightRepository flightRepository;
    // injektálás.. konstruktorinjektálás.. lombok.. a @RequiredArgsConstructor a private final esetén
    // olyan konstruktort fog legenerálni, ami az összes private final tagváltozóját elfogadja argumentumként
    // ha egy konstruktort talál, annak minden változóját injektálni próbálja

    @Autowired
    AirportRepository airportRepository;
    @Autowired
    FlightRepository flightRepository;

    @Autowired
    DelayService delayService;

    @Autowired
    TaskScheduler taskScheduler;

    private Map<Long, ScheduledFuture<?>> delayPollerJobs = new ConcurrentHashMap<>(); // mert tobbszalon meghivodhat, ezert concurrent hashmap

    @Transactional
    public Flight save(Flight flight) {
        //a takeoff/landing airportból csak az id-t vesszük figyelembe, már létezniük kell
        flight.setTakeoff(airportRepository.findById(flight.getTakeoff().getId()).get());
        flight.setLanding(airportRepository.findById(flight.getLanding().getId()).get());
        return flightRepository.save(flight);
    }

    public List<Flight> findFlightsByExample(Flight example) {
        long id = example.getId();
        String flightNumber = example.getFlightNumber();
        String takeoffIata = null;
        Airport takeoff = example.getTakeoff();
        if (takeoff != null)
            takeoffIata = takeoff.getIata();
        LocalDateTime takeoffTime = example.getTakeoffTime();

        //Specification<Flight> spec = Specification.where(null); // üres Specification, ami semmire nem szűr

        // a FlightSpecifications feltételeit ide írjuk bele közvetlen --->

        ArrayList<Predicate> predicates = new ArrayList<com.querydsl.core.types.Predicate>();

        QFlight flight = QFlight.flight;

        if (id > 0) {

            // spec = spec.and(FlightSpecifications.hasId(id));
            predicates.add(flight.id.eq(id));
        }

        if (StringUtils.hasText(flightNumber)) // SpringFramework-ös StringUtils
            // spec = spec.and(FlightSpecifications.hasFlightNumber(flightNumber));
            predicates.add(flight.flightNumber.startsWithIgnoreCase(flightNumber));

        if (StringUtils.hasText(takeoffIata)) // SpringFramework-ös StringUtils
            //spec = spec.and(FlightSpecifications.hasTakeoffIata(takeoffIata));
            predicates.add(flight.takeoff.iata.startsWith(takeoffIata));

        if (takeoffTime != null) // SpringFramework-ös StringUtils
        //spec = spec.and(FlightSpecifications.hasTakeoffTime(takeoffTime));
        {
            LocalDateTime startOfDay =
                    LocalDateTime.of(takeoffTime.toLocalDate(),
                            LocalTime.MIDNIGHT);
            predicates.add(flight.takeoffTime.between(startOfDay, startOfDay.plusDays(1)));

        }


        //return flightRepository.findAll(spec, Sort.by("id"));
        return Lists.newArrayList(flightRepository.findAll(ExpressionUtils.allOf(predicates)));
    }

//      TOROLVE ES UJ IRVA
    // -- vegul toroltuk, m hosszu a tranzakcio, lassu, a getDelay-ek miatt
//    @Transactional //mert modosit a flight-okon
//    @Scheduled(cron="*/30 * * * * *")
//    public void updateDelays() {
//        System.out.println("updateDelays called");
//        flightRepository.findAll().forEach(flight -> flight.setDelayInSec(delayService.getDelay(flight.getId())));
//    }
// -----> az uj:

    //@Transactional //mert modosit a flight-okon -- vegul toroltuk, m hosszu a tranzakcio, lassu, a getDelay-ek miatt
    //@Scheduled(cron = "*/15 * * * * *") //15mpenkent
    //@Scheduled(cron = "0 0  * * * *") //minden oraban - ez a fixedrate pollinghoz volt
    //@Scheduled(cron = "0 0  * * * *") //minden oraban - ez volt a polling start/stop elso verziohoz
    //az alabbi ket scheduled beallitast a cache-hez kikapcsoljuk, ne firkalja tele a consolet:
    //@Scheduled(cron = "*/5 * * * * *") //
    //@SchedulerLock(name = "updateDelays") // schedLock annotacio, fusson, de csak ha az updateDelays nevu lock, ami mar DB-ben van, nem foglalt, ill ertelemszeruen foglalja le, ha meg nincs lefoglalva
    //@Async //engedjuk raindulni a kov task-ot? debuggerben lathato threads-nel , enelkul csak scheduling-1 fut, ezzel pedig a task-1, task-2, stb raindul .. ha nem Asznc fut, csak a scheduling-1 szal fut
    public void updateDelays() {
        System.out.println("updateDelays called");
        flightRepository.findAll().forEach(flight ->
        {
            updateFlightWithDelay(flight);
        });
    }

    private void updateFlightWithDelay(Flight flight) {
        flight.setDelayInSec(delayService.getDelay(flight.getId()));
        flightRepository.save(flight);
    }

    //suritett pollozas inditas es leallitas
    public void startDelayPollingForFlight(long flightId, long rate){ // cron helyett fixed rate scheduling lesz
        ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> {
            Optional<Flight> flightOptional = flightRepository.findById(flightId);
            if (flightOptional.isPresent())
                updateFlightWithDelay(flightOptional.get());
        }, rate);
        stopDelayPollingForFlight(flightId); // leallitani, nehogy meg egyet inditsunk ref nelkul, ha nem lett leallitva elozo
        delayPollerJobs.put(flightId, scheduledFuture);
    }

    public void stopDelayPollingForFlight(long flightId){
        ScheduledFuture<?> scheduledFuture = delayPollerJobs.get(flightId);
        if(scheduledFuture!=null)
            scheduledFuture.cancel(false); // ha epp futasban van, akarjuk-e megszakitani, azt nem akarjuk
    }


//    // dummy :)
//    @Scheduled(cron = "*/10 * * * * *")
//    public void dummy(){
//        try {
//            Thread.sleep(8000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("dummy called");
//    }


}
