package configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, proxyTargetClass = true)
public class WebsecurityConfig extends WebSecurityConfigurerAdapter {


	@Autowired
	private RestAuthenticationEntryPoint entryPoint;

	@Autowired
	private RestAccessDeniedHandler handler;

	
	//@Autowired 
	//private AuthenticationService authenticationService; 
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    http    	        	
      	.csrf().disable()      	
      	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    .and()
    	.httpBasic()
    .and()
    	.authorizeRequests()
    	.antMatchers("/app/**").permitAll()		
	.and()
		.exceptionHandling()
			//.authenticationEntryPoint(entryPoint)
	    	.accessDeniedHandler(handler);    	
	    
		    
      	
      
    /*
      .and().
      
    
        .authorizeRequests()
          .antMatchers("/app/**").permitAll()
          .anyRequest().authenticated();
    */
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER","ADMIN");
    }

	

    
}
