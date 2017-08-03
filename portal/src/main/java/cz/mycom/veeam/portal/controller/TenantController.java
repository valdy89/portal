package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.model.TenantHistory;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.model.UserHistory;
import cz.mycom.veeam.portal.repository.*;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private ConfigRepository configRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantHistoryRepository tenantHistoryRepository;
    @Autowired
    private UserHistoryRepository userHistoryRepository;

    @RequestMapping(method = RequestMethod.GET)
    public Tenant get(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        Tenant tenant = user.getTenant();
        if (tenant == null) {
            tenant = new Tenant();
            tenant.setUser(user);
        }
        int credit = user.getCredit();
        Calendar cal = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int priceQuota = Integer.parseInt(configRepository.getOne("price.quota").getValue());
        int priceVm = Integer.parseInt(configRepository.getOne("price.vm").getValue());
        int priceServer = Integer.parseInt(configRepository.getOne("price.server").getValue());
        int privateWorkstation = Integer.parseInt(configRepository.getOne("price.workstation").getValue());
        while (credit > 0) {
            int change = credit;
            if (month != cal.get(Calendar.MONTH)) {
                credit -= tenant.getVmCount() * priceVm;
                credit -= tenant.getServerCount() * priceServer;
                credit -= tenant.getWorkstationCount() * privateWorkstation;
                month = cal.get(Calendar.MONTH);
            }
            credit -= Math.ceil(((float) tenant.getQuota() / 1024 / 10) * priceQuota);
            if (credit <= 0) {
                break;
            }
            change -= credit;
            if (change <= 0) {
                cal = null;
                break;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        if (cal != null) {
            tenant.setCreditDate(cal.getTime());
        } else {
            tenant.setCreditDate(new Date());
        }
        return tenant;
    }

    @RequestMapping(method = RequestMethod.POST)
    public Tenant save(Principal principal, @RequestBody Change change) {
        User user = userRepository.findByUsername(principal.getName());
        Tenant tenant = user.getTenant();
        if (change.getCredit() != null) {
            UserHistory userHistory = new UserHistory(user, principal.getName());
            int credit = user.getCredit() + change.getCredit();
            user.setCredit(credit);
            userHistoryRepository.save(userHistory);
        } else {
            LogonSession logonSession = veeamService.logonSystem();
            try {
                if (tenant == null) {
                    List<String> descriptionList = new ArrayList<>();
                    descriptionList.add("Email: " + user.getUsername());
                    if (StringUtils.isNotBlank(user.getCompanyName())) {
                        descriptionList.add("Company: " + user.getCompanyName());
                    }
                    descriptionList.add("Portal automatically created user - " + DateFormatUtils.format(new Date(), "dd.MM.yyyy HH:mm:ss"));

                    tenant = new Tenant();
                    tenant.setUserId(user.getId());
                    tenant.setDateCreated(new Date());
                    tenant.setEnabled(true);

                    String name = StringUtils.substringBefore(principal.getName(), "@");
                    name += RandomStringUtils.randomNumeric(5);

                    tenant.setUsername(name);

                    CreateCloudTenantSpec cloudTenant = new CreateCloudTenantSpec();
                    cloudTenant.setName(name);
                    cloudTenant.setDescription(StringUtils.join(descriptionList, IOUtils.LINE_SEPARATOR_WINDOWS));

                    cloudTenant.setPassword(change.getPassword());
                    cloudTenant.setEnabled(true);
                    cloudTenant.setThrottlingEnabled(false);
                    cloudTenant.setMaxConcurrentTasks(5);
                    cloudTenant.setBackupServerUid(veeamService.getBackupServerUUID());

                    if (change.getQuota() != null && change.getQuota() > 0) {
                        Repository repository = veeamService.getPreferredRepository(change.getQuota());
                        if (repository == null) {
                            sendMail(tenant, tenant.getQuota());
                        } else {
                            cloudTenant.setResources(new CreateCloudTenantResourceListType());
                            CreateCloudTenantResourceSpec backupResource = new CreateCloudTenantResourceSpec();
                            backupResource.setQuotaMb(change.getQuota());
                            backupResource.setName(user.getUsername() + " - BackupResource");
                            backupResource.setRepositoryUid(repository.getUID());
                            cloudTenant.getResources().getBackupResources().add(backupResource);
                            tenant.setRepositoryUid(StringUtils.substringAfterLast(repository.getUID(), ":"));
                        }
                    }

                    CloudTenant saveTenant = veeamService.createTenant(cloudTenant);
                    tenant.setUid(StringUtils.substringAfterLast(saveTenant.getUID(), ":"));
                    tenant = tenantRepository.save(tenant);
                } else {
                    CloudTenant cloudTenant = veeamService.getTenant(tenant.getUid());
                    cloudTenant.setEnabled(tenant.isEnabled());
                    if (StringUtils.isNotBlank(tenant.getPassword())) {
                        cloudTenant.setPassword(tenant.getPassword());
                    } else {
                        cloudTenant.setPassword(null);
                    }
                    if (change.getQuota() != null && change.getQuota() > 0) {
                        if (StringUtils.isNotBlank(tenant.getRepositoryUid())) {
                            Repository repository = veeamService.getRepository(tenant.getRepositoryUid());
                            Integer sumQuota = tenantRepository.sumQuota(tenant.getRepositoryUid(), tenant.getUid());
                            if (sumQuota == null) {
                                sumQuota = change.getQuota();
                            } else {
                                sumQuota += change.getQuota();
                            }
                            if (sumQuota > (repository.getCapacity() / Math.pow(1024, 2)) * veeamService.getFilledParam()) {
                                sendMail(tenant, tenant.getQuota());
                                throw new RuntimeException("Tento požadavek nelze z technických důvodů momentálně splnit automaticky. Byla kontaktována podpora dodavatele. Budete kontaktováni v nejkratším možném termínu.");
                            }

                            if (change.getQuota() != null) {
                                CloudTenantResources cloudTenantResources = cloudTenant.getResources();
                                if (cloudTenantResources != null
                                        && !CollectionUtils.isEmpty(cloudTenantResources.getCloudTenantResources())) {
                                    CloudTenantResource cloudTenantResource = cloudTenantResources.getCloudTenantResources().get(0);
                                    cloudTenantResource.getRepositoryQuota().setQuota(Long.valueOf(change.getQuota()));
                                }
                            }

                        } else {
                            Repository repository = veeamService.getPreferredRepository(change.getQuota());
                            if (repository == null) {
                                sendMail(tenant, tenant.getQuota());
                            } else {
                                CreateCloudTenantResourceSpec backupResource = new CreateCloudTenantResourceSpec();
                                backupResource.setQuotaMb(change.getQuota());
                                backupResource.setName(user.getUsername() + " - BackupResource");
                                backupResource.setRepositoryUid(repository.getUID());
                                tenant.setRepositoryUid(StringUtils.substringAfterLast(repository.getUID(), ":"));
                                veeamService.createResource(tenant.getUid(), backupResource);
                            }
                        }
                        veeamService.saveTenant(tenant.getUid(), cloudTenant);
                    }
                }

                if (change.getQuota() != null && tenant.getQuota() != change.getQuota()) {
                    int diff = change.getQuota() - tenant.getQuota();
                    TenantHistory tenantHistory = new TenantHistory(tenant, principal.getName());
                    tenantHistoryRepository.save(tenantHistory);
                    tenant.setQuota(change.getQuota());

                    int credit = user.getCredit();
                    //kdyz je vic tak ho zkasni
                    if (diff > 0) {
                        int priceQuota = Integer.parseInt(configRepository.getOne("price.quota").getValue());
                        credit -= Math.ceil(((float) diff / 1024 / 10) * priceQuota);
                    }
                    if (credit != user.getCredit()) {
                        UserHistory userHistory = new UserHistory(user, principal.getName());
                        userHistoryRepository.save(userHistory);
                        user.setCredit(credit);
                    }
                }

            } finally {
                veeamService.logout(logonSession);
            }
        }
        return tenant;
    }

    private void sendMail(Tenant tenant, int quota) {

    }

    @Data
    public static class Change {
        private Integer quota;
        private String password;
        private Integer credit;
    }
}
