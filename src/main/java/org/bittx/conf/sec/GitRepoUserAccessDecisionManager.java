package org.bittx.conf.sec;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * Access decision manager.
 *
 * 操作FilterSecurityInterceptor这个interceptor，使用withObjectPostProcessor来设置
 *
 * FilterSecurityInterceptor
 * 这个filter有几个要素，如下：
 *
 *    SecurityMetadataSource
 *    AccessDecisionManager
 *    AuthenticationManager
 *
 * 可以根据情况重新设置，这里我们重写一下SecurityMetadataSource用来动态获取url权限配置，
 * 还有AccessDecisionManager来进行权限判断。
 *
 * @author Asin Liu
 * @since 1.0.0
 * @version 2.0.0
 */

public class GitRepoUserAccessDecisionManager implements AccessDecisionManager {
    /**
     * Resolves an access control decision for the passed parameters.
     *
     * @param authentication   the caller invoking the method (not null)
     * @param object           the secured object being called
     * @param configAttributes the configuration attributes associated with the secured
     *                         object being invoked
     * @throws AccessDeniedException               if access is denied as the authentication does not
     *                                             hold a required authority or ACL privilege
     * @throws InsufficientAuthenticationException if access is denied as the
     *                                             authentication does not provide a sufficient level of trust
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {

        final boolean matched = configAttributes.stream()
                .anyMatch(o->hasRole(authentication,o.getAttribute()));
        // 该url配置的有权限，但登录用户没有match到对应权限，则禁止访问
        if(!matched){
            throw new AccessDeniedException("Not allowed");
        }
    }

    /**
     * Check if the authentication has specified role or not.
     * @param authentication    authentication to be checked.
     * @param role              specified role to be check to.
     * @return                  True if the authentication has role, false otherwise.
     */
    private boolean hasRole(Authentication authentication , String role){
        if(authentication == null || authentication.getAuthorities()==null){
            return false;
        }
        return authentication
                .getAuthorities()
                .parallelStream()
                .anyMatch(o->o.getAuthority().equals(role));
    }

    /**
     * Indicates whether this <code>AccessDecisionManager</code> is able to process
     * authorization requests presented with the passed <code>ConfigAttribute</code>.
     * <p>
     * This allows the <code>AbstractSecurityInterceptor</code> to check every
     * configuration attribute can be consumed by the configured
     * <code>AccessDecisionManager</code> and/or <code>RunAsManager</code> and/or
     * <code>AfterInvocationManager</code>.
     * </p>
     *
     * @param attribute a configuration attribute that has been configured against the
     *                  <code>AbstractSecurityInterceptor</code>
     * @return true if this <code>AccessDecisionManager</code> can support the passed
     * configuration attribute
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /**
     * Indicates whether the <code>AccessDecisionManager</code> implementation is able to
     * provide access control decisions for the indicated secured object type.
     *
     * @param clazz the class that is being queried
     * @return <code>true</code> if the implementation can process the indicated class
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
