package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.LogonSession;
import com.veeam.ent.v1.RepositoryReportFrame;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public List<RepositoryReportFrame.Period> list() {
        LogonSession logonSession = veeamService.logonSystem();
        try {
            return veeamService.getRepositoryReport();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            veeamService.logout(logonSession.getSessionId());
        }
    }
}
