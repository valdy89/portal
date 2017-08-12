package cz.mycom.veeam.portal.idoklad;

import lombok.Getter;
import lombok.Setter;

/**
 * @author dursik
 */
@Getter
@Setter
public class Country extends AbstractObject {
    String Code;
    String Name;
    String NameEnglish;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Country{");
        sb.append("Id='").append(Id).append('\'');
        sb.append(", Code='").append(Code).append('\'');
        sb.append(", Name='").append(Name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
