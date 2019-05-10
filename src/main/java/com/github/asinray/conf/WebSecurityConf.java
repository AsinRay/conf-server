package com.github.asinray.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Resource;

import com.github.asinray.sec.CachedAuthenticationProvider;
import com.github.asinray.sec.GitRepoUserAccessDecisionManager;
import com.github.asinray.sec.GitRepoUserFilterInvocationSecurityMetadataSource;

import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

/**
 * Web security conf
 * 
 * @author Asin Liu
 * @since 1.0.0
 */
@EnableWebSecurity
public class WebSecurityConf {

    private static Collection<UserDetails> udl = new ArrayList<UserDetails>(){
        private static final long serialVersionUID = -7688352300074218708L;
        {
        User.UserBuilder ub = User.builder();
        ub.username("root").password("toor").roles("ROOT");
        UserDetails ud = (UserDetails) ub.build();
        add(ud);
    }};

    @Bean
    PasswordEncoder passwordEncoder(){
        return new PasswordEncoder(){

            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }

        };
    }

    @Bean 
    public CachedAuthenticationProvider cachedAuthenticationProvider(){
        return new CachedAuthenticationProvider();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(new InMemoryUserDetailsManager());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        ProviderManager authenticationManager = new ProviderManager(Arrays.asList(cachedAuthenticationProvider(),daoAuthenticationProvider()));
        authenticationManager.setEraseCredentialsAfterAuthentication(false);
        return authenticationManager;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new InMemoryUserDetailsManager(udl);
    }

    @Resource
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }


    @Configuration
    @Order(1)
    public static class AdminWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws Exception {
            http.formLogin().disable();
            http.csrf().disable();
            http.antMatcher("/**").authorizeRequests()
                    .anyRequest().authenticated()
                    .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>(){
                        /**
                         * Initialize the object possibly returning a modified instance that should be used
                         * instead.
                         *
                         * @param fsi the object to initialize
                         * @return the initialized version of the object
                         */
                        @Override
                        public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
                            fsi.setSecurityMetadataSource(new GitRepoUserFilterInvocationSecurityMetadataSource());
                            fsi.setAccessDecisionManager(new GitRepoUserAccessDecisionManager());
                            return fsi;
                        }
                    })
                    .and().httpBasic()
            ;
        }
    }

    @Configuration
    @Order(2)
    public static class ExtWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/error","/favicon.ico");
        }
    }

}