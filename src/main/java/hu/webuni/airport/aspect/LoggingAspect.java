package hu.webuni.airport.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {


    @Before("execution(* hu.webuni.airport.repository.*.*(..))") //* mind1 melyik tipusban (barmely interface, vagy osztaly), * mindegy, h mi a metodus (barmely metodus), (..) mindegy, milyen parameterei vannak
    public void logBefore(JoinPoint joinPoint){
        System.out.println(String.format("Method %s called in class %s",
                joinPoint.getSignature(), //.getSignature melyik metodus, teljes signaturaja a metodusnak
                joinPoint.getTarget().getClass().getName() //.getTarget a cel osztaly, megkerdezzuk oszt nevet
                ));
    }

}
