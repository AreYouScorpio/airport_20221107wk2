package hu.webuni.airport.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@annotation(hu.webuni.airport.aspect.LogCall) || @within(hu.webuni.airport.aspect.LogCall)") //mikor kell meghivodnia.. ha a metodusra ratesszuk a @LogCall annotaciot, akk hivodjon meg VAGY olyan tipuson belul van, amely tipuson rajta van a @LogCall annotacio (pl interface-en, v metoduson)
    public void annotationLogCall(){

    }

    //@Before("execution(* hu.webuni.airport.repository.*.*(..))")
    @Before("hu.webuni.airport.aspect.LoggingAspect.annotationLogCall()") // ez a metodus definialja a szabalyt, a pointcut-ot
    //* mind1 melyik tipusban (barmely interface, vagy osztaly),
    // * mindegy, h mi a metodus (barmely metodus),
    // (..) mindegy, milyen parameterei vannak
    public void logBefore(JoinPoint joinPoint){
        Class<?> clazz = joinPoint.getTarget().getClass();
        Class<?>[] interfaces = clazz.getInterfaces();
        //vannak-e interface-k? -> ettol fugg, milyen tipust fogok kiirni (osztalynev, vagy 0. interface tostringje)
        String type = interfaces.length == 0 ? clazz.getName() : interfaces[0].toString();

//        System.out.println(String.format("Method %s called in class %s",
//                joinPoint.getSignature(), //.getSignature melyik metodus, teljes signaturaja a metodusnak
//                type //.getTarget a cel osztaly, megkerdezzuk oszt nevet //getClass().getName() is lehetoseg, de akk generalt nevet ad
//                ));
        // system.out.format kv, akk nekem kell a sortorest is kitenni a vegere %n

                System.out.format("Method %s called in class %s%n",
                joinPoint.getSignature(), //.getSignature melyik metodus, teljes signaturaja a metodusnak
                type //.getTarget a cel osztaly, megkerdezzuk oszt nevet //getClass().getName() is lehetoseg, de akk generalt nevet ad
                );



    }

}
