package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.CloudTenantResources;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dursik
 */
@RestController
@RequestMapping("/tenant")
public class TenantController {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private VeeamService veeamService;

    @RequestMapping(method = RequestMethod.POST)
    public List<Tenant> list() {
        List<Tenant> tenants = tenantRepository.findAll();
        LogonSession logonSession = veeamService.logonSystem();
        List<Tenant> ret = new ArrayList<>();
        tenants.stream()
                .filter(t -> StringUtils.isNotBlank(t.getUid()))
                .forEach(t -> {
                    CloudTenant tenant = veeamService.getTenant(t.getUid());
                    t.setVm(tenant.getVmCount());
                    CloudTenantResources resources = tenant.getResources();
                    resources.getCloudTenantResources().stream().forEach(r -> {
                        //t.setUsedQuota(t.getUsedQuota() + r.getRepositoryQuota().getUsedQuota());
                        t.setUsedQuota(1000);
                        t.setQuota(t.getQuota() + r.getRepositoryQuota().getQuota());
                    });
                    ret.add(t);
                });
        veeamService.logout(logonSession.getSessionId());
        return ret;
    }
}
