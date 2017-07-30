package cz.mycom.veeam.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.event.TransactionalEventListener;

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
    private Long quota;

    @Column
    private Long usedQuota;

    @Column
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", foreignKey = @ForeignKey(name = "sub_tenants_tenant_fk"), updatable = false)
    @JsonIgnore
    private Tenant tenant;

    @Transient
    private String password;
}
