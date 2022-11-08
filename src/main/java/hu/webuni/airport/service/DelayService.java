package hu.webuni.airport.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DelayService {

    private Random random = new Random();

    //szimulaljuk, h ez egy hosszabb lekerdezes, pl egy partner rendszert hivunk, ahol live adatok vannak a repulesrol, egy hosszan tarto muvelet
    public int getDelay(long flightId) {
        System.out.println("getDelay called");
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
        }
        return random.
                nextInt(0, 1800);
    }

}
