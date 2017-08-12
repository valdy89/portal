package cz.mycom.veeam.portal.idoklad;

import lombok.Getter;
import lombok.Setter;

/**
 * @author dursik
 */
@Getter
@Setter
public class Bank extends AbstractObject {
    String Code;
    int CountryId;
    boolean IsOutOfDate;
    String Name;
    String NumberCode;
    String Swift;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Bank{");
        sb.append("Id='").append(Id).append('\'');
        sb.append(", Code='").append(Code).append('\'');
        sb.append(", CountryId=").append(CountryId);
        sb.append(", IsOutOfDate=").append(IsOutOfDate);
        sb.append(", Name='").append(Name).append('\'');
        sb.append(", NumberCode='").append(NumberCode).append('\'');
        sb.append(", Swift='").append(Swift).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
