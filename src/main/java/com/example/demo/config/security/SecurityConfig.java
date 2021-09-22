package com.example.demo.config.security;

import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final OidcAuthenticationSuccessHandler oidcAuthenticationSuccessHandler;

    @Override protected void configure(HttpSecurity http) throws Exception {
        http
            .oauth2Login()
                .authorizationEndpoint().baseUri("/api/oauth2/authorization")
                .and()
            .successHandler(oidcAuthenticationSuccessHandler)
            .and()
            .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint())
            .and()
            .authorizeRequests()
                .antMatchers("/", "/api/auth-config", "/api/oauth2/**").permitAll()
                .antMatchers("/api/**").authenticated()
                .and()
            .logout()
                .logoutUrl("/api/logout")
                .deleteCookies("SESSION");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder, AccountRepository accRepo) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(accRepo);
        authProvider.setPasswordEncoder(encoder);

        return authProvider;
    }
}
