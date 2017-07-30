package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.service.IDokladService;
import cz.mycom.veeam.portal.idoklad.ProformaInvoiceInsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author dursik
 */
@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private IDokladService iDokladService;
    @RequestMapping(method = RequestMethod.GET)
    public List<ProformaInvoiceInsert> list() {
        return iDokladService.invoices();
    }
}
