package cz.mycom.veeam.portal.idoklad;

import cz.mycom.veeam.portal.model.PaymentStatusEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author dursik
 */
@Getter
@Setter
public class AbstractInvoice extends AbstractObject {
    int PurchaserId;
    String VariableSymbol;
    String OrderNumber;
    String DateOfIssue;
    String DateOfMaturity;
    String DateOfPayment;
    PaymentStatusEnum PaymentStatus;
    String documentNumber;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProformaInvoice{");
        sb.append("Id=").append(Id);
        sb.append(", PurchaserId=").append(PurchaserId);
        sb.append(", VariableSymbol='").append(VariableSymbol).append('\'');
        sb.append(", OrderNumber='").append(OrderNumber).append('\'');
        sb.append(", DateOfIssue=").append(DateOfIssue);
        sb.append(", DateOfMaturity=").append(DateOfMaturity);
        sb.append(", DateOfPayment=").append(DateOfPayment);
        sb.append(", PaymentStatus=").append(PaymentStatus);
        sb.append('}');
        return sb.toString();
    }
}
