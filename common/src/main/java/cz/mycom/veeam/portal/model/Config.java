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
@Table(name = "portal_config")
public class Config {
    @Id
    @Column
    private String name;

    @Column
    private String value;
}
