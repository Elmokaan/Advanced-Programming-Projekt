package de.hsalbsig.HonigruehrmaschineSteuerung.security;

import de.hsalbsig.HonigruehrmaschineSteuerung.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                        .mvcMatchers("/css/**","/jquery/**").permitAll()
                        .mvcMatchers( "/signup").permitAll()
                        .anyRequest().authenticated()
        .and().formLogin().loginPage("/login").permitAll().defaultSuccessUrl("/dashboard")
                .and().logout().logoutUrl("/logout").and().csrf().disable();

    }

    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return new CustomUserDetailsService();
    }

    @Bean
    PasswordEncoder passwordEncoder (){
        return new BCryptPasswordEncoder();
    }

}
