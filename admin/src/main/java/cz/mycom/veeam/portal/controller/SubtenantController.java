package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.CloudSubtenant;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.repository.SubtenantRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@RequestMapping("/subtenant")
public class SubtenantController {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private SubtenantRepository subtenantRepository;
    @Autowired
    private VeeamService veeamService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Subtenant> list(@RequestParam(required = true) int userId) {
        return subtenantRepository.findByTenant_UserId(userId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void save(@RequestBody Subtenant subtenant) {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            Tenant tenant = tenantRepository.findBySubtenantsEquals(subtenant);
            String tenantUid = tenant.getUid();
            String subtenantUid = subtenant.getUid();

            CloudSubtenant cloudSubtenant = veeamService.getSubtenant(tenantUid, subtenantUid);
            cloudSubtenant.setEnabled(subtenant.isEnabled());

            if (cloudSubtenant.getRepositoryQuota() != null) {
                cloudSubtenant.getRepositoryQuota().setQuotaMb((long) subtenant.getQuota());
            }

            if (StringUtils.isNotBlank(subtenant.getPassword())) {
                cloudSubtenant.setPassword(subtenant.getPassword());
            } else {
                cloudSubtenant.setPassword(null);
            }
            veeamService.saveSubtenant(tenantUid, subtenantUid, cloudSubtenant);
            subtenantRepository.save(subtenant);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession);
        }
    }
}
