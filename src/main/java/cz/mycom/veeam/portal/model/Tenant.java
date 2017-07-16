package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

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

    @Column(name = "vm_count")
    private int vmCount;

    @Column
    private String surname;

    @Column
    private String firstname;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name = "subtenant_count")
    private int subtenantCount;

    @Transient
    private long usedQuota;

    @Transient
    private String name;

    @Transient
    private String description;

    @Transient
    private long quota;
}
