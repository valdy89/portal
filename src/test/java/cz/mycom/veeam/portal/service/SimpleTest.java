package cz.mycom.veeam.portal.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import cz.mycom.veeam.portal.idoklad.CountryData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author dursik
 */
@Slf4j
public class SimpleTest {
    @Test
    public void testJson() throws Exception {
        InputStream resourceAsStream = SimpleTest.class.getResourceAsStream("/countries.json");
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        //objectMapper.configure(MapperFeature.USE_STD_BEAN_NAMING, true);
        CountryData countryData = objectMapper.readValue(IOUtils.toByteArray(resourceAsStream), CountryData.class);
        log.info("CountryData: " + countryData);
    }
}
