package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * @author dursik
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class Tenant {

    @Id
    @Column
    private String username;

    @Column
    private String uid;

    @Column
    private boolean enabled;

    @Column
    private Integer vm;

    @Column
    private long quota = 0;

    @Column
    private String surname;

    @Column
    private String firstname;

    @Transient
    private long usedQuota = 0;
}
