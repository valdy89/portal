package cz.mycom.veeam.portal.idoklad;

import cz.mycom.veeam.portal.model.CreditCheckEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author dursik
 */
@Getter
@Setter
public class Contact extends AbstractObject {
    String City;
    String CompanyName;
    int CountryId;
    String Email;
    String IdentificationNumber;
    String PostalCode;
    String Street;
    String VatIdentificationNumber;
    String Surname;
    String Firstname;
    String Phone;
    CreditCheckEnum CreditCheck;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Contact{");
        sb.append("Id='").append(Id).append('\'');
        sb.append(", City='").append(City).append('\'');
        sb.append(", CompanyName='").append(CompanyName).append('\'');
        sb.append(", CountryId=").append(CountryId);
        sb.append(", Email='").append(Email).append('\'');
        sb.append(", IdentificationNumber='").append(IdentificationNumber).append('\'');
        sb.append(", PostalCode='").append(PostalCode).append('\'');
        sb.append(", Street='").append(Street).append('\'');
        sb.append(", VatIdentificationNumber='").append(VatIdentificationNumber).append('\'');
        sb.append(", Surname='").append(Surname).append('\'');
        sb.append(", Firstname='").append(Firstname).append('\'');
        sb.append(", Phone='").append(Phone).append('\'');
        sb.append(", CreditCheck=").append(CreditCheck);
        sb.append('}');
        return sb.toString();
    }
}
