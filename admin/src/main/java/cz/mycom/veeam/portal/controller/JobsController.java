package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.idoklad.Country;
import cz.mycom.veeam.portal.repository.CountryRepository;
import cz.mycom.veeam.portal.service.AccountingService;
import cz.mycom.veeam.portal.service.IDokladService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

/**
 * @author dursik
 */
@Transactional
@RestController
public class JobsController {

    @Autowired
    private AccountingService accountingService;
    @Autowired
    private IDokladService iDokladService;
    @Autowired
    private CountryRepository countryRepository;

    @Secured("ROLE_SYSTEM")
    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public HashMap<String, String> list() {
        accountingService.process();
        HashMap<String, String> ret = new HashMap<>();
        ret.put("message","done");
        return ret;
    }

    @Secured("ROLE_SYSTEM")
    @RequestMapping(value = "/countries", method = RequestMethod.GET)
    public HashMap<String, String> countries() {
        List<Country> countries = iDokladService.getCountries();
        if (!countries.isEmpty()) {
            countryRepository.deleteAll();
        }
        for (Country country : countries) {
            cz.mycom.veeam.portal.model.Country countryEntity = new cz.mycom.veeam.portal.model.Country();
            countryEntity.setCode(country.getCode());
            countryEntity.setName(country.getName());
            countryEntity.setNameEnglish(country.getNameEnglish());
            countryRepository.save(countryEntity);
        }
        HashMap<String, String> ret = new HashMap<>();
        ret.put("message","done");
        return ret;
    }
}
