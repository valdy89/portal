package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author dursik
 */
@Getter
@Setter
@Entity
@Table(name = "close_day")
public class CloseDay {
    @Id
    @Column
    private int id;

    @Column
    private String result;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
}
