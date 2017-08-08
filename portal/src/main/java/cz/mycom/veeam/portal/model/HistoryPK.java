package cz.mycom.veeam.portal.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author dursik
 */
public class HistoryPK implements Serializable {
    @Id
    @Column
    private String uid;

    @Id
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryPK historyPK = (HistoryPK) o;
        return Objects.equals(uid, historyPK.uid) &&
                Objects.equals(dateCreated, historyPK.dateCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, dateCreated);
    }
}
