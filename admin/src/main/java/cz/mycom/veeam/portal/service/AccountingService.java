package cz.mycom.veeam.portal.service;

import com.opencsv.CSVReader;
import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.idoklad.ProformaInvoice;
import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ConfigRepository configRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AccountingHelperService accountingHelperService;
    @Autowired
    private IDokladService iDokladService;


    @Scheduled(cron = "0/5 * * * * ?")
    public void checkOrders() {
        List<Order> orders = orderRepository.findByPaymentStatusIsNull();
        for (Order order : orders) {
            try {
                accountingHelperService.checkOrder(order);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                if (e instanceof HttpClientErrorException) {
                    log.error(((HttpClientErrorException) e).getResponseBodyAsString());
                }
                mailService.sendError("checkOrders", e);
            }
        }
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void checkPaid() {
        List<ProformaInvoice> proformaPaid = null;
        try {
            proformaPaid = iDokladService.getProformaPaid(orderRepository.findMinUnpaidProformaId());
        } catch (Exception e) {
            mailService.sendError("idoklad checkPaid", e);
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
                mailService.sendError("checkPaid", e);
            }
        }
    }

    @Scheduled(cron = "1 0 0 * * ?")
    public void process() {
        Map<String, Integer[]> countMap = new HashMap<>();

        File csvFile = new File(configRepository.getOne("csv.path").getValue(), "VeeamCloudUsageReport.csv");
        if (csvFile.exists() && csvFile.canRead()) {

            try {
                CSVReader csvReader = new CSVReader(new FileReader(csvFile), ';');
                String[] header = csvReader.readNext();
                String[] line = null;
                while ((line = csvReader.readNext()) != null) {
                    countMap.put(line[0], new Integer[]{Integer.parseInt(line[1]), Integer.parseInt(line[2]), Integer.parseInt(line[3])});
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                mailService.sendError("Error CSV parse: " + e.getMessage(), e);
            }
        }

        LogonSession logonSession = veeamService.logonSystem();
        try {
            List<CloudTenant> tenants = veeamService.getTenants();
            for (CloudTenant cloudTenant : tenants) {
                try {
                    accountingHelperService.process(cloudTenant, countMap);
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


}
