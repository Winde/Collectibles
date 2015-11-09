package configuration.security;

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
	private RestAccessDeniedHandler accessDeniedhandler;

	@Autowired
	private LogoutSuccessHandler logoutHandler;

	
	//@Autowired 
	//private AuthenticationService authenticationService; 
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    
    http    	        	
      	.csrf().disable()      	
      	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .and()
    	.httpBasic()
    .and()
    	.authorizeRequests()
    	.antMatchers("/app/**").permitAll()		
	.and()
		.exceptionHandling()
			//.authenticationEntryPoint(entryPoint)
	    	.accessDeniedHandler(accessDeniedhandler)
	.and()
	    .logout()
		    .logoutUrl("/logout")
		    .logoutSuccessHandler(logoutHandler)
		    .deleteCookies("JSESSIONID")
     .and()
     	.headers()     	
     	//.antMatchers("/image/content/**")     		
     			.cacheControl()
     				.disable();
	
    	
    /*	
    http
    // custom JSON based authentication by POST of 
 	// {"username":"<name>","password":"<password>"} 
 	// which sets the token header upon authentication
    .addFilterBefore(new StatelessLoginFilter("/login",
			tokenAuthenticationService,
			userDetailsService,
			tokenAuthenticationManager),UsernamePasswordAuthenticationFilter.class)

 	// custom Token based authentication based on 
 	// the header previously given to the client
 	.addFilterBefore(new StatelessAuthenticationFilter(),UsernamePasswordAuthenticationFilter.class);
    */
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
