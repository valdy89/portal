package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.*;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.TenantHistory;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.repository.TenantHistoryRepository;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Calendar;
import java.util.Date;

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

    @RequestMapping(method = RequestMethod.GET)
    public Tenant get(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        Tenant tenant = user.getTenant();
        if (tenant == null) {
            tenant = new Tenant();
            tenant.setUser(user);
        }
        int credit = tenant.getCredit();
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
        TenantHistory tenantHistory = null;
        if (change.getCredit() != null) {
            int credit = tenant.getCredit() + change.getCredit();
            tenant.setCredit(credit);
            tenantHistory = new TenantHistory(tenant, principal.getName());
        } else {
            LogonSession logonSession = veeamService.logonSystem();
            try {

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

                if (change.getQuota() != null && tenant.getQuota() != change.getQuota()) {
                    int diff = change.getQuota() - tenant.getQuota();
                    tenant.setQuota(change.getQuota());

                    Integer todayMax = tenantHistoryRepository.getTodayMaxQuota(tenant.getUid());
                    if (diff > 0 && (todayMax == null || todayMax < tenant.getQuota())) {
                        int credit = tenant.getCredit();
                        //kdyz je vic tak ho zkasni
                        int priceQuota = Integer.parseInt(configRepository.getOne("price.quota").getValue());
                        credit -= Math.ceil(((float) diff / 1024 / 10) * priceQuota);

                        if (credit != tenant.getCredit()) {
                            tenant.setCredit(credit);
                        }
                    }
                    tenantHistory = new TenantHistory(tenant, principal.getName());
                }

            } finally {
                veeamService.logout(logonSession);
            }
        }

        if (tenantRepository != null) {
            tenantHistoryRepository.save(tenantHistory);
        }
        //kdyz nemam credity tak zakazat
        if (tenant.getCredit() < 0) {
            LogonSession logonSession = veeamService.logonSystem();
            try {
                CloudTenant cloudTenant = veeamService.getTenant(tenant.getUid());
                cloudTenant.setEnabled(false);
                veeamService.saveTenant(tenant.getUid(), cloudTenant);
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
