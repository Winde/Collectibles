package configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import configuration.security.jwt.JwtAuthenticationProvider;
import configuration.security.jwt.StatelessAuthenticationFilter;
import configuration.security.jwt.StatelessLoginFilter;
import configuration.security.jwt.TokenAuthenticationService;
import configuration.security.jwt.UserDetailsServiceImpl;

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
	
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;
	
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Autowired
	private AuthenticationManagerBuilder authenticationManagerBuilder;
	
	@Autowired
	private JwtAuthenticationProvider jwtAuthenticationProvider;
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    
    	
	
    /*
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
	*/
    http    
    	.csrf().disable()  
    	.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
    .and()
    	.authorizeRequests()
    	.antMatchers("/app/**").permitAll()		
	.and()	     
     	.headers()     	
     	//.antMatchers("/image/content/**")     		
     			.cacheControl()
     				.disable()
    .and()
    
	// custom JSON based authentication by POST of 
	// {"username":"<name>","password":"<password>"} 
	// which sets the token header upon authentication
	.addFilterBefore(new StatelessLoginFilter("/login",tokenAuthenticationService,
            userDetailsService,authenticationManagerBuilder.getOrBuild()), UsernamePasswordAuthenticationFilter.class)
 
	// custom Token based authentication based on 
	// the header previously given to the client
	.addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class);	
    	
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
       
    	BCryptPasswordEncoder passWordEncoder = new BCryptPasswordEncoder();
    	
    	auth
    	.authenticationProvider(jwtAuthenticationProvider)
    	.userDetailsService(userDetailsService)
    	.passwordEncoder(passWordEncoder);
    	/*.and()
            .inMemoryAuthentication()
                .withUser("username").password("password").roles("USER","ADMIN");*/
       
    	/*
    	auth
        	.userDetailsService(userDetailsService)
        	.passwordEncoder(new ShaPasswordEncoder(256));
       */
        
    }

	

    
}
