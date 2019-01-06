package cz.mycom.veeam.portal.service;

import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.*;
import cz.mycom.veeam.portal.repository.CloseDayRepository;
import cz.mycom.veeam.portal.repository.OrderRepository;
import cz.mycom.veeam.portal.repository.TenantHistoryRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import lv.konts.ecomm.merchant.Merchant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author dursik
 */
@Slf4j
@Service
@Transactional
public class MerchantService implements InitializingBean {
    public static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private TenantHistoryRepository tenantHistoryRepository;
    @Autowired
    private VeeamService veeamService;
    @Autowired
    private CloseDayRepository closeDayRepository;

    private Merchant merchant;

    public void afterPropertiesSet() {
        try {
            Properties merchantProperties = new Properties();
            merchantProperties.load(new FileInputStream("c:/app/conf/merchant.properties"));
            merchant = new Merchant(merchantProperties);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean checkTransaction(HttpServletRequest request, String transId) throws Exception {
        Order order = orderRepository.findByTransId(transId);
        if (order == null) {
            throw new RuntimeException("Neexistujíci transakce.");
        }

        String result = merchant.getTransResult(order.getTransId(), getClientIp(request));
        log.debug(result);
        order.setResult(result);

        BufferedReader rdr = new BufferedReader(new StringReader(result));
        Map<String, String> lines = new HashMap<>();
        for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] pom = line.split(":");
            lines.put(StringUtils.trim(pom[0]), StringUtils.trim(pom[1]));
        }
        rdr.close();

        if (StringUtils.equalsIgnoreCase("OK", lines.get("RESULT"))) {
            order.setPaymentStatus(PaymentStatusEnum.Paid);
            Tenant tenant = tenantRepository.findByUid(order.getTenantUid());
            tenant.setCredit(tenant.getCredit() + order.getCredit());
            tenantRepository.save(tenant);

            if (tenant.getCredit() > 0 && !tenant.isEnabled()) {
                tenant.setEnabled(true);
                LogonSession logonSession = null;
                try {
                    logonSession = veeamService.logonSystem();
                    CloudTenant cloudTenant = veeamService.getTenant(tenant.getUid());
                    cloudTenant.setEnabled(true);
                    cloudTenant.setPassword(null);
                    veeamService.saveTenant(tenant.getUid(), cloudTenant);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    veeamService.logout(logonSession);
                }
            }

            TenantHistory tenantHistory = new TenantHistory(tenant, "Fakturace");
            tenantHistoryRepository.save(tenantHistory);
            return true;
        }
        orderRepository.delete(order);
        return false;
    }

    public String startSMSTrans(HttpServletRequest request, BigDecimal finalPrice, String descr) {
        String result = merchant.startSMSTrans(finalPrice.multiply(HUNDRED).toString(), "203", getClientIp(request), descr);
        log.debug("Result: {}", result);
        if (result.startsWith("error:")) {
            throw new RuntimeException(result);
        }
        String transId = StringUtils.trim(result.split(":")[1]);
        log.debug("TransId: " + transId);
        return transId;
    }

    @Retryable(maxAttempts = 5)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void closeDay() {
        String result = merchant.closeDay();
        log.debug("Result: {}", result);
        CloseDay closeDay = new CloseDay();
        closeDay.setDateCreated(new Date());
        closeDay.setResult(result);
        closeDayRepository.save(closeDay);
        if (result.startsWith("error:")) {
            throw new RuntimeException(result);
        }
    }

    private static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
