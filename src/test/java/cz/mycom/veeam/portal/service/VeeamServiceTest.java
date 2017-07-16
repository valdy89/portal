package cz.mycom.veeam.portal.service;

import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class VeeamServiceTest {

    @Autowired
    VeeamService veeamService;

    @Test
    @WithMockUser(roles={"SYSTEM"})
    public void tenants() throws Exception {
        LogonSession logonSession = veeamService.logonSystem();
        EntityReferences tenants = veeamService.tenants();
        tenants.getReves().stream().forEach(t -> log.debug("Tenant: " + t.getName()));
        veeamService.logout(logonSession.getSessionId());
    }

    @Test
    @WithMockUser(roles={"SYSTEM"})
    public void getRepositories() {
        LogonSession logonSessionType = veeamService.logonSystem();
        List<Repository> repositories = veeamService.getRepositories();
        repositories.stream().forEach(r -> log.info(r.getCapacity() + " " + r.getFreeSpace()));
        veeamService.logout(logonSessionType.getSessionId());
    }

    @Test
    @WithMockUser(roles={"SYSTEM"})
    public void getTenantResource() {
        LogonSession logonSessionType = veeamService.logonSystem();
        CloudTenantResources resources = veeamService.getTenantResources("358c6e3a-f55a-4b79-9b39-8608a1908b30");
        resources.getCloudTenantResources().stream().forEach(r -> log.info(r.getRepositoryQuota().getQuota() + " " + r.getRepositoryQuota().getUsedQuota()));
        veeamService.logout(logonSessionType.getSessionId());
    }

    @Test
    @WithMockUser(roles={"SYSTEM"})
    public void createTenant() {
        LogonSession logonSessionType = veeamService.logonSystem();
        CreateCloudTenantSpec tenant = new CreateCloudTenantSpec();
        tenant.setName("dursik@gmail.com");
        tenant.setDescription("Portal automatically created user - " + DateFormatUtils.format(new Date(), "dd.MM.yyyy HH:mm:ss"));
        tenant.setEnabled(false);
        String password = RandomStringUtils.randomAscii(10);
        log.info("Password: " + password);
        tenant.setPassword(password);
        tenant.setVmCount(1);
        tenant.setThrottlingEnabled(false);
        tenant.setMaxConcurrentTasks(1);
        tenant.setBackupServerUid(veeamService.getBackupServerUUID());
        tenant.setResources(new CreateCloudTenantResourceListType());
        CreateCloudTenantResourceSpec backupResource = new CreateCloudTenantResourceSpec();
        backupResource.setQuotaMb(2000);
        backupResource.setName("dursik@gmail.com - BackupResource");
        Repository repostitory = veeamService.getPreferredRepository();
        backupResource.setRepositoryUid(repostitory.getUID());

        tenant.getResources().getBackupResources().add(backupResource);
        CloudTenant tenantEntity = veeamService.createTenant(tenant);
        log.info(tenantEntity.getUID());
        veeamService.logout(logonSessionType.getSessionId());
    }
}