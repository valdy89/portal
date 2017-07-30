package cz.mycom.veeam.portal.idoklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dursik
 */
@Getter
@Setter
public abstract class AbstractData<T extends AbstractObject> {
    int TotalItems;
    int TotalPages;
    List<T> Data = new ArrayList<T>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "{");
        sb.append("TotalItems=").append(TotalItems);
        sb.append(", TotalPages=").append(TotalPages);
        sb.append(", Data=").append(Data);
        sb.append('}');
        return sb.toString();
    }
}
