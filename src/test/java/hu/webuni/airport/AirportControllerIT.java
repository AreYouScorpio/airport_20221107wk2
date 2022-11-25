package hu.webuni.airport;


import hu.webuni.airport.api.model.AirportDto;
import hu.webuni.airport.api.model.AirportDto;
import hu.webuni.airport.service.AirportService;
import hu.webuni.airport.service.InitDbService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // server indítás random porton
public class AirportControllerIT {

    private static final String BASE_URI="/api/airports";

    @Autowired
    WebTestClient webTestClient; // tudja, melyik porton fut a server a localhoston, és oda küldi a kéréseket majd a teszt idejére

    @Test
    void testThatCreatedAirportIsListed() throws Exception {
        List<AirportDto> airportsBefore = getAllAirports();
        //AirportDto newAirport = new AirportDto(5, "jdlksj" , "IGH");
        //AirportDto @Builder annotacioval keszitve ehelyett:

//      generated Dto-kon nincsen Build annotacio, itt siman lehet buildelni, emiatt cserelni kellett open api generator utan
//        AirportDto newAirport = AirportDto.builder()
//                        .id(5)
//                        .name("jdlksj")
//                        .iata("IGH")
//                        .build();

        AirportDto newAirport = new AirportDto()
                        .id(5L)
                        .name("jdlksj")
                        .iata("IGH");

        createAirport(newAirport);

        List<AirportDto>  airportsAfter = getAllAirports();

        assertThat(airportsAfter.subList(0, airportsBefore.size()))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(airportsBefore);

        assertThat(airportsAfter.get(airportsAfter.size()-1))
                .usingRecursiveComparison()
                .isEqualTo(newAirport);

    }

    private void createAirport(AirportDto newAirport) {
        webTestClient
                .post()
                .uri(BASE_URI)
                .bodyValue(newAirport)
                .exchange()
                .expectStatus()
                .isOk();
    }

    private List<AirportDto> getAllAirports() {
        List<AirportDto> responseList = webTestClient
                .get()
                .uri(BASE_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(AirportDto.class)
                .returnResult().getResponseBody();

        Collections.sort(responseList, (a1, a2) -> Long.compare(a1.getId(), a2.getId()));

        return responseList;
    }














        }
