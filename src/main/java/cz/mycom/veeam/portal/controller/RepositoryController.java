package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@RequestMapping("/repository")
public class RepositoryController {
    @Autowired
    private VeeamService veeamService;

    @RequestMapping(method = RequestMethod.GET)
    @Secured("ROLE_SYSTEM")
    public List<Repository> list() {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            List<Repository> ret = new ArrayList<>();
            double giga = Math.pow(2, 30);
            veeamService.getRepositoryReport().stream().forEach(r -> {
                if (r.getName().startsWith("Scale-")) {
                    Repository repository = new Repository();
                    repository.setName(r.getName());
                    repository.setBackupSize((double)r.getBackupSize() / giga);
                    repository.setCapacity((double)r.getCapacity() / giga);
                    repository.setFreeSpace((double)r.getFreeSpace() / giga);
                    ret.add(repository);
                }
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
        private int vmCount;
        private int tenantCount;

    }
}
