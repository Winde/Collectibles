package configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import configuration.security.jwt.JwtAuthenticationOnSuccess;
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
	
	@Autowired
    private PasswordEncoder passwordEncoder;
    
	@Bean 
	public PasswordEncoder bCryptPasswordEncoder(){
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	public FilterRegistrationBean insertRegistrationAfter() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		JwtAuthenticationOnSuccess userFilter = new JwtAuthenticationOnSuccess(tokenAuthenticationService);
		registrationBean.setFilter(userFilter);
		registrationBean.setOrder(Integer.MAX_VALUE);
		return registrationBean;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	
		http    
			.csrf().disable()  
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
		.and()
			.authorizeRequests()
				.antMatchers("/app/**").permitAll()		
		.and()	     
			.headers()     	 		
				.cacheControl()
				.disable()
		.and()
	    
			// custom JSON based authentication by POST of 
			// {"username":"<name>","password":"<password>"} 
			// which sets the token header upon authentication
			.addFilterBefore(new StatelessLoginFilter("/login",tokenAuthenticationService,userDetailsService,authenticationManagerBuilder.getOrBuild()), UsernamePasswordAuthenticationFilter.class)

			// custom Token based authentication based on 
			// the header previously given to the client
			.addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), UsernamePasswordAuthenticationFilter.class);	
	    
	}
       
    
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		auth
		.authenticationProvider(jwtAuthenticationProvider)
			.userDetailsService(userDetailsService)
				.passwordEncoder(passwordEncoder);

        
    }

	

    
}
