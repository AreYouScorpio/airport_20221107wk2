package hu.webuni.airport.model;

import lombok.*;
import org.hibernate.envers.RevisionType;

import java.util.Date;

//@Getter
//@Setter
//@Data-ban benne van sok minden
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryData<T>{

    private T data;
    private RevisionType revType;
    private int revision;
    private Date date;




}

