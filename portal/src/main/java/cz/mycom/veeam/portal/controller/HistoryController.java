package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.TenantHistory;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.TenantHistoryRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@RequestMapping("/history")
public class HistoryController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TenantHistoryRepository tenantHistoryRepository;

    private static String[] labels = new String[]{
            "Zakoupená alokace GB", "Využívaná velikost GB", "Počet VM", "Počet Server", "Počet Workstation"
    };

    @RequestMapping(method = RequestMethod.POST)
    public HistoryResponse create(@RequestBody HistoryRequest historyRequest, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        List<TenantHistory> tenantHistoryList = tenantHistoryRepository.findByUidAndDateCreatedBetweenOrderByDateCreated(user.getTenant().getUid(), historyRequest.getFrom(), historyRequest.getTo());


        List<Integer[]> repositoryChange = new ArrayList<>();
        List<Integer> creditChange = new ArrayList<>();
        List<Date> repositoryDates = new ArrayList<>();
        List<Date> creditDates = new ArrayList<>();
        for (TenantHistory tenantHistory : tenantHistoryList) {
            Integer[] repositoryValues = {tenantHistory.getQuota() / 1024, tenantHistory.getUsedQuota() / 1024, tenantHistory.getVmCount(), tenantHistory.getServerCount(), tenantHistory.getWorkstationCount()};
            int credit = tenantHistory.getCredit();

            if (repositoryChange.isEmpty() || creditChange.isEmpty()) {
                repositoryChange.add(repositoryValues);
                creditChange.add(credit);
                repositoryDates.add(tenantHistory.getDateCreated());
                creditDates.add(tenantHistory.getDateCreated());
            } else {
                if (!Arrays.equals(repositoryValues, repositoryChange.get(repositoryChange.size() - 1))) {
                    repositoryChange.add(repositoryValues);
                    repositoryDates.add(tenantHistory.getDateCreated());
                }
                if (credit != creditChange.get(creditChange.size() - 1)) {
                    creditChange.add(credit);
                    creditDates.add(tenantHistory.getDateCreated());
                }
            }
        }
        HistoryResponse ret = new HistoryResponse();
        ret.histories = tenantHistoryList;

        List<Integer[]> pomRepoValues = new ArrayList<>();
        if (repositoryChange.isEmpty()) {
            ret.repositoryLabels.add(format(historyRequest.getFrom()));
            ret.repositoryLabels.add(format(historyRequest.getTo()));
        } else {
            int step = repositoryChange.size() / 10;
            if (step == 0) {
                step = 1;
            }
            for (int i = 0; i < repositoryChange.size(); i += step) {
                pomRepoValues.add(repositoryChange.get(i));
                ret.repositoryLabels.add(format(repositoryDates.get(i)));
            }

        }
        if (creditChange.isEmpty()) {
            ret.creditLabels.add(format(historyRequest.getFrom()));
            ret.creditLabels.add(format(historyRequest.getTo()));
        } else {
            int step = creditChange.size() / 10;
            if (step == 0) {
                step = 1;
            }
            for (int i = 0; i < creditChange.size(); i += step) {
                ret.creditData.add(creditChange.get(i));
                ret.creditLabels.add(format(creditDates.get(i)));
            }
        }

        if (!pomRepoValues.isEmpty()) {
            List<List<Integer>> repositoryData = new ArrayList<>(5);
            for (int i = 0; i < 5; i++) {
                repositoryData.add(new ArrayList<>());
            }
            for (Integer[] pom : pomRepoValues) {
                for (int i = 0; i < 5; i++) {
                    List<Integer> list = repositoryData.get(i);
                    if (list == null) {
                        list  = new ArrayList<>();
                        repositoryData.add(list);
                    }
                    list.add(pom[i]);
                }
            }

            for (int i = 0; i < repositoryData.size(); i++) {
                List<Integer> list = repositoryData.get(i);
                boolean forRemove = false;
                if (i >= 2) {
                    forRemove = true;
                    for (Integer pom : list) {
                        if (pom > 0) {
                            forRemove = false;
                            break;
                        }
                    }
                }
                if (!forRemove) {
                    ret.repositoryData.add(list);
                    ret.repositorySeries.add(labels[i]);
                }
            }
        }


        return ret;
    }

    private String format(Date to) {
        return DateFormatUtils.format(to, "dd.MM.yyyy");
    }

    @Data
    public static class HistoryResponse {
        private List<TenantHistory> histories = new ArrayList<>();
        private List<String> repositoryLabels = new ArrayList<>();
        private List<String> creditLabels = new ArrayList<>();
        private List<List<Integer>> repositoryData = new ArrayList<>();
        private List<Integer> creditData = new ArrayList<>();
        private List<String> repositorySeries = new ArrayList<>();
    }

    @Data
    public static class HistoryRequest {
        private Date from;
        private Date to;
    }
}
