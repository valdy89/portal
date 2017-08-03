package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Getter
@Setter
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column
    private int userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String uid;

    @Column
    private String repositoryUid;

    @Column
    private int vmCount;

    @Column
    private int serverCount;

    @Column
    private int workstationCount;

    @Column
    private int quota;

    @Column
    private int usedQuota;

    @Column
    private boolean enabled;

    @OneToOne
    @JoinColumn(name="userId")
    private User user;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE)
    private List<Subtenant> subtenants = new ArrayList<>();

    @Transient
    private String password;

    @Transient
    private Date creditDate;
}
