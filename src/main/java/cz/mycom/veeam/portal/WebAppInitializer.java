package cz.mycom.veeam.portal;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @author dursik
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{AppConfig.class};
    }

    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[0];
    }

    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
