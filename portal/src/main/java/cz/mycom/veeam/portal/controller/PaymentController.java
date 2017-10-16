package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.model.PaymentStatusEnum;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.ConfigRepository;
import cz.mycom.veeam.portal.repository.OrderRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.MerchantService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@Controller
@Transactional
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantService merchantService;

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    @RequestMapping(value = "/ok", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void ok(@RequestBody MultiValueMap<String, String> multiValueMap, Principal principal, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("Principal: " + principal);
        boolean result = merchantService.checkTransaction(request, multiValueMap.get("trans_id").get(0));
        response.sendRedirect(result ? "/?payment=success" : "/?payment=failed");
    }

    @RequestMapping(value = "/fail", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void fail(@RequestBody MultiValueMap<String, String> multiValueMap, Principal principal, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("Principal: " + principal);
        merchantService.checkTransaction(request, multiValueMap.get("trans_id").get(0));
        response.sendRedirect("/?payment=failed");
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public OrderResponse create(@RequestBody OrderRequest orderRequest, Principal principal, HttpServletRequest request) throws Exception {
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

        OrderResponse orderResponse = new OrderResponse();

        Order order = new Order();
        order.setTenantUid(tenant.getUid());
        BigDecimal price = orderRequest.getPrice();
        order.setPrice(price);
        BigDecimal dph = new BigDecimal(configRepository.getOne("dph.percent").getValue()).divide(HUNDRED);
        BigDecimal finalPrice = price.add(price.multiply(dph)).setScale(0, RoundingMode.HALF_UP);
        order.setPriceWithVat(finalPrice);
        BigDecimal creditCount = price.divide(new BigDecimal("2.5"), BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.TEN);
        order.setCredit(creditCount.setScale(0, BigDecimal.ROUND_FLOOR).intValue());
        order.setDateCreated(new Date());
        if (orderRequest.getType() == 0) {
            String transId = merchantService.startSMSTrans(request, finalPrice, "Nákup kreditů: " + order.getCredit() + "ks");
            order.setTransId(transId);
            order.setPaymentStatus(PaymentStatusEnum.Unpaid);
            orderResponse.setBankUrl(configRepository.getOne("bank.server.url").getValue() + "?trans_id=" + URLEncoder.encode(transId, "UTF-8"));
        }
        order = orderRepository.save(order);
        orderResponse.setOrderId(order.getId());
        return orderResponse;
    }

    @Data
    public static class BankRequest {
        private String trans_id;
    }

    @Data
    public static class OrderRequest {
        private BigDecimal price;
        private int type;
        private User user;
    }

    @Data
    public static class OrderResponse {
        private String bankUrl;
        private int orderId;
    }
}
