package cz.mycom.veeam.portal.service;

import java.util.List;
import java.util.stream.Collectors;

import com.veeam.ent.v1.CloudSubtenant;
import com.veeam.ent.v1.CloudSubtenantCreateSpec;
import com.veeam.ent.v1.CloudSubtenants;
import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.CloudTenantFreeLicenseCounters;
import com.veeam.ent.v1.CloudTenantResources;
import com.veeam.ent.v1.CreateCloudTenantResourceSpec;
import com.veeam.ent.v1.CreateCloudTenantSpec;
import com.veeam.ent.v1.EntityReferences;
import com.veeam.ent.v1.LogonSession;
import com.veeam.ent.v1.Repository;
import com.veeam.ent.v1.RepositoryReportFrame;
import com.veeam.ent.v1.Task;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import cz.mycom.veeam.portal.model.Config;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;

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

	@Retryable(maxAttempts = 10)
	public EntityReferences tenants() {
		EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("cloud/tenants"), EntityReferences.class);
		log.debug("Tenants: " + referenceListType);
		return referenceListType;
	}

	@Retryable(maxAttempts = 10)
	public CloudTenant createTenant(CreateCloudTenantSpec cloudTenant) {
		Task taskType = veeamRestTemplate.postForObject(getUrl("cloud/tenants"), cloudTenant, Task.class);
		taskType = waitForTast(taskType);
		String url = taskType.getLinks().getLinks().stream().filter(l -> "Related".equals(l.getRel())).findFirst().get().getHref();
		return veeamRestTemplate.getForObject(url, CloudTenant.class);
	}

	@Retryable(maxAttempts = 10)
	public void saveTenant(String uid, CloudTenant tenant) {
		ResponseEntity<Task> taskType = veeamRestTemplate.exchange(getUrl("cloud/tenants/{tenantUid}"), HttpMethod.PUT, new HttpEntity<CloudTenant>(tenant), Task.class, uid);
		waitForTast(taskType.getBody());
	}

	@Retryable(maxAttempts = 10)
	public CloudSubtenant createSubtenant(String tenantUid, CloudSubtenantCreateSpec subtenantCreateSpec) {
		Task taskType = veeamRestTemplate.postForObject(getUrl("cloud/tenants/" + tenantUid + "/subtenants"), subtenantCreateSpec, Task.class);
		waitForTast(taskType);
		int i = 0;
		while (true) {
			for (CloudSubtenant cloudSubtenant : getSubtenants(tenantUid)) {
				if (cloudSubtenant.getName().equals(subtenantCreateSpec.getName())) {
					return cloudSubtenant;
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
			}
			if (++i > 1000) {
				throw new RuntimeException("No response");
			}
		}
	}

	@Retryable(maxAttempts = 10)
	public void saveSubtenant(String tenantUid, String subtenantUid, CloudSubtenant subtenant) {
		ResponseEntity<Task> taskType = veeamRestTemplate.exchange(getUrl("cloud/tenants/{tenantUid}/subtenants/{subtenantUid}"), HttpMethod.PUT, new HttpEntity<CloudSubtenant>(subtenant), Task.class, tenantUid, subtenantUid);
		waitForTast(taskType.getBody());
	}

	@Retryable(maxAttempts = 10)
	public void deteleSubtenant(String tenantUid, String uid) {
		ResponseEntity<Task> taskType = veeamRestTemplate.exchange(getUrl("cloud/tenants/{tenantUid}/subtenants/{uid}"), HttpMethod.DELETE, null, Task.class, tenantUid, uid);
		waitForTast(taskType.getBody());
	}

	@Retryable(maxAttempts = 10)
	public CloudSubtenant getSubtenant(String tenantUid, String subtenantUid) {
		return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{tenantUid}/subtenants/{subtenantUid}"), CloudSubtenant.class, tenantUid, subtenantUid);
	}

	@Retryable(maxAttempts = 10)
	public List<CloudTenant> getTenants() {
		EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("cloud/tenants"), EntityReferences.class);
		return referenceListType.getReves()
				.stream()
				.map(r -> veeamRestTemplate.getForObject(r.getHref() + "?format=Entity", CloudTenant.class))
				.collect(Collectors.toList());
	}

	@Retryable(maxAttempts = 10)
	public CloudTenantFreeLicenseCounters getCloudTenantFreeLicenseCounters(String uid) {
		return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{uid}/freelicenseCounters"), CloudTenantFreeLicenseCounters.class, uid);
	}

	@Retryable(maxAttempts = 10)
	public CloudTenant getTenant(String uid) {
		return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{uid}?format=Entity"), CloudTenant.class, uid);
	}

	@Retryable(maxAttempts = 10)
	public List<CloudSubtenant> getSubtenants(String uid) {
		return veeamRestTemplate.getForObject(getUrl("/cloud/tenants/{uid}/subtenants"), CloudSubtenants.class, uid).getCloudSubtenants();
	}

	@Retryable(maxAttempts = 10)
	public CloudTenantResources getTenantResources(String uid) {
		return veeamRestTemplate.getForObject(getUrl("cloud/tenants/{uid}/resources"), CloudTenantResources.class, uid);
	}

	@Retryable(maxAttempts = 10)
	public List<Repository> getRepositories() {
		EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("repositories"), EntityReferences.class);
		return referenceListType.getReves()
				.stream()
				.filter(r -> StringUtils.startsWithIgnoreCase(r.getName(), "Scale-out"))
				.map(r -> veeamRestTemplate.getForObject(r.getHref() + "?format=Entity", Repository.class))
				.collect(Collectors.toList());
	}

	@Retryable(maxAttempts = 10)
	public List<RepositoryReportFrame.Period> getRepositoryReport() {
		RepositoryReportFrame repositoryReportFrame = veeamRestTemplate.getForObject(getUrl("reports/summary/repository"), RepositoryReportFrame.class);
		return repositoryReportFrame.getPeriods().stream()
				.filter(r -> StringUtils.startsWithIgnoreCase(r.getName(), "Scale-out"))
				.collect(Collectors.toList());
	}

	@Retryable(maxAttempts = 10)
	public void createResource(String tenantUid, CreateCloudTenantResourceSpec backupResource) {
		Task taskType = veeamRestTemplate.postForObject(getUrl("cloud/tenants/" + tenantUid + "/resources"), backupResource, Task.class);
		waitForTast(taskType);
	}

	@Retryable(maxAttempts = 10)
	public Repository getPreferredRepository(int quota) {
		List<Repository> repositories = getRepositories();
		if (repositories.isEmpty()) {
			throw new IllegalStateException("None repository found");
		}
		float filledParam = getFilledParam();

		Repository fallback = null;
		for (Repository repository : repositories) {
			Integer sumQuota = tenantRepository.sumQuota(StringUtils.substringAfterLast(repository.getUID(), ":"), "NOT_EXISTING");
			if (sumQuota == null) {
				sumQuota = quota;
			} else {
				sumQuota += quota;
			}
			if (sumQuota <= (repository.getCapacity() / Math.pow(1024, 2)) * filledParam) {
				//nasel jsem misto
				return repository;
			}
			if (fallback == null || repository.getCapacity() > fallback.getCapacity()) {
				fallback = repository;
			}
		}
		return fallback;
	}

	@Retryable(maxAttempts = 10)
	public Repository getRepository(String uid) {
		return veeamRestTemplate.getForObject(getUrl("repositories/{uid}?format=Entity"), Repository.class, uid);
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

	@Retryable(maxAttempts = 10)
	public String getBackupServerUUID() {
		EntityReferences referenceListType = veeamRestTemplate.getForObject(getUrl("backupServers"), EntityReferences.class);
		return referenceListType.getReves()
				.stream()
				.findFirst().get().getUID();
	}

	@Retryable(maxAttempts = 10)
	public LogonSession logonSystem() {
		try {
			return logonSession(Base64.encodeBase64String((veeamApiUser + ":" + keyStoreService.readData("veeam.api.password")).getBytes("UTF-8")));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private LogonSession logonSession(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + token);
		ResponseEntity<LogonSession> responseEntity = veeamRestTemplate.exchange(getUrl("sessionMngr/?v=latest"), HttpMethod.POST, new HttpEntity<>(headers), LogonSession.class);
		LogonSession logonSession = responseEntity.getBody();
		log.debug("LogonSession: " + logonSession.getSessionId());
		SessionIdThreadLocal.setSessionId(responseEntity.getHeaders().getFirst("X-RestSvcSessionId"));
		return logonSession;
	}

	public void logout(LogonSession logonSession) {
		if (logonSession == null) {
			return;
		}
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
		} finally {
			SessionIdThreadLocal.clean();
		}
	}


	private String getUrl(String path) {
		return veeamApiUrl + path;
	}

	private Task waitForTast(Task taskType) {
		int i = 0;
		while (!"Finished".equals(taskType.getState())) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
			}
			try {
				taskType = veeamRestTemplate.getForObject(taskType.getHref(), Task.class);
				log.debug("TaskType: " + taskType.getState());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			if (++i > 1000) {
				throw new IllegalStateException(taskType.getResult().getMessage());
			}
		}
		if (!taskType.getResult().isSuccess()) {
			throw new IllegalStateException(taskType.getResult().getMessage());
		}
		return taskType;
	}
}
