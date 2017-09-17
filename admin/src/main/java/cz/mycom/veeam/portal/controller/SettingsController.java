package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.Config;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.service.KeyStoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dursik
 */
@Slf4j
@RestController
@Transactional
@RequestMapping("/settings")
public class SettingsController {
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private KeyStoreService keyStoreService;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, String> get() {
        HashMap<String, String> ret = new LinkedHashMap<>();
        for (Config config : configRepository.findAllOrderByName()) {
            ret.put(config.getName(), config.getValue());
        }
        return ret;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void save(@RequestBody Map<String, String> values) {
        log.debug("Values: " + values);
        for (String key : values.keySet()) {
            String value = values.get(key);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            Config config = configRepository.findOne(key);
            if (config != null) {
                config.setValue(value);
            } else {
                keyStoreService.storeData(key, value);
            }
        }
    }
}
