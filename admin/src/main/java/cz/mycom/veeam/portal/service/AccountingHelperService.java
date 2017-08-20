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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkOrder(Order order) {
        Tenant tenant = tenantRepository.findByUid(order.getTenantUid());
        User user = tenant.getUser();
        Contact contact = null;
        if (StringUtils.isBlank(user.getName())) {
            contact = iDokladService.findContact("support@mycom.cz");
            if (contact == null) {
                contact = new Contact();
                contact.setCompanyName("Koncový zákazník");
                contact.setEmail("support@mycom.cz");
                contact.setCountryId(iDokladService.getCountry("cz").getId());
                contact = iDokladService.saveContact(contact);
            }
        } else {
            contact = iDokladService.findContact(user.getEmail());
            if (contact == null) {
                contact = new Contact();
                contact.setCountryId(iDokladService.getCountry("cz").getId());
                contact.setDefaultBankAccount(null);
            }
            contact.setCompanyName(user.getName());
            contact.setEmail(user.getEmail());

            if (StringUtils.isBlank(user.getStreet())) {
                contact.setStreet("");
            } else {
                contact.setStreet(user.getStreet());
            }
            if (StringUtils.isBlank(user.getIco())) {
                contact.setIdentificationNumber("");
            } else
                contact.setIdentificationNumber(user.getIco());
            if (StringUtils.isBlank(user.getDic())) {
                contact.setVatIdentificationNumber("");
            } else
                contact.setVatIdentificationNumber(user.getDic());
            if (StringUtils.isBlank(user.getPostalCode())) {
                contact.setPostalCode("");
            } else
                contact.setPostalCode(user.getPostalCode());
            if (StringUtils.isBlank(user.getCity())) {
                contact.setCity("");
            } else
                contact.setCity(user.getCity());
            if (StringUtils.isBlank(user.getPhone())) {
                contact.setPhone("");
            } else
                contact.setPhone(user.getPhone());

            if (contact.getDefaultBankAccount() != null) {
                contact.getDefaultBankAccount().setBankId("");
            }

            contact = iDokladService.saveContact(contact);
            user.setCreditCheck(contact.getCreditCheck());
        }

        ProformaInvoiceInsert proformaInvoice = iDokladService.proformaDefault();
        proformaInvoice.setOrderNumber(StringUtils.leftPad(String.valueOf(order.getId()), 8, '0'));
        proformaInvoice.setPurchaserId(contact.getId());
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
            mailService.sendMail(user.getEmail(), "Zálohová faktura č. " + order.getDocumentNumber(), "", order.getDocumentNumber() + ".pdf", output);
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

            try {
                String pdf = iDokladService.getInvoicePdf(order.getInvoiceId());
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                IOUtils.write(Base64.decodeBase64(pdf), output);
                mailService.sendMail(tenant.getUser().getEmail(), "Faktura č. " + order.getDocumentNumber(), "", order.getDocumentNumber() + ".pdf", output);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        orderRepository.save(order);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(CloudTenant cloudTenant, Map<String, Integer[]> countMap) {
        log.info("Tenant: {} - {}", cloudTenant.getName(), cloudTenant.getUID());
        log.info("W: {}, VM: {}, S: {}", cloudTenant.getWorkStationBackupCount(), cloudTenant.getVmCount(), cloudTenant.getServerBackupCount());
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

        //jestli jiz dnes neprobehlo uctovani
        List<TenantHistory> todaySystem = tenantHistoryRepository.getTodayByModifier(tenantUid, SYSTEM);
        if (tenant.getUser() != null && todaySystem.isEmpty()) {
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

            if (tenant.getCredit() < 0 && !tenant.isVip() && cloudTenant.isEnabled()) {
                log.warn("Disabling cloud tenant, no credit");
                tenant.setEnabled(false);
                cloudTenant.setEnabled(false);
                cloudTenant.setPassword(null);
                veeamService.saveTenant(tenantUid, cloudTenant);
                mailService.sendMail(tenant.getUser().getEmail(), "Účet " + tenant.getUsername() + " byl zablokován", "Váš účet byl zablokován z důvodu nedostatečného kreditu.");
            }
        }
        tenantRepository.save(tenant);

        if (todaySystem.isEmpty()) {
            TenantHistory tenantHistory = new TenantHistory(tenant, SYSTEM);
            tenantHistoryRepository.save(tenantHistory);
        }
    }

}
