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
@Table(name = "users")
public class User {
    @Id
    @Column
    private int id;

    @Column(unique = true)
    private String username;

    @Column
    private String surname;

    @Column
    private String firstname;

    @Column
    private String mobile;

    @Column
    private String postalCode;

    @Column
    private String companyName;

    @Column
    private String street;

    @Column
    private String city;

    @Column
    private String country;

    @Column
    private String ico;

    @Column
    private String dic;

    @Column
    private boolean vip;

    @Column
    @JsonIgnore
    private boolean enabled;

    @Column
    @JsonIgnore
    private String password;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private Date dateCreated;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Tenant tenant;
}
