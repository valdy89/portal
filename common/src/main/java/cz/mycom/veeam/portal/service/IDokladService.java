package cz.mycom.veeam.portal.service;

import cz.mycom.veeam.portal.idoklad.*;
import cz.mycom.veeam.portal.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@Service
public class IDokladService {
    public static final String IDOKLAD_URL = "https://app.idoklad.cz/developer/";

    @Autowired
    private OAuth2RestTemplate iDokladRestTemplate;

    @Retryable(maxAttempts = 10)
    public Contact getContact(User user) {
        Contact contact = null;
        if (StringUtils.isBlank(user.getName())) {
            contact = findContact("support@mycombackup.cz");
            if (contact == null) {
                contact = new Contact();
                contact.setCompanyName("Koncový zákazník");
                contact.setEmail("support@mycombackup.cz");
                contact.setCountryId(getCountry("cz").getId());
                contact = saveContact(contact);
            }
        } else {
            contact = findContact(user.getEmail());
            if (contact == null) {
                contact = new Contact();
                contact.setCountryId(getCountry("cz").getId());
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

            contact = saveContact(contact);
            user.setCreditCheck(contact.getCreditCheck());
        }
        return contact;
    }

    @Retryable(maxAttempts = 10)
    public Country getCountry(String code) {
        Assert.isTrue(StringUtils.isNotBlank(code), "code is required");
        CountryData data = iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/Countries?filter=Code~eq~" + code, CountryData.class);
        log.debug(String.valueOf(data));
        if (data.getTotalItems() == 0) {
            throw new RuntimeException("Country " + code + " not found");
        }
        return data.getData().get(0);
    }

    @Retryable(maxAttempts = 10)
    public List<Country> getCountries() {
        CountryData data = iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/Countries?pagesize=1000", CountryData.class);
        return data.getData();
    }

    @Retryable(maxAttempts = 10)
    public Contact saveContact(Contact contact) {
        if (contact.getId() != null) {
            //update, PATCH nefungoval
            iDokladRestTemplate.put(IDOKLAD_URL + "api/v2/Contacts/{id}", contact, contact.getId());
        } else {
            contact = iDokladRestTemplate.postForObject(IDOKLAD_URL + "api/v2/Contacts", contact, Contact.class);
        }
        log.debug(String.valueOf(contact));
        return contact;
    }

    @Retryable(maxAttempts = 10)
    public Contact findContact(String email) {
        Assert.isTrue(StringUtils.isNotBlank(email), "email is required");
        ContactData data = iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/Contacts?filter=email~eq~" + email, ContactData.class);
        log.debug(String.valueOf(data));
        if (data.getTotalItems() != 1) {
            log.debug("Contact not found by email: {}", email);
            return null;
        }
        return data.getData().get(0);
    }

    @Retryable(maxAttempts = 10)
    public void deleteContact(int id) {
        iDokladRestTemplate.delete(IDOKLAD_URL + "api/v2/Contacts/{id}", id);
    }

    @Retryable(maxAttempts = 10)
    public ProformaInvoiceInsert proformaDefault() {
        try {
            return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/ProformaInvoices/Default", ProformaInvoiceInsert.class);
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public ProformaInvoice proforma(ProformaInvoiceInsert proformaInvoiceInsert) {
        try {
            ProformaInvoice invoice = iDokladRestTemplate.postForObject(IDOKLAD_URL + "api/v2/ProformaInvoices", proformaInvoiceInsert, ProformaInvoice.class);
            return invoice;
        } catch (HttpStatusCodeException e) {
            log.error("Error: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public ProformaInvoice getProformaInvoice(Integer proformaId) {
        try {
            return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/ProformaInvoices/{id}", ProformaInvoice.class, proformaId);
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public void exportProforma(int proformaId) {
        try {
            iDokladRestTemplate.put(IDOKLAD_URL + "api/v2/ProformaInvoices/{id}/Exported/1", null, proformaId);
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public List<ProformaInvoice> getProformaPaid(int lastUnpaidProformaId) {
        try {
            ProformaInvoiceData proformaInvoiceData = iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/ProformaInvoices?pagesize=1000&filter=ispaid~eq~true|exported~eq~0|id~gte~" + lastUnpaidProformaId, ProformaInvoiceData.class);
            return proformaInvoiceData.getData();
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public IssuedInvoiceInsert invoiceDefault() {
        try {
            return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/IssuedInvoices/Default", IssuedInvoiceInsert.class);
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }

    }

    @Retryable(maxAttempts = 10)
    public IssuedInvoice invoice(IssuedInvoiceInsert issuedInvoiceInsert) {
        try {
            IssuedInvoice invoice = iDokladRestTemplate.postForObject(IDOKLAD_URL + "api/v2/IssuedInvoices", issuedInvoiceInsert, IssuedInvoice.class);
            return invoice;
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public IssuedInvoice getInvoice(Integer invoiceId) {
        try {
            return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/IssuedInvoices/{id}", IssuedInvoice.class, invoiceId);
        } catch (HttpStatusCodeException e) {
            log.error("{}: {}",e.getMessage(), e.getResponseBodyAsString());
            throw e;
        }
    }

    @Retryable(maxAttempts = 10)
    public String getProformaPdf(Integer id) {
        return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/ProformaInvoices/{id}/GetPdf", String.class, id);
    }

    @Retryable(maxAttempts = 10)
    public String getInvoicePdf(Integer id) {
        return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/IssuedInvoices/{id}/GetPdf", String.class, id);
    }
}
