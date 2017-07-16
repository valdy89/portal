package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.CloudSubtenant;
import com.veeam.ent.v1.CloudSubtenants;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dursik
 */
@RestController
@RequestMapping("/subtenant")
public class SubtenantController {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private VeeamService veeamService;

    @RequestMapping( method = RequestMethod.GET)
    @Secured("ROLE_SYSTEM")
    public List<Subtenant> list(@RequestParam(required = true) String uid) {
        LogonSession logonSession = veeamService.logonSystem();
        List<Subtenant> ret = new ArrayList<>();
        CloudSubtenants subtenants = veeamService.getSubtenants(uid);
        subtenants.getCloudSubtenants().stream().forEach(t -> {
            Subtenant subtenant = new Subtenant();
            subtenant.setName(t.getName());
            subtenant.setDescription(t.getDescription());
            subtenant.setEnabled(t.isEnabled());
            if (t.getRepositoryQuota()!=null) {
                subtenant.setUsedQuota(t.getRepositoryQuota().getUsedQuotaMb());
                subtenant.setQuota(t.getRepositoryQuota().getQuotaMb());
            }
            ret.add(subtenant);
        });
        veeamService.logout(logonSession.getSessionId());
        return ret;
    }

    @Data
    public static class Subtenant {
        private String name;
        private String description;
        private long quota;
        private long usedQuota;
        private boolean enabled;
    }
}
