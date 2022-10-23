package antifraud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {
    @Autowired
    UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        RestAuthenticationEntryPoint restAuthenticationEntryPoint = new RestAuthenticationEntryPoint();

        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .mvcMatchers("/api/auth/user/*", "/api/auth/access", "/api/auth/role").hasAuthority("ROLE_ADMINISTRATOR")
                .mvcMatchers("/api/auth/list").hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_SUPPORT")
                .mvcMatchers("/api/antifraud/transaction").hasAuthority("ROLE_MERCHANT")
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip/**").hasAuthority("ROLE_SUPPORT")
                .mvcMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip/**").hasAuthority("ROLE_SUPPORT")
                .mvcMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/**").hasAuthority("ROLE_SUPPORT")
                .mvcMatchers(HttpMethod.POST, "/api/antifraud/stolencard/**").hasAuthority("ROLE_SUPPORT")
                .mvcMatchers(HttpMethod.GET, "/api/antifraud/stolencard/**").hasAuthority("ROLE_SUPPORT")
                .mvcMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/**").hasAuthority("ROLE_SUPPORT")
                .antMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasAuthority("ROLE_SUPPORT")
                .mvcMatchers(HttpMethod.PUT, "/api/antifraud/transaction/**").hasAuthority("ROLE_SUPPORT")
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }
}


