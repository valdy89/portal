package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.Order;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.OrderRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.IDokladService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.security.Principal;
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

    @RequestMapping(method = RequestMethod.GET)
    public List<Order> list(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return orderRepository.findByTenantUidOrderByDateCreatedDesc(user.getTenant().getUid());
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
}
