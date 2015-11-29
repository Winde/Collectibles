package main;

import java.util.concurrent.ScheduledExecutorService;

import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import model.connection.ContinuousScrapper;
import model.connection.amazon.AmazonConnector;
import model.connection.goodreads.GoodReadsConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.google.common.cache.CacheBuilder;

import configuration.security.jwt.JwtAuthenticationOnSuccess;
import configuration.security.jwt.TokenAuthenticationService;


@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan({
	"web.controllers",
	"configuration",
	"model.authentication",
	"model.connection",
	"model.dataobjects.events",
	"model.persistence.queues",
	"model.dataobjects.serializable.services"
})
@EnableCaching
@EnableJpaRepositories(basePackages = {"model.persistence"})
@EntityScan("model.dataobjects")
@PropertySource("classpath:amazon.properties")
@PropertySource("classpath:goodreads.properties")
@PropertySource("classpath:boardgamegeek.properties")
@PropertySource("classpath:ebay.properties")
@PropertySource("classpath:steam.properties")
@PropertySource("classpath:reddis.properties")
@PropertySource(value = {
        "classpath:reddis.properties",
        "classpath:reddis-${spring.profiles.active:default}.properties"
})
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class CollectiblesApplication {
	
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private DataSource datasource;
	
	public static void main(String[] args) {
        SpringApplication.run(CollectiblesApplication.class, args);
    }
    
    @Bean
    public CacheManager getCacheManager() {
    	GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilder(
            CacheBuilder.
            newBuilder().            
            maximumSize(200));
        return cacheManager;           
    }
    
    @Bean
    public View jsonTemplate() {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        view.setPrettyPrint(true);
        return view;
    }
	
    @Bean
    public ViewResolver viewResolver() {
        return new BeanNameViewResolver();
    }
    
    @Bean
    public CommonsMultipartResolver commonsMultipartResolver() {
        final CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setMaxUploadSize(-1);
        return commonsMultipartResolver;
    }


    @Bean
    public ScheduledExecutorFactoryBean executorService() {
        ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
        bean.setThreadNamePrefix("schedule-");
        bean.setPoolSize(10);
        return bean;
    }
}
