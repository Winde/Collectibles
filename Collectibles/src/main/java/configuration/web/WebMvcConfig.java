package configuration.web;

import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.resource.GzipResourceResolver;

@Configuration
public class WebMvcConfig extends WebMvcAutoConfigurationAdapter {
	        
    @Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    	
    	registry
    		.addResourceHandler("/app/**")
    		.addResourceLocations(
                "classpath:/static/resources/")
    		.setCachePeriod(60*60*3)
    		.resourceChain(true)
    		.addResolver(new GzipResourceResolver());    	
	}
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/app/").setViewName("forward:/app/index.html");
    }
    
}