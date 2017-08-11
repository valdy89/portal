package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author dursik
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "countries")
public class Country {
    @Id
    @Column
    private String code;

    @Column
    private String name;

    @Column
    private String nameEnglish;
}
