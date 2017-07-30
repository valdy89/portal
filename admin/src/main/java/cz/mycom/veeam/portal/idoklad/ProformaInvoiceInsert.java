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
public class ProformaInvoiceInsert extends AbstractObject {
    int PurchaserId;
    String VariableSymbol;
    String OrderNumber;
    List<InvoiceItem> ProformaInvoiceItems = new ArrayList<InvoiceItem>();
}
