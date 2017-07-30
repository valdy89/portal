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
public class UserHistory {
    public UserHistory() {
    }

    public UserHistory(User user, String modifier) {
        this.modifier = modifier;
        this.userId = user.getId();
        this.credit = user.getCredit();
        this.dateCreated = new Date();

    }

    @Id
    @Column
    private int userId;

    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column
    private int credit;

    @Column
    private String modifier;
}
