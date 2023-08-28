package com.logicea.cards.jwt;

import com.logicea.cards.auditor.SpringSecurityAuditorAware;
import com.logicea.cards.error.CustomAccessDeniedHandler;
import com.logicea.cards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableJpaAuditing
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private  JwtConfig jwtConfig;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Disable CSRF for simplicity, configure properly for production
                .authorizeRequests()
                .antMatchers("/api/auth/login").permitAll() // Allow login endpoint
                .antMatchers("/api/cards/all-cards").hasAnyAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(authenticationManager(), jwtConfig), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtAuthorizationFilter(authenticationManager(),jwtConfig,userRepository))
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler()); // Custom access denied handler
    }
}


