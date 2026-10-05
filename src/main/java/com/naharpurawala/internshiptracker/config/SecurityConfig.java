package com.naharpurawala.internshiptracker.config;

import com.naharpurawala.internshiptracker.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder); return provider;
    }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http,DaoAuthenticationProvider authenticationProvider)throws Exception{
        http.authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth->auth.requestMatchers("/signup","/login","/access-denied","/css/**","/js/**","/images/**","/error","/actuator/health","/actuator/health/**").permitAll().anyRequest().authenticated())
            .exceptionHandling(exceptions->exceptions.accessDeniedPage("/access-denied"))
            .formLogin(form->form.loginPage("/login").usernameParameter("email").defaultSuccessUrl("/",true).failureUrl("/login?error").permitAll())
            .logout(logout->logout.logoutSuccessUrl("/login?logout").invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));
        return http.build();
    }
}
