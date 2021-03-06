package com.example.secured.bootsecurity.security;

import com.example.secured.bootsecurity.db.UserRepository;
import com.example.secured.bootsecurity.service.UserPrincipalDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private UserPrincipalDetailService userPrincipalDetailService;
    private UserRepository userRepository;


    public SecurityConfiguration(UserPrincipalDetailService userPrincipalDetailService, UserRepository userRepository) {
        this.userPrincipalDetailService = userPrincipalDetailService;
        this.userRepository = userRepository;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /*
        In memory Authentication
        ** Code start here
        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder()
        .encode("admin123")).roles("ADMIN")
                .and().withUser("dan").password(passwordEncoder()
                .encode("dan123")).roles("USER")
                .and().withUser("manager").password(passwordEncoder()
                .encode("manager123")).roles("MANAGER");
 */

        auth.authenticationProvider(authenticationProvider());
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //remove csrf and state in session because it is not needed in JWT
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //add jwt filters
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(),this.userRepository))
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/api/public/admin/*").hasRole("ADMIN")
                .antMatchers("/api/public/management/*").hasRole("MANAGER");

    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userPrincipalDetailService);
        return daoAuthenticationProvider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
