package cz.mycom.veeam.portal.service;

import cz.mycom.veeam.portal.idoklad.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

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

    @Cacheable("countries")
    public Country getCountry(String code) {
        Assert.isTrue(StringUtils.isNotBlank(code), "code is required");
        CountryData data = iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/Countries?filter=Code~eq~" + code, CountryData.class);
        log.debug(String.valueOf(data));
        if (data.getTotalItems() == 0) {
            throw new RuntimeException("Country " + code + " not found");
        }
        return data.getData().get(0);
    }

    public Contact saveContact(Contact contact) {
        if (contact.getId() != null) {
            //update
            iDokladRestTemplate.put(IDOKLAD_URL + "api/v2/Contacts/{id}", contact, contact.getId());
        } else {
            contact = iDokladRestTemplate.postForObject(IDOKLAD_URL + "api/v2/Contacts", contact, Contact.class);
        }
        log.debug(String.valueOf(contact));
        return contact;
    }

    public Contact findContact(String email) {
        Assert.isTrue(StringUtils.isNotBlank(email), "email is required");
        ContactData data = iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/Contacts?filter=Email~eq~" + email, ContactData.class);
        log.debug(String.valueOf(data));
        if (data.getTotalItems() != 1) {
            log.debug("Contact not found by email: {}", email);
            return null;
        }
        return data.getData().get(0);
    }

    public void deleteContact(int id) {
        iDokladRestTemplate.delete(IDOKLAD_URL + "api/v2/Contacts/{id}", id);
    }

    public ProformaInvoiceInsert proformaDefault() {
        try {
            return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/ProformaInvoices/Default", ProformaInvoiceInsert.class);
        } catch (HttpStatusCodeException e) {
            log.error("Error: {}", e.getResponseBodyAsString());
            throw e;
        }

    }
    public ProformaInvoice proforma(ProformaInvoiceInsert proformaInvoiceInsert) {
        try {
            ProformaInvoice proformaInvoice = iDokladRestTemplate.postForObject(IDOKLAD_URL + "api/v2/ProformaInvoices", proformaInvoiceInsert, ProformaInvoice.class);
            log.debug(String.valueOf(proformaInvoice));
            return proformaInvoice;
        } catch (HttpStatusCodeException e) {
            log.error("Error: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    public List<ProformaInvoiceInsert> invoices() {
        return null;
    }

    public String getPdf(Integer id) {
        return iDokladRestTemplate.getForObject(IDOKLAD_URL + "api/v2/ProformaInvoices/{id}/GetPdf", String.class, id);
    }

    public void sendProforma(Integer id) {
        iDokladRestTemplate.put(IDOKLAD_URL + "api/v2/ProformaInvoices/{id}/SendMailToPurchaser", id);
    }
}
