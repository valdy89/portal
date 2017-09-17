package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.CloudTenantResource;
import com.veeam.ent.v1.CloudTenantResources;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.TenantHistory;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.SubtenantRepository;
import cz.mycom.veeam.portal.repository.TenantHistoryRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@Transactional
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private VeeamService veeamService;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SubtenantRepository subtenantRepository;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private TenantHistoryRepository tenantHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @RequestMapping(method = RequestMethod.GET)
    public List<Tenant> list() {
        List<Tenant> tenantList = tenantRepository.findAll();
        for (Tenant tenant : tenantList) {
            tenant.setSubtenantsCount(tenant.getSubtenants().size());
        }
        return tenantList;
    }

    @RequestMapping(path = "/nocredit", method = RequestMethod.GET)
    public List<Tenant> nocredit() {
        ArrayList<Tenant> ret = new ArrayList<>();
        List<Tenant> tenantList = tenantRepository.findAll();
        for (Tenant tenant : tenantList) {
            if (tenant.getCredit() > 0 || tenant.getUser() == null) {
                continue;
            }
            tenant.setSubtenantsCount(tenant.getSubtenants().size());
            ret.add(tenant);
        }
        return ret;
    }

    @RequestMapping(method = RequestMethod.POST)
    public Tenant save(@RequestBody Tenant tenant, Principal principal) {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            CloudTenant cloudTenant = veeamService.getTenant(tenant.getUid());
            cloudTenant.setEnabled(tenant.isEnabled());
            if (StringUtils.isNotBlank(tenant.getPassword())) {
                cloudTenant.setPassword(tenant.getPassword());
                User user = userRepository.findByTenantUid(tenant.getUid());
                if (user != null) {
                    user.setPassword(passwordEncoder.encode(tenant.getPassword()));
                }
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession);
        }
        Tenant tenantEntity = tenantRepository.findByUid(tenant.getUid());
        if (tenantEntity == null || tenant.getCredit() != tenantEntity.getCredit() || tenant.getQuota() != tenantEntity.getQuota()) {
            log.debug("Saving tenant history");
            tenantHistoryRepository.save(new TenantHistory(tenant, principal.getName()));
        }
        return tenantRepository.save(tenant);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void delete(@RequestParam(required = true) String uid) {
        LogonSession logonSession = veeamService.logonSystem();
        Tenant tenant = tenantRepository.findOne(uid);
        CloudTenant cloudTenant = null;
        try {
            cloudTenant = veeamService.getTenant(tenant.getUid());
        } catch (Exception e) {
        } finally {
            veeamService.logout(logonSession);
        }
        if (cloudTenant == null) {
            if (tenant.getUser() != null) {
                ((JdbcUserDetailsManager) userDetailsService).deleteUser(tenant.getUser().getUsername());
            }
            for (Subtenant subtenant : tenant.getSubtenants()) {
                subtenantRepository.delete(subtenant);
            }
            tenantRepository.delete(tenant);
        } else {
            throw new RuntimeException("Tenant musí být nejdříve smazán z Veeam Backup");
        }
    }

}
