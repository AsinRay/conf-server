package com.github.asinray.sec;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

/**
 * 
 * @author Asin Liu
 */
public class GitRepoUserAccessDecisionManager implements AccessDecisionManager {



    /**
     * Resloves an access control decision for the passed parameters.
     * 
     * In {@link AbstractSecurityInterceptor} line 215, it fail-fast when
     * configAttributes is nul or is empty.
     * 
     * @param authentication    the caller invoking the method (not null)
     * @param object            the secured object being called.
     * @param configAttributes  the configuration attributes associated with the secured
     *                          object being invoked
     * @throws AccessDeniedException                if access is denied as the authentication does not
     *                                              hold a required authority or ACL privilege
     * @throws InsufficientAuthencationException    if access is denied as the 
     *                                              authentication does not provide a sufficient level of trust
     * 
     * @see {@link AbstractSecurityIntercaptor}
     * 
     *  code in /org/springframework/security/access/intercept/AbstractSecurityInterceptor.java line 215
     *  check if the cofigAttributes is null or is empty then skip the <strong>decide<strong> method.
     * 
     *      <code>
     *      if(CollectionUtils.isEmpty(configAttributes)){
     *          throw new AccessDeniedException("Not allowed");
     *      }
     *      </code>
     */


      /**
     * Resolves an access control decision for the passed parameters.
     *
     * In {@link AbstractSecurityInterceptor} line 215, it fail-fast when
     * configAttributes is null or is empty.
     *
     * @param authentication   the caller invoking the method (not null)
     * @param object           the secured object being called
     * @param configAttributes the configuration attributes associated with the secured
     *                         object being invoked
     * @throws AccessDeniedException               if access is denied as the authentication does not
     *                                             hold a required authority or ACL privilege
     * @throws InsufficientAuthenticationException if access is denied as the
     *                                             authentication does not provide a sufficient level of trust
     *
     * @see {@link AbstractSecurityInterceptor}
     *
     *  code in /org/springframework/security/access/intercept/AbstractSecurityInterceptor.java line 215
     *  check if the configAttributes is null or is empty then skip the <strong>decide<strong> method.
     *  if (CollectionUtils.isEmpty(configAttributes)) {
     *      throw new AccessDeniedException("Not allowed");
     *  }
     *
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        final boolean matched = configAttributes
                                .stream()
                                .anyMatch(o->hasRole(authentication,o.getAttribute()));
        // 该url配置的有权限，但登录用户没有match到对应权限，则禁止访问                        
        if(!matched){
            throw new AccessDeniedException("Not allowed");
        }

    }

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
     * authorization requests presented with the passed <code>ConfigAttribute</code>
     * <p>
     * This allows the <code>AbstractSecurityInterceptor</code> to check every
     * configuration attribute can be consumed by the configured
     * <code>AccessDecisionManager</code> and/or <code>RunAsManager</code> and/or
     * <code>AfterInvocationManager</code>
     * </p>
     * 
     * @param attribute a configuration attribute that has been configured against the
     *                  <code>AbstractSecurityInterceptor</code>
     * @return true if this <code>AccessDecisionManager</code> can support the passed
     * configuration attribute.
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