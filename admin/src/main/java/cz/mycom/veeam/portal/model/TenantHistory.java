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
        this.uid = tenant.getUid();
        this.dateCreated = new Date();
        this.repositoryUid = tenant.getRepositoryUid();
        this.vmCount = tenant.getVmCount();
        this.workstationCount = tenant.getWorkstationCount();
        this.serverCount = tenant.getServerCount();
        this.usedQuota = tenant.getUsedQuota();
        this.quota = tenant.getQuota();
        this.credit = tenant.getCredit();
    }

    @Id
    @Column
    private String uid;

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
    private long quota;

    @Column
    private long usedQuota;

    @Column
    private int credit;

    @Column
    private String modifier;

    @Column
    private String note;
}
