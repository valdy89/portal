package cz.mycom.veeam.portal.service;

import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.idoklad.ProformaInvoice;
import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.model.PaymentStatusEnum;
import cz.mycom.veeam.portal.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@Component
@Transactional
public class AccountingService {
    @Autowired
    private VeeamService veeamService;
    @Autowired
    private MailService mailService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AccountingHelperService accountingHelperService;
    @Autowired
    private IDokladService iDokladService;
    @Autowired
    private MerchantService merchantService;


    @Scheduled(cron = "0/5 * * * * ?")
    public void checkOrders() {
        List<Order> orders = orderRepository.findByPaymentStatusIsNull();
        for (Order order : orders) {
            try {
                accountingHelperService.checkOrder(order);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                httpException("checkOrders", e);
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?")
    public void checkPaidOnline() {
        List<Order> orders = orderRepository.findByTransIdIsNotNullAndInvoiceIdIsNullAndPaymentStatusIn(PaymentStatusEnum.Paid);
        for (Order order : orders) {
            try {
                accountingHelperService.checkPaidOnline(order);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                httpException("checkPaidOnline", e);
            }
        }
    }

    //@Scheduled(cron = "0 0/10 * * * ?")
    public void checkPaidProforma() {
        List<ProformaInvoice> proformaPaid = null;
        try {
            Integer minUnpaidProformaId = orderRepository.findMinUnpaidProformaId();
            if (minUnpaidProformaId != null) {
                proformaPaid = iDokladService.getProformaPaid(minUnpaidProformaId);
            }
        } catch (Exception e) {
            mailService.sendError("idoklad checkPaidProforma", e);
        }
        if (CollectionUtils.isEmpty(proformaPaid)) {
            log.debug("No paid invoices.");
            return;
        }
        for (ProformaInvoice proformaInvoice : proformaPaid) {
            try {
                accountingHelperService.checkPaid(proformaInvoice);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                httpException("checkPaidProforma", e);
            }
        }
    }

    @Scheduled(cron = "0 0 4,8,12,16,20 * * ?")
    //@Scheduled(cron = "0 0/5 * * * ?")
    public void update() {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            List<CloudTenant> tenants = veeamService.getTenants();
            for (CloudTenant cloudTenant : tenants) {
                try {
                    accountingHelperService.update(cloudTenant);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    mailService.sendError("update", e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            mailService.sendError("Error updating: " + e.getMessage(), e);
        } finally {
            veeamService.logout(logonSession);
        }
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void checkTenants() {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            List<CloudTenant> tenants = veeamService.getTenants();
            for (CloudTenant cloudTenant : tenants) {
                try {
                    accountingHelperService.checkTenant(cloudTenant);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    mailService.sendError("checkTenant", e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            mailService.sendError("Error notifications: " + e.getMessage(), e);
        } finally {
            veeamService.logout(logonSession);
        }
    }

    @Scheduled(cron = "1 0 0 * * ?")
    public void process() {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            List<CloudTenant> tenants = veeamService.getTenants();
            for (CloudTenant cloudTenant : tenants) {
                try {
                    accountingHelperService.process(cloudTenant);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    mailService.sendError("process", e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            mailService.sendError("Error accounting: " + e.getMessage(), e);
        } finally {
            veeamService.logout(logonSession);
        }
    }

    @Scheduled(cron = "50 59 23 * * ?")
    public void closeDate() {
        try {
            merchantService.closeDay();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            mailService.sendError("Error close day: " + e.getMessage(), e);
        }
    }

    private void httpException(String message, Exception e) {
        mailService.sendError(message, e);
        long millis = 10 * 60 * 1000;
        if (e instanceof HttpClientErrorException) {
            log.error(((HttpClientErrorException) e).getResponseBodyAsString());
            //kdyz neni zaplaceno tak neposilat  email
            if (((HttpClientErrorException) e).getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                millis = 30 * 60 * 1000;
            }
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}
