package cz.mycom.veeam.portal.idoklad;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author dursik
 */
@Getter
@Setter
public abstract class AbstractObject implements Serializable {
    Integer Id;
}
