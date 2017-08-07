package cz.mycom.veeam.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "sub_tenants")
public class Subtenant {

    @Id
    @Column
    private String username;

    @Column(nullable = false, unique = true, updatable = false)
    private String uid;

    @Column
    private long quota;

    @Column
    private long usedQuota;

    @Column
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenantUid", foreignKey = @ForeignKey(name = "sub_tenants_tenant_fk"), updatable = false)
    @JsonIgnore
    private Tenant tenant;

    @Transient
    private String password;
}
