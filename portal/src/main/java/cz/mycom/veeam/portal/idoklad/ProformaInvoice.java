package cz.mycom.veeam.portal.idoklad;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Getter
@Setter
public class ProformaInvoice extends AbstractObject {
    int PurchaserId;
    String VariableSymbol;
    String OrderNumber;
    Date DateOfIssue;
    Date DateOfMaturity;
    Date DateOfPayment;
    PaymentStatusEnum PaymentStatus;

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
