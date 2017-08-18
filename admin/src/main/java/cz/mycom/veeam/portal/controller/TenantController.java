package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.CloudTenantResource;
import com.veeam.ent.v1.CloudTenantResources;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.SubtenantRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@RequestMapping("/tenant")
public class TenantController {
    @Autowired
    private VeeamService veeamService;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Tenant> list() {
        return tenantRepository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public Tenant save(@RequestBody Tenant tenant) {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            CloudTenant cloudTenant = veeamService.getTenant(tenant.getUid());
            cloudTenant.setEnabled(tenant.isEnabled());
            if (StringUtils.isNotBlank(tenant.getPassword())) {
                cloudTenant.setPassword(tenant.getPassword());
            } else {
                cloudTenant.setPassword(null);
            }
            CloudTenantResources cloudTenantResources = cloudTenant.getResources();
            if (cloudTenantResources != null
                    && !CollectionUtils.isEmpty(cloudTenantResources.getCloudTenantResources())) {
                CloudTenantResource cloudTenantResource = cloudTenantResources.getCloudTenantResources().get(0);
                cloudTenantResource.getRepositoryQuota().setQuota((long) tenant.getQuota());
            }
            veeamService.saveTenant(tenant.getUid(), cloudTenant);
            tenantRepository.save(tenant);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession);
        }
        return tenantRepository.save(tenant);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void delete(@RequestParam(required = true) int id) {
        LogonSession logonSession = veeamService.logonSystem();
        Tenant tenant = tenantRepository.findOne(id);
        CloudTenant cloudTenant = null;
        try {
            cloudTenant = veeamService.getTenant(tenant.getUid());
        } catch (Exception e) {
        } finally {
            veeamService.logout(logonSession);
        }
        if (cloudTenant == null) {
            ((JdbcUserDetailsManager) userDetailsService).deleteUser(tenant.getUser().getUsername());
        } else {
            throw new RuntimeException("Tenant musí být nejdříve smazán z Veeam Backup");
        }
    }

}
