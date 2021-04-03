package cz.mycom.veeam.portal.service;

import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.idoklad.*;
import cz.mycom.veeam.portal.model.*;
import cz.mycom.veeam.portal.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@Component
public class AccountingHelperService {
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

    private static final String SYSTEM = "System";
    private NumberFormat numberFormat = new DecimalFormat("#.00");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkOrder(Order order) {
        Tenant tenant = tenantRepository.findByUid(order.getTenantUid());
        User user = tenant.getUser();

        ProformaInvoiceInsert proformaInvoice = iDokladService.proformaDefault();
        proformaInvoice.setOrderNumber(StringUtils.leftPad(String.valueOf(order.getId()), 8, '0'));
        proformaInvoice.setPurchaserId(iDokladService.getContact(user).getId());
        proformaInvoice.setProformaInvoiceItems(new ArrayList<>());
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setAmount(BigDecimal.ONE);
        invoiceItem.setName("Nákup kreditů: " + order.getCredit() + "ks");
        invoiceItem.setUnit("");
        invoiceItem.setUnitPrice(order.getPrice());
        invoiceItem.setPriceType(PriceTypeEnum.WithoutVat);
        invoiceItem.setVatRateType(VatRateTypeEnum.Basic);
        proformaInvoice.getProformaInvoiceItems().add(invoiceItem);
        proformaInvoice.setDateOfMaturity(DateFormatUtils.ISO_DATETIME_FORMAT.format(DateUtils.addDays(new Date(), 1)));
        proformaInvoice.setDateOfPayment(null);
        ProformaInvoice proforma = iDokladService.proforma(proformaInvoice);
        order.setProformaId(proforma.getId());
        order.setDocumentNumber(proforma.getDocumentNumber());
        order.setPaymentStatus(proforma.getPaymentStatus());
        orderRepository.save(order);

        try {
            String pdf = iDokladService.getProformaPdf(order.getProformaId());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.write(Base64.decodeBase64(pdf), output);
            String from = configRepository.getOne("fakturace.email").getValue();
            ClassPathResource classPathResource = new ClassPathResource("zalohova_faktura.txt");
            String text = IOUtils.toString(classPathResource.getInputStream(), "UTF-8");
            text = text.replace("%DATE%", DateFormatUtils.format(order.getDateCreated(), "dd.MM.yyyy"));
            text = text.replace("%PRICE%", numberFormat.format(order.getPriceWithVat()));
            mailService.sendMail(from, user.getEmail(), "Veeam CloudConnect portál – zálohová faktura č. " + order.getDocumentNumber(), text, order.getDocumentNumber() + ".pdf", output);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkPaid(ProformaInvoice proforma) {
        Order order = orderRepository.findByProformaId(proforma.getId());
        if (order == null) {
            log.error("Order not found, proforma document number: " + proforma.getDocumentNumber());
            return;
        }
        if (order.getInvoiceId() != null) {
            iDokladService.exportProforma(order.getProformaId());
            return;
        }

        order.setPaymentStatus(proforma.getPaymentStatus());

        if (order.getPaymentStatus() == PaymentStatusEnum.Paid || order.getPaymentStatus() == PaymentStatusEnum.Overpaid) {
            IssuedInvoiceInsert issuedInvoiceInsert = iDokladService.invoiceDefault();
            issuedInvoiceInsert.setOrderNumber(StringUtils.leftPad(String.valueOf(order.getId()), 8, '0'));
            issuedInvoiceInsert.setDateOfPayment(proforma.getDateOfPayment());
            InvoiceItem invoiceItem = issuedInvoiceInsert.getIssuedInvoiceItems().get(0);
            InvoiceItem proformaItem = proforma.getProformaInvoiceItems()
                    .stream()
                    .filter(i -> !i.getName().equals("Rounding"))
                    .findFirst().get();
            invoiceItem.setUnitPrice(proformaItem.getUnitPrice());
            invoiceItem.setUnit(proformaItem.getUnit());
            invoiceItem.setName(proformaItem.getName());
            invoiceItem.setVatRateType(proformaItem.getVatRateType());
            invoiceItem.setPriceType(proformaItem.getPriceType());
            issuedInvoiceInsert.setDateOfMaturity(DateFormatUtils.ISO_DATETIME_FORMAT.format(DateUtils.addDays(new Date(), 1)));
            issuedInvoiceInsert.setPurchaserId(proforma.getPurchaserId());
            IssuedInvoice invoice = iDokladService.invoice(issuedInvoiceInsert);
            iDokladService.exportProforma(order.getProformaId());

            order.setInvoiceId(invoice.getId());
            order.setDocumentNumber(invoice.getDocumentNumber());

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

            sendInvoice(tenant.getUser().getEmail(), order);
        }
        orderRepository.save(order);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(CloudTenant cloudTenant) {
        Tenant tenant = updateTenant(cloudTenant);
        tenantRepository.save(tenant);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(CloudTenant cloudTenant) {
        Tenant tenant = updateTenant(cloudTenant);

        //jestli jiz dnes neprobehlo uctovani
        List<TenantHistory> todaySystem = tenantHistoryRepository.getTodayByModifier(tenant.getUid(), SYSTEM);
        if (tenant.getUser() != null && todaySystem.isEmpty()) {
            Calendar now = Calendar.getInstance();
            int credit = tenant.getCredit();
            int priceVm = Integer.parseInt(configRepository.getOne("price.vm").getValue());
            int priceServer = Integer.parseInt(configRepository.getOne("price.server").getValue());
            int priceWorkstation = Integer.parseInt(configRepository.getOne("price.workstation").getValue());

            Integer[] counts = new Integer[]{cloudTenant.getBackupCount(), cloudTenant.getServerBackupCount(), cloudTenant.getWorkStationBackupCount()};
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

            if (tenant.getCredit() < 0 && !tenant.isVip() && cloudTenant.isEnabled()) {
                log.warn("Disabling cloud tenant, no credit");
                tenant.setEnabled(false);
                cloudTenant.setEnabled(false);
                cloudTenant.setPassword(null);
                veeamService.saveTenant(tenant.getUid(), cloudTenant);
                try {
                    String from = configRepository.getOne("support.email").getValue();
                    ClassPathResource classPathResource = new ClassPathResource("zablokovany_ucet.txt");
                    String text = IOUtils.toString(classPathResource.getInputStream(), "UTF-8");
                    mailService.sendMail(from, tenant.getUser().getEmail(), "Veeam CloudConnect portál – účet " + tenant.getUsername() + " byl zablokován", text);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        tenantRepository.save(tenant);

        if (todaySystem.isEmpty()) {
            TenantHistory tenantHistory = new TenantHistory(tenant, SYSTEM);
            tenantHistoryRepository.save(tenantHistory);
        }
    }

    private Tenant updateTenant(CloudTenant cloudTenant) {
        log.info("Tenant: {} - {}", cloudTenant.getName(), cloudTenant.getUID());
        StringWriter stringWriter = new StringWriter();
        JAXB.marshal(cloudTenant, stringWriter);
        log.info("CloudTenant: {}", stringWriter);
        log.info("CloudTenant W: {}, VM: {}, S: {}", cloudTenant.getWorkStationBackupCount(), cloudTenant.getBackupCount(), cloudTenant.getServerBackupCount());
        String uid = StringUtils.substringAfterLast(cloudTenant.getUID(),":");
        CloudTenantFreeLicenseCounters licenseCounters = veeamService.getCloudTenantFreeLicenseCounters(uid);
        log.info("FreeLicense W: {}, VM: {}, S: {}", licenseCounters.getRentalWorkstationBackupCount(), licenseCounters.getRentalVMBackupCount(), licenseCounters.getRentalServerBackupCount());
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

        for (CloudSubtenant cloudSubtenant : veeamService.getSubtenants(tenantUid)) {
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
                subtenant.setQuota(pom != null ? pom.intValue() : 0);
                pom = repositoryQuota.getUsedQuotaMb();
                subtenant.setUsedQuota(pom != null ? pom.intValue() : 0);
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
        tenant.setVmCount(cloudTenant.getBackupCount() + licenseCounters.getRentalVMBackupCount());
        tenant.setServerCount(cloudTenant.getServerBackupCount() + licenseCounters.getRentalServerBackupCount());
        tenant.setWorkstationCount(cloudTenant.getWorkStationBackupCount() + licenseCounters.getRentalWorkstationBackupCount());
        return tenant;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkPaidOnline(Order order) {
        Tenant tenant = tenantRepository.findByUid(order.getTenantUid());
        User user = tenant.getUser();

        IssuedInvoiceInsert issuedInvoiceInsert = iDokladService.invoiceDefault();
        issuedInvoiceInsert.setOrderNumber(StringUtils.leftPad(String.valueOf(order.getId()), 8, '0'));
        issuedInvoiceInsert.setDateOfPayment(DateFormatUtils.ISO_DATETIME_FORMAT.format(new Date()));
        InvoiceItem invoiceItem = issuedInvoiceInsert.getIssuedInvoiceItems().get(0);
        invoiceItem.setAmount(BigDecimal.ONE);
        invoiceItem.setName("Nákup kreditů: " + order.getCredit() + "ks");
        invoiceItem.setUnit("");
        invoiceItem.setUnitPrice(order.getPrice());
        invoiceItem.setPriceType(PriceTypeEnum.WithoutVat);
        invoiceItem.setVatRateType(VatRateTypeEnum.Basic);
        issuedInvoiceInsert.setDateOfMaturity(DateFormatUtils.ISO_DATETIME_FORMAT.format(DateUtils.addDays(new Date(), 1)));
        issuedInvoiceInsert.setPurchaserId(iDokladService.getContact(user).getId());
        IssuedInvoice invoice = iDokladService.invoice(issuedInvoiceInsert);

        order.setInvoiceId(invoice.getId());
        order.setDocumentNumber(invoice.getDocumentNumber());

        orderRepository.save(order);

        sendInvoice(tenant.getUser().getEmail(), order);

    }

    private void sendInvoice(String email, Order order) {
        try {
            String pdf = iDokladService.getInvoicePdf(order.getInvoiceId());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.write(Base64.decodeBase64(pdf), output);
            String from = configRepository.getOne("fakturace.email").getValue();
            ClassPathResource classPathResource = new ClassPathResource("faktura.txt");
            String text = IOUtils.toString(classPathResource.getInputStream(), "UTF-8");
            text = text.replace("%DATE%", DateFormatUtils.format(order.getDateCreated(), "dd.MM.yyyy"));
            text = text.replace("%PRICE%", numberFormat.format(order.getPriceWithVat()));
            mailService.sendMail(from, email, "Veeam CloudConnect portál – faktura č. " + order.getDocumentNumber(), text, order.getDocumentNumber() + ".pdf", output);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkTenant(CloudTenant cloudTenant) {
        log.info("Check Tenant: {} - {}", cloudTenant.getName(), cloudTenant.getUID());
        String tenantUid = StringUtils.substringAfterLast(cloudTenant.getUID(), ":");
        Tenant tenant = tenantRepository.findByUid(tenantUid);
        if (!tenant.isEnabled() || tenant.getCredit() <= 0) {
            return;
        }
        try {
            if (tenant.getUsedQuota() > (tenant.getQuota() * 0.95)) {
                if (!tenant.isQuotaNotif()) {
                    log.info("Sending zaplneni_mista notification: " + cloudTenant.getName());
                    tenant.setQuotaNotif(true);
                    String from = configRepository.getOne("support.email").getValue();
                    ClassPathResource classPathResource = new ClassPathResource("zaplneni_mista.txt");
                    String text = IOUtils.toString(classPathResource.getInputStream(), "UTF-8");
                    mailService.sendMail(from, tenant.getUser().getEmail(), "support@mycom.cz", "MyCom BACKUP Portal: Vaše úložiště je téměř plné", text);
                }
            } else {
                tenant.setQuotaNotif(false);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (tenant.getServerCount() > 0
                    || tenant.getVmCount() > 0
                    || tenant.getWorkstationCount() > 0
                    || tenant.getQuota() > 0) {
                int credit = tenant.getCredit();
                Calendar cal = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
                int month = cal.get(Calendar.MONTH);
                int priceQuota = Integer.parseInt(configRepository.getOne("price.quota").getValue());
                int priceVm = Integer.parseInt(configRepository.getOne("price.vm").getValue());
                int priceServer = Integer.parseInt(configRepository.getOne("price.server").getValue());
                int priceWorkstation = Integer.parseInt(configRepository.getOne("price.workstation").getValue());
                //aby neposi
                int days = 0;
                while (credit > 0) {
                    int change = credit;
                    if (month != cal.get(Calendar.MONTH)) {
                        credit -= tenant.getVmCount() * priceVm;
                        credit -= tenant.getServerCount() * priceServer;
                        credit -= tenant.getWorkstationCount() * priceWorkstation;
                        month = cal.get(Calendar.MONTH);
                    }
                    credit -= Math.ceil(((float) tenant.getQuota() / 1024 / 10) * priceQuota);
                    if (credit <= 0) {
                        break;
                    }
                    change -= credit;
                    if (change <= 0) {
                        break;
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    days++;
                }
                if (days < 7) {
                    if (!tenant.isCreditNotif()) {
                        log.info("Sending dochazi_kredity notification: " + cloudTenant.getName());
                        tenant.setCreditNotif(true);
                        String from = configRepository.getOne("support.email").getValue();
                        ClassPathResource classPathResource = new ClassPathResource("dochazi_kredity.txt");
                        String text = IOUtils.toString(classPathResource.getInputStream(), "UTF-8");
                        mailService.sendMail(from, tenant.getUser().getEmail(), "support@mycom.cz", "MyCom BACKUP Portal: Blíží se vyčerpání kreditu", text);
                    }
                } else {
                    tenant.setCreditNotif(false);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        tenantRepository.save(tenant);
    }

}
