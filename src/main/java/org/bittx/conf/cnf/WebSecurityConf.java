package org.bittx.conf.cnf;


import org.bittx.conf.sec.CachedAuthenticationProvider;
import org.bittx.conf.service.MemPersistenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Web security conf
 *
 * @author Asin Liu
 * @since 1.0.0
 */
@Configuration
public class WebSecurityConf {

    /**
     * Configuration init root user with role ROLE_ROOT.
     * spring security will add ROLE_ prefix automatically.
     */
    private static CopyOnWriteArrayList<UserDetails> udl = new CopyOnWriteArrayList<UserDetails>(){{
        User.UserBuilder ub = User.builder();
        add( ub.username("root").password("toor").roles("ROOT").build());
    }};


    /**
     * Other password encoder ?
     * @return
     */
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
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        List<User> ul = MemPersistenceService.loadUsers(MemPersistenceService.USER_STORE_FILE);
        if (ul == null || ul.isEmpty()) {
            return new InMemoryUserDetailsManager(udl);
        }
        udl.clear();
        ul.stream().forEach(o-> udl.add(o));
        return new InMemoryUserDetailsManager(udl);
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return inMemoryUserDetailsManager();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        return daoAuthenticationProvider;
    }

    @Bean
    public CachedAuthenticationProvider cachedAuthenticationProvider(){
        return new CachedAuthenticationProvider();
    }

    /**
     * 不擦除认证密码，
     * 擦除会导致TokenBasedRememberMeServices因为找不到Credentials再调用UserDetailsService
     * 而抛出UsernameNotFoundException
     *
     * @return
     */
    @Bean
    public AuthenticationManager authenticationManager(){
        ProviderManager authenticationManager = new ProviderManager(
                Arrays.asList(cachedAuthenticationProvider(),daoAuthenticationProvider()));
        authenticationManager.setEraseCredentialsAfterAuthentication(false);
        return authenticationManager;
    }

    @Resource
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }
}
