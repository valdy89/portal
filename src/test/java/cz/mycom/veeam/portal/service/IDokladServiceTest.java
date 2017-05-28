package cz.mycom.veeam.portal.service;

import cz.mycom.veeam.portal.AppConfig;
import cz.mycom.veeam.portal.idoklad.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;

/**
 * @author dursik
 */
@Slf4j
@WebAppConfiguration
@ContextConfiguration(classes = AppConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IDokladServiceTest {

    @Autowired
    IDokladService iDokladService;

    @Test
    public void getCountries() throws Exception {
        Country country = iDokladService.getCountry("CZ");
        log.debug("Country id: " + country.getId());
    }

    @Test
    public void testContactCreate() {
        Contact contact = new Contact();
        contact.setCompanyName("dursik");
        contact.setCountryId(2);
        contact = iDokladService.saveContact(contact);
        log.info("Contact id: " + contact.getId());
        if (contact.getId() != null) {
            iDokladService.deleteContact(contact.getId());
        }
    }

    @Test
    public void testProforma() {
        ProformaInvoiceInsert proformaInvoiceInsert = new ProformaInvoiceInsert();
        proformaInvoiceInsert.setPurchaserId(4051782);
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setAmount(BigDecimal.ONE);
        invoiceItem.setUnitPrice(BigDecimal.TEN);
        invoiceItem.setUnit("Litry");
        invoiceItem.setName("za beno");
        proformaInvoiceInsert.getProformaInvoiceItems().add(invoiceItem);
        iDokladService.proforma(proformaInvoiceInsert);
    }

    @Test
    public void testFindContact() {
        Contact contact = iDokladService.findContact("123456");
        log.info("CreditCheck: " + contact.getCreditCheck());
    }

    @Test
    public void invoices() throws Exception {

    }

}