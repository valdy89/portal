package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.SubtenantRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@Transactional
@RequestMapping("/subtenant")
public class SubtenantController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubtenantRepository subtenantRepository;
    @Autowired
    private VeeamService veeamService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Subtenant> list(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return subtenantRepository.findByTenant_UserId(user.getId());
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody Subtenant subtenant, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        LogonSession logonSession = veeamService.logonSystem();
        try {
            Tenant tenant = user.getTenant();
            String tenantUid = tenant.getUid();

            if (subtenantRepository.findByUsernameAndTenantUid(subtenant.getUsername(), tenantUid)!=null) {
                throw new RuntimeException("Subtenant již existuje: " + subtenant.getUsername());
            }
            CloudTenantResources resources = veeamService.getTenant(tenantUid).getResources();
            if (resources == null || resources.getCloudTenantResources().isEmpty()) {
                throw new RuntimeException("Tenant nemá přiřazené úložiště");
            }
            CloudTenantResource cloudTenantResource = resources.getCloudTenantResources().get(0);
            CloudTenantRepositoryQuotaInfoType repositoryQuota = cloudTenantResource.getRepositoryQuota();
            if (repositoryQuota == null) {
                throw new RuntimeException("Tenant nemá přiřazené úložiště");
            }
            if (subtenant.getQuota() > repositoryQuota.getQuota()) {
                throw new RuntimeException("Překročená maximální velikost úložiště, maximální velikost : " + repositoryQuota.getQuota()/1024 + " GB");
            }

            CloudSubtenantCreateSpec subtenantCreateSpec = new CloudSubtenantCreateSpec();
            subtenantCreateSpec.setName(subtenant.getUsername());
            subtenantCreateSpec.setPassword(subtenant.getPassword());
            subtenantCreateSpec.setQuotaName("Subtenant quota - " + repositoryQuota.getDisplayName());
            subtenantCreateSpec.setQuotaMb(subtenant.getQuota().intValue());
            subtenantCreateSpec.setEnabled(true);
            subtenantCreateSpec.setTenantResourceId(cloudTenantResource.getId());
            CloudSubtenant cloudSubtenant = veeamService.createSubtenant(tenantUid, subtenantCreateSpec);

            subtenant.setTenant(tenant);
            subtenant.setUid(cloudSubtenant.getId());
            subtenant.setDateCreated(new Date());
            subtenantRepository.save(subtenant);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession);
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void save(@RequestBody Subtenant subtenant, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        LogonSession logonSession = veeamService.logonSystem();
        try {
            Tenant tenant = user.getTenant();
            String tenantUid = tenant.getUid();
            Subtenant subtenantEntity = subtenantRepository.findByUsernameAndTenantUid(subtenant.getUsername(), tenantUid);
            if (subtenantEntity == null) {
                throw new RuntimeException("Subtenant neexistuje: " + subtenant.getUsername());
            }
            String subtenantUid = subtenantEntity.getUid();

            CloudSubtenant cloudSubtenant = veeamService.getSubtenant(tenantUid, subtenantUid);
            cloudSubtenant.setEnabled(subtenant.isEnabled());
            subtenantEntity.setEnabled(subtenant.isEnabled());

            if (cloudSubtenant.getRepositoryQuota() != null) {
                cloudSubtenant.getRepositoryQuota().setQuotaMb(subtenant.getQuota());
                subtenantEntity.setQuota(subtenant.getQuota());
            }

            if (StringUtils.isNotBlank(subtenant.getPassword())) {
                cloudSubtenant.setPassword(subtenant.getPassword());
            } else {
                cloudSubtenant.setPassword(null);
            }
            veeamService.saveSubtenant(tenantUid, subtenantUid, cloudSubtenant);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession);
        }
    }
}
