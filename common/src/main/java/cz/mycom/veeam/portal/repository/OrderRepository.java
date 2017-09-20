package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.model.PaymentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author dursik
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByTransIdIsNotNullAndInvoiceIdIsNullAndPaymentStatusIn(PaymentStatusEnum... status);
    List<Order> findByPaymentStatusIsNull();
    List<Order> findByTenantUidOrderByDateCreatedDesc(String uid);
    Order findByTenantUidAndId(String uid, Integer id);
    Order findByTransId(String transId);
    Order findByTenantUidAndTransId(String uid, String transId);
    @Query(value = "from Order where tenantUid = :tenantUid and (paymentStatus is null or paymentStatus not in (1,3,4))")
    List<Order> findUnpaid(@Param("tenantUid") String uid);
    @Query(value = "select min(proformaId) from Order where (paymentStatus is null or paymentStatus not in (1,3,4))")
    Integer findMinUnpaidProformaId();
    Order findByProformaId(Integer proformaId);
}
