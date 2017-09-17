import lv.konts.ecomm.merchant.Merchant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

/**
 * @author dursik
 */
public class EcommMerchant {
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.load(EcommMerchant.class.getResourceAsStream("merchant.properties"));
            Merchant merchant = new Merchant(properties);
            //String result = merchant.startSMSTrans("100", "203", "89.233.175.177", "test");
            //String result = merchant.getTransResult("\tXj3TGbrUk1InKISNIi2RHduTNP4=","89.233.175.177");
            String result = merchant.closeDay();
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            return;
        }
    }
}
