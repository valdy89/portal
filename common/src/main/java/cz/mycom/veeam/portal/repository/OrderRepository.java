package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.model.PaymentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author dursik
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByPaymentStatusIn(PaymentStatusEnum... status);
    List<Order> findByTenantUidOrderByDateCreatedDesc(String uid);
    Order findByTenantUidAndId(String uid, Integer proformaId);
}
