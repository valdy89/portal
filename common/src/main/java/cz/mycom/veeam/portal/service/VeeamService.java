package cz.mycom.veeam.portal.service;

import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.model.Config;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dursik
 */
@Slf4j
@Service
public class VeeamService {

    @Value("${veeam.api.url}")
    private String veeamApiUrl;

    @Value("${veeam.api.user}")
    private String veeamApiUser;

    @Autowired
    private KeyStoreService keyStoreService;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RestTemplate veeamRestTemplate;

    @Secured("ROLE_SYSTEM")
    public EntityReferences tenants() {
        EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("cloud/tenants"), EntityReferences.class);
        log.debug("Tenants: " + referenceListType);
        return referenceListType;
    }

    @Secured("ROLE_SYSTEM")
    public CloudTenant createTenant(CreateCloudTenantSpec cloudTenant) {
        try {
            Task taskType = veeamRestTemplate.postForObject(getUrl("cloud/tenants"), cloudTenant, Task.class);
            taskType = waitForTast(taskType);
            String url = taskType.getLinks().getLinks().stream().filter(l -> "Related".equals(l.getRel())).findFirst().get().getHref();
            return veeamRestTemplate.getForObject(url, CloudTenant.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Secured("ROLE_SYSTEM")
    public void saveTenant(String uid, CloudTenant tenant) {
        try {
            ResponseEntity<Task> taskType = veeamRestTemplate.exchange(getUrl("cloud/tenants/{tenantUid}"), HttpMethod.PUT, new HttpEntity<CloudTenant>(tenant), Task.class, uid);
            waitForTast(taskType.getBody());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Secured("ROLE_SYSTEM")
    public CloudSubtenant createSubtenant(String tenantUid, CloudSubtenantCreateSpec subtenantCreateSpec) {
        try {
            Task taskType = veeamRestTemplate.postForObject(getUrl("cloud/tenants/" + tenantUid + "/subtenants"), subtenantCreateSpec, Task.class);
            waitForTast(taskType);
            return veeamRestTemplate.getForObject("/cloud/tenants/" + tenantUid + "/subtenants/" + subtenantCreateSpec.getName(), CloudSubtenant.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Secured("ROLE_SYSTEM")
    public void saveSubtenant(String tenantUid, String subtenantUid, CloudSubtenant subtenant) {
        try {
            ResponseEntity<Task> taskType = veeamRestTemplate.exchange(getUrl("cloud/tenants/{tenantUid}/subtenants/{subtenantUid}"), HttpMethod.PUT, new HttpEntity<CloudSubtenant>(subtenant), Task.class, tenantUid, subtenantUid);
            waitForTast(taskType.getBody());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CloudSubtenant getSubtenant(String tenantUid, String subtenantUid) {
        try {
            return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{tenantUid}/subtenants/{subtenantUid}"), CloudSubtenant.class, tenantUid, subtenantUid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public List<CloudTenant> getTenants() {
        try {
            EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("cloud/tenants"), EntityReferences.class);
            return referenceListType.getReves()
                    .stream()
                    .map(r -> veeamRestTemplate.getForObject(r.getHref() + "?format=Entity", CloudTenant.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CloudTenant getTenant(String uid) {
        try {
            return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{uid}?format=Entity"), CloudTenant.class, uid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public CloudSubtenants getSubtenants(String uid) {
        try {
            return veeamRestTemplate.getForObject(getUrl("/cloud/tenants/{uid}/subtenants"), CloudSubtenants.class, uid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public CloudTenantResources getTenantResources(String uid) {
        try {
            return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{uid}/resources"), CloudTenantResources.class, uid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Secured("ROLE_SYSTEM")
    public List<Repository> getRepositories() {
        try {
            EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("repositories"), EntityReferences.class);
            return referenceListType.getReves()
                    .stream()
                    .filter(r -> StringUtils.startsWithIgnoreCase(r.getName(), "Scale-out"))
                    .map(r -> veeamRestTemplate.getForObject(r.getHref() + "?format=Entity", Repository.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Secured("ROLE_SYSTEM")
    public List<RepositoryReportFrame.Period> getRepositoryReport() {
        try {
            RepositoryReportFrame repositoryReportFrame = veeamRestTemplate.getForObject(getUrl("reports/summary/repository"), RepositoryReportFrame.class);
            return repositoryReportFrame.getPeriods().stream()
                    .filter(r -> StringUtils.startsWithIgnoreCase(r.getName(), "Scale-out"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void createResource(String tenantUid, CreateCloudTenantResourceSpec backupResource) {
        try {
            Task taskType = veeamRestTemplate.postForObject(getUrl("cloud/tenants/" + tenantUid + "/resources"), backupResource, Task.class);
            waitForTast(taskType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Repository getPreferredRepository(int quota) {
        List<Repository> repositories = getRepositories();
        if (repositories.isEmpty()) {
            throw new IllegalStateException("None repository found");
        }
        float filledParam = getFilledParam();

        for (Repository repository : repositories) {
            Integer sumQuota = tenantRepository.sumQuota(StringUtils.substringAfterLast(repository.getUID(), ":"), "NOT_EXISTING");
            if (sumQuota == null) {
                sumQuota = quota;
            } else {
                sumQuota += quota;
            }
            if (sumQuota <= (repository.getCapacity() / 1024) * filledParam) {
                //nasel jsem misto
                return repository;
            }
        }
        return null;
    }

    public Repository getRepository(String uid) {
        try {
            return veeamRestTemplate.getForObject(getUrl("repositories/{uid}?format=Entity"), Repository.class, uid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public float getFilledParam() {
        Config config = configRepository.findOne("filled.constant");
        float filledParam = 10;
        if (config != null) {
            filledParam = Float.parseFloat(config.getValue());
        }
        filledParam = filledParam / 100;
        return filledParam;
    }

    public String getBackupServerUUID() {
        try {
            EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("backupServers"), EntityReferences.class);
            return referenceListType.getReves()
                    .stream()
                    .findFirst().get().getUID();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public LogonSession logonSystem() {
        try {
            return logonSession(Base64.encodeBase64String((veeamApiUser + ":" + keyStoreService.readData("veeam.api.password")).getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private LogonSession logonSession(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + token);
        ResponseEntity<LogonSession> responseEntity = veeamRestTemplate.exchange(getUrl("sessionMngr/?v=latest"), HttpMethod.POST, new HttpEntity<>(headers), LogonSession.class);
        LogonSession logonSession = responseEntity.getBody();
        log.debug("LogonSession: " + logonSession.getSessionId());
        return logonSession;
    }

    public void logout(LogonSession logonSession) {
        logout(logonSession.getSessionId());
    }

    public void logout(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return;
        }
        try {
            veeamRestTemplate.delete(getUrl("logonSessions/{sessionId}"), sessionId);
        } catch (Exception e) {
            log.warn("Logout exception: {}", e.getMessage());
        }
    }


    private String getUrl(String path) {
        return veeamApiUrl + path;
    }

    private Task waitForTast(Task taskType) throws InterruptedException {
        while (!"Finished".equals(taskType.getState())) {
            Thread.sleep(100);
            taskType = veeamRestTemplate.getForObject(taskType.getHref(), Task.class);
            log.debug("TaskType: " + taskType.getState());
            //FIXME kdyz to vytuhne tak neco udelat
        }
        if (!taskType.getResult().isSuccess()) {
            throw new IllegalStateException(taskType.getResult().getMessage());
        }
        return taskType;
    }
}
