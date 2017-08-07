package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author dursik
 */
@RestController
public class AccountingController {

    @Autowired
    private AccountingService accountingService;

    @Secured("ROLE_SYSTEM")
    @RequestMapping(name = "/account", method = RequestMethod.GET)
    public HashMap<String, String> list() {
        accountingService.process();
        HashMap<String, String> ret = new HashMap<>();
        ret.put("message","done");
        return ret;
    }
}
