package cz.mycom.veeam.portal.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * @author dursik
 */
public class HistoryPK implements Serializable {
    @Id
    @Column
    private int userId;

    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
