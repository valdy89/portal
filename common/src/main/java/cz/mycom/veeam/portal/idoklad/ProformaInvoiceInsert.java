package cz.mycom.veeam.portal.idoklad;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dursik
 */
@Getter
@Setter
public class ProformaInvoiceInsert extends AbstractInvoiceInsert {
    List<InvoiceItem> ProformaInvoiceItems = new ArrayList<InvoiceItem>();
}
