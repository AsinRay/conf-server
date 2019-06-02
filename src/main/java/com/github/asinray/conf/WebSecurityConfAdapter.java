package com.github.asinray.conf;

import com.github.asinray.sec.GitRepoUserAccessDecisionManager;
import com.github.asinray.sec.GitRepoUserFilterInvocationSecurityMetadataSource;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;


/**
 * Web security configuration for ot.
 * <p>
 * Multiple HttpSecurity instances config:
 * <code>
 *
 * @Order(1) public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
 * protected void configure(HttpSecurity http) throws Exception {
 * http.antMatcher("/open/**")
 * .authorizeRequests()
 * .antMatchers().permitAll();
 * }
 * }
 * </code>
 * <p>
 * For more details of spring security, please visit :
 * https://docs.spring.io/spring-security/site/docs/4.2.x/reference/htmlsingle/#multiple-httpsecurity
 * <p>
 * Attention :
 *
 * <code>
 * @Resource FailedAuthenticationEntryPoint failedAuthenticationEntryPoint;
 * </code>
 * and set
 * <code>
 * http.exceptionHandling().authenticationEntryPoint(failedAuthenticationEntryPoint).and()
 * </code>
 * <p>
 * Conflict with
 * <code>
 * loginFilter.setAuthenticationFailureHandler(new ResponseAuthenticationFailureHandler());
 * </code>
 * configuration, so you must choose one of them for failure handler.
 *
 *
 * 1. Test if standalone context path with the same port is available.
 * 2. send a help info for multiple port config
 *
 * @author Asin Liu
 * @version 1.0.0
 */

@EnableWebSecurity
@Order(1)
public class WebSecurityConfAdapter extends WebSecurityConfigurerAdapter {
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