package org.bittx.conf.cnf;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Ignore web security filter
 *
 * @author Asin Liu
 */

@Configuration
@Order(2)
public class IgnoreWebSecConfAdapter extends WebSecurityConfigurerAdapter {

    /**
     * Override this method to configure {@link WebSecurity}. For example, if you wish to
     * ignore certain requests.
     *
     * @param web
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // super.configure(web);
        web.ignoring().antMatchers("/error","/favicon.ico");
    }
}
