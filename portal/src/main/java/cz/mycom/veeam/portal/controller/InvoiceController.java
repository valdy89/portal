package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.repository.OrderRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.IDokladService;
import cz.mycom.veeam.portal.service.MailService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@Transactional
@RequestMapping("/invoice")
public class InvoiceController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IDokladService iDokladService;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private MailService mailService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Order> list(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return orderRepository.findByTenantUidOrderByDateCreatedDesc(user.getTenant().getUid());
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody OrderRequest orderRequest, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        Tenant tenant = user.getTenant();
        List<Order> unpaid = orderRepository.findUnpaid(tenant.getUid());
        if (!unpaid.isEmpty()) {
            throw new RuntimeException("Máte nezaplacenou fakturu");
        }

        User requestUser = orderRequest.getUser();
        if (requestUser != null) {
            user.setName(requestUser.getName());
            user.setStreet(requestUser.getStreet());
            user.setCity(requestUser.getCity());
            user.setPostalCode(requestUser.getPostalCode());
            user.setIco(requestUser.getIco());
            user.setDic(requestUser.getDic());
        }

        Order order = new Order();
        order.setTenantUid(tenant.getUid());
        BigDecimal price = orderRequest.getPrice();
        order.setPrice(price);
        BigDecimal creditCount = price.divide(new BigDecimal("3"), BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.TEN);
        order.setCredit(creditCount.setScale(0, BigDecimal.ROUND_FLOOR).intValue());
        order.setDateCreated(new Date());
        orderRepository.save(order);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public void getFile(@PathVariable("id") Integer id, HttpServletResponse response, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        Order order = orderRepository.findByTenantUidAndId(user.getTenant().getUid(), id);
        if (order == null) {
            throw new RuntimeException("WTF");
        }
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + order.getDocumentNumber() + ".pdf");
            String pdf = null;
            if (order.getInvoiceId() != null) {
                pdf = iDokladService.getInvoicePdf(order.getInvoiceId());
            } else {
                pdf = iDokladService.getProformaPdf(order.getProformaId());
            }
            IOUtils.write(Base64.decodeBase64(pdf), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }

    }

    @Data
    public static class OrderRequest {
        private BigDecimal price;
        private int type;
        private User user;
    }
}
