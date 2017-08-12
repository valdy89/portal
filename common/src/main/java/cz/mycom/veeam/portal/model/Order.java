package cz.mycom.veeam.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author dursik
 */
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String tenantUid;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column
    private Integer invoiceId;

    @Column
    private Integer proformaId;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private PaymentStatusEnum paymentStatus;

    @Column
    private BigDecimal price;

    @Column
    private int credit;

    @Column
    private String documentNumber;
}
