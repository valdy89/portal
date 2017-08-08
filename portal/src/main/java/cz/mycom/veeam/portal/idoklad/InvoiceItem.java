package cz.mycom.veeam.portal.idoklad;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author dursik
 */
@Getter
@Setter
public class InvoiceItem extends AbstractObject {
    BigDecimal Amount;
    String Name;
    PriceTypeEnum PriceType;
    String Unit;
    BigDecimal UnitPrice;
    VatRateTypeEnum VatRateType;
}
