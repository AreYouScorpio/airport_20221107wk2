package hu.webuni.airport.model;


import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.repository.EntityGraph;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Audited
public class Address {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private long id;

    private String city;
    private String street;
    private String zip;


}
