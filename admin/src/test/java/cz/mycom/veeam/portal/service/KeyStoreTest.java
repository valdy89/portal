package cz.mycom.veeam.portal.service;

import cz.mycom.veeam.portal.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@Slf4j
@ContextConfiguration(classes = AppConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class KeyStoreTest {

    @Autowired
    private KeyStoreService keyStoreService;

    @Test
    public void read() {
        final String passwd = keyStoreService.readData("veeam.api.password");
        System.out.println(passwd);
    }
}
