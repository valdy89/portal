package cz.mycom.veeam.portal.service;

import com.opencsv.CSVReader;
import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.idoklad.*;
import cz.mycom.veeam.portal.model.*;
import cz.mycom.veeam.portal.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author dursik
 */
@Slf4j
@Component
public class AccountingService {
    @Autowired
    private VeeamService veeamService;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SubtenantRepository subtenantRepository;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private MailService mailService;
    @Autowired
    private TenantHistoryRepository tenantHistoryRepository;
    @Autowired
    private IDokladService iDokladService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final String SYSTEM = "SYSTEM";


    @Scheduled(cron = "0 0 * * * ?")
    public void checkPaid() {
        List<Order> orders = orderRepository.findByPaymentStatusIn(PaymentStatusEnum.Unpaid, PaymentStatusEnum.PartialPaid);
        for (Order order : orders) {
            try {
                transactionTemplate.execute(new TransactionCallback<Integer>() {
                    @Override
                    public Integer doInTransaction(TransactionStatus transactionStatus) {
                        if (order.getProformaId() != null) {
                            ProformaInvoice proforma = iDokladService.getProformaInvoice(order.getProformaId());
                            order.setPaymentStatus(proforma.getPaymentStatus());

                            if (order.getPaymentStatus() == PaymentStatusEnum.Paid || order.getPaymentStatus() == PaymentStatusEnum.Overpaid) {
                                IssuedInvoiceInsert issuedInvoiceInsert = iDokladService.invoiceDefault();
                                issuedInvoiceInsert.setDateOfPayment(proforma.getDateOfPayment());
                                InvoiceItem invoiceItem = issuedInvoiceInsert.getIssuedInvoiceItems().get(0);
                                InvoiceItem proformaItem = proforma.getProformaInvoiceItems().get(0);
                                invoiceItem.setUnitPrice(proformaItem.getUnitPrice());
                                invoiceItem.setUnit(proformaItem.getUnit());
                                invoiceItem.setName(proformaItem.getName());
                                issuedInvoiceInsert.setDateOfMaturity(DateFormatUtils.ISO_DATETIME_FORMAT.format(DateUtils.addDays(new Date(), 1)));
                                issuedInvoiceInsert.setPurchaserId(proforma.getPurchaserId());
                                IssuedInvoice invoice = iDokladService.invoice(issuedInvoiceInsert);
                                order.setInvoiceId(invoice.getId());
                                order.setDocumentNumber(invoice.getDocumentNumber());

                                try {
                                    String pdf = iDokladService.getInvoicePdf(order.getInvoiceId() != null ? order.getInvoiceId() : order.getProformaId());
                                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                                    IOUtils.write(Base64.decodeBase64(pdf), output);
                                    Tenant tenant = tenantRepository.findByUid(order.getTenantUid());
                                    mailService.sendMail(tenant.getUser().getUsername(), "Faktura č. " + order.getDocumentNumber(), "", order.getDocumentNumber() + ".pdf", output);
                                } catch (IOException e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        }

                        return 1;
                    }
                });

            } catch (Exception e) {
                log.error(e.getMessage(), e);
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
                transactionTemplate.execute(new TransactionCallback<Integer>() {
                    @Override
                    public Integer doInTransaction(TransactionStatus transactionStatus) {
                        log.info("Tenant: {} - {}", cloudTenant.getName(), cloudTenant.getUID());
                        String tenantUid = StringUtils.substringAfterLast(cloudTenant.getUID(), ":");
                        Tenant tenant = tenantRepository.findByUid(tenantUid);
                        if (tenant == null) {
                            log.warn("Not existing tenant");
                            tenant = new Tenant();
                            tenant.setUid(tenantUid);
                            tenant.setEnabled(cloudTenant.isEnabled());
                            tenant.setDateCreated(new Date());
                            tenant.setUsername(cloudTenant.getName());
                            tenant = tenantRepository.save(tenant);
                        } else {
                            tenant.setEnabled(cloudTenant.isEnabled());
                            tenant.setUsername(cloudTenant.getName());
                        }
                        CloudSubtenants subtenants = veeamService.getSubtenants(tenantUid);
                        if (subtenants != null) {
                            for (CloudSubtenant cloudSubtenant : subtenants.getCloudSubtenants()) {
                                String subtenantUid = StringUtils.substringAfterLast(cloudSubtenant.getId(), ":");
                                Subtenant subtenant = subtenantRepository.findByUid(subtenantUid);
                                if (subtenant == null) {
                                    subtenant = new Subtenant();
                                    subtenant.setEnabled(cloudSubtenant.isEnabled());
                                    subtenant.setTenant(tenant);
                                    subtenant.setUid(subtenantUid);
                                    subtenant.setUsername(cloudSubtenant.getName());
                                    subtenant.setDateCreated(new Date());
                                    subtenant = subtenantRepository.save(subtenant);
                                } else {
                                    subtenant.setEnabled(cloudSubtenant.isEnabled());
                                }

                                CloudSubtenantRepositoryQuotaInfoType repositoryQuota = cloudSubtenant.getRepositoryQuota();
                                if (repositoryQuota != null) {
                                    Long pom = repositoryQuota.getQuotaMb();
                                    subtenant.setQuota(pom != null ? pom : 0L);
                                    pom = repositoryQuota.getUsedQuotaMb();
                                    subtenant.setUsedQuota(pom != null ? pom : 0L);
                                }
                            }
                        }
                        if (cloudTenant.getResources() != null) {
                            for (CloudTenantResource cloudTenantResource : cloudTenant.getResources().getCloudTenantResources()) {
                                CloudTenantRepositoryQuotaInfoType repositoryQuota = cloudTenantResource.getRepositoryQuota();
                                if (repositoryQuota != null) {
                                    if (repositoryQuota.getQuota() != null) {
                                        tenant.setQuota(repositoryQuota.getQuota().intValue());
                                    }
                                    if (repositoryQuota.getUsedQuota() != null) {
                                        tenant.setUsedQuota(repositoryQuota.getUsedQuota().intValue());
                                    }
                                    tenant.setRepositoryUid(StringUtils.substringAfterLast(repositoryQuota.getRepositoryUid(), ":"));
                                }
                            }
                        }

                        //jestli jiz dnes neprobehlo uctovani
                        TenantHistory todaySystem = tenantHistoryRepository.getTodayByModifier(tenantUid, SYSTEM);
                        if (tenant.getUser() != null && todaySystem == null) {
                            Calendar now = Calendar.getInstance();
                            int credit = tenant.getCredit();
                            int priceVm = Integer.parseInt(configRepository.getOne("price.vm").getValue());
                            int priceServer = Integer.parseInt(configRepository.getOne("price.server").getValue());
                            int priceWorkstation = Integer.parseInt(configRepository.getOne("price.workstation").getValue());

                            Integer[] counts = countMap.get(tenant.getUsername());
                            //kontrola jestli se nezmenil pocet VM
                            if (now.get(Calendar.DAY_OF_MONTH) != 1 && counts != null) {
                                if (counts[0] > tenant.getVmCount()) {
                                    credit -= (counts[0] - tenant.getVmCount()) * priceVm;
                                }
                                if (counts[1] > tenant.getServerCount()) {
                                    credit -= (counts[1] - tenant.getServerCount()) * priceServer;
                                }
                                if (counts[2] > tenant.getWorkstationCount()) {
                                    credit -= (counts[2] - tenant.getWorkstationCount()) * priceWorkstation;
                                }
                            }

                            if (counts != null) {
                                tenant.setVmCount(counts[0]);
                                tenant.setServerCount(counts[1]);
                                tenant.setWorkstationCount(counts[2]);
                            }

                            //na zacatku mesice zkasni vsechny
                            if (now.get(Calendar.DAY_OF_MONTH) == 1) {
                                credit -= tenant.getVmCount() * priceVm;
                                credit -= tenant.getServerCount() * priceServer;
                                credit -= tenant.getWorkstationCount() * priceWorkstation;
                            }

                            //kazdy den za quotu
                            int priceQuota = Integer.parseInt(configRepository.getOne("price.quota").getValue());
                            credit -= Math.ceil(((float) tenant.getQuota() / 1024 / 10) * priceQuota);
                            tenant.setCredit(credit);

                            if (tenant.getCredit() < 0 && !tenant.getUser().isVip() && cloudTenant.isEnabled()) {
                                log.warn("Disabling cloud tenant, no credit");
                                tenant.setEnabled(false);
                                cloudTenant.setEnabled(false);
                                cloudTenant.setPassword(null);
                                veeamService.saveTenant(tenantUid, cloudTenant);
                                mailService.sendMail(tenant.getUser().getUsername(), "Účet " + tenant.getUsername() + " byl zablokován", "Váš účet byl zablokován z důvodu nedostatečného kreditu.");
                            }
                        }
                        if (todaySystem == null) {
                            TenantHistory tenantHistory = new TenantHistory(tenant, SYSTEM);
                            tenantHistoryRepository.save(tenantHistory);
                        }
                        return 1;
                    }
                });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            mailService.sendError("Error accounting: " + e.getMessage(), e);
        } finally {
            veeamService.logout(logonSession);
        }
    }


}
