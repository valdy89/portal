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
@Table(name = "tenant_history")
@IdClass(HistoryPK.class)
public class TenantHistory {
    public TenantHistory() {
    }

    public TenantHistory(Tenant tenant, String modifier) {
        this.modifier = modifier;
        this.userId = tenant.getUserId();
        this.dateCreated = new Date();
        this.repositoryUid = tenant.getRepositoryUid();
        this.vmCount = tenant.getVmCount();
        this.workstationCount = tenant.getWorkstationCount();
        this.serverCount = tenant.getServerCount();
        this.usedQuota = tenant.getUsedQuota();
        this.quota = tenant.getQuota();
    }

    @Id
    @Column
    private int userId;

    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

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
    private String modifier;
}
