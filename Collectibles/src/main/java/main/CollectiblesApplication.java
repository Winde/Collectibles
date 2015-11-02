package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan({
	"web.controllers",
	"configuration",
	"model.authentication"
})
@EnableJpaRepositories(basePackages = {"model.persistence"})
@EntityScan("model.dataobjects")
public class CollectiblesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectiblesApplication.class, args);
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
}
