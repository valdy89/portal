package cz.mycom.veeam.portal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

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

    @Column
    @Id
    private String uid;

    @Column(unique = true, nullable = false)
    private String username;

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
    private int credit;

    @Column
    private boolean enabled;

    @OneToOne
    @JoinColumn(name = "userId")
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Subtenant> subtenants = new ArrayList<>();

    @Transient
    private String password;

    @Transient
    private Date creditDate;

    @Transient
    private int unpaidOrders;
}
