package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private String username;

    @Column
    private String surname;

    @Column
    private String firstname;

    @Column
    private String mobile;

    @Column
    private String companyname;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String country;

    @Column
    private String ico;

    @Column
    private String dic;
}
