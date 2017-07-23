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
    private int id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String uid;

    @Column
    private String repositoryUid;

    @Column
    private Integer vmCount;

    @Column
    private Integer creditCount;

    @Column
    private Long quota;

    @Column
    private Long usedQuota;

    @Column
    private boolean vip;

    @Column
    private boolean enabled;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE)
    private List<Subtenant> subtenants = new ArrayList<>();

    @Transient
    private String password;
}
