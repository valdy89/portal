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
public class AbstractInvoiceInsert extends AbstractObject {
    int PurchaserId;
    String VariableSymbol;
    String OrderNumber;
    List<InvoiceItem> ProformaInvoiceItems = new ArrayList<InvoiceItem>();

    String AccountNumber;
    Integer BankId;
    String BankName;
    String BankNumberCode;
    Integer ConstantSymbolId;
    Integer CurrencyId;
    String DateOfIssue;
    String DateOfMaturity;
    String DateOfPayment;
    String DateOfTaxing;
    String Description;
    String DocumentNumber;
    Integer DocumentSerialNumber;
    Integer EetResponsibility;
    String Iban;
    String Swift;
}
