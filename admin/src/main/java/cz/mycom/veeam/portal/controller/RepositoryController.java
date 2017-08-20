package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.LogonSession;
import com.veeam.ent.v1.Repository;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dursik
 */
@Slf4j
@RestController
@Transactional
@RequestMapping("/repository")
public class RepositoryController {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private VeeamService veeamService;

    @RequestMapping(method = RequestMethod.GET)

    public List<Repository> list() {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            double giga = Math.pow(2, 30);
            List<Repository> ret = new ArrayList<>();
            List<com.veeam.ent.v1.Repository> repositories = veeamService.getRepositories();
            repositories.stream().forEach(r-> {
                Repository repository = new Repository();
                repository.setName(r.getName());
                repository.setBackupSize((double) (r.getCapacity() - r.getFreeSpace()) / giga);
                repository.setCapacity((double) r.getCapacity() / giga);
                repository.setFreeSpace((double) r.getFreeSpace() / giga);
                String uid = StringUtils.substringAfterLast(r.getUID(),":");
                List<Tenant> tenants = tenantRepository.findByRepositoryUid(uid);
                repository.setTenantCount(tenants.size());
                int vmCount = 0, serverCount =  0,workstationCount  = 0;
                long purchasedSpace = 0;
                for (Tenant tenant : tenants) {
                    vmCount += tenant.getVmCount();
                    serverCount += tenant.getServerCount();
                    workstationCount += tenant.getWorkstationCount();
                    purchasedSpace += tenant.getQuota();
                }
                repository.setVmCount(vmCount);
                repository.setServerCount(serverCount);
                repository.setWorkstationCount(workstationCount);
                repository.setPurchasedSpace(purchasedSpace / 1024);
                ret.add(repository);
            });

            return ret;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession.getSessionId());
        }
    }

    @Data
    public static class Repository {
        private String name;
        private double capacity;
        private double freeSpace;
        private double backupSize;
        private long purchasedSpace;
        private int vmCount;
        private int serverCount;
        private int workstationCount;
        private int tenantCount;

    }
}
