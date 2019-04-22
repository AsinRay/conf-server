package com.github.asinray.sec;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;

/**
 * Cached authentication provider for conf server authentication.
 * 
 * @author Asin Liu
 * @param <GrantedAuthority>
 */
public class CachedAuthenticationProvider implements AuthenticationProvider {

    private static final String INIT_ADMIN_NAME = "root";
    private static final String INIT_ADMIN_PSWD = "root";

    /** 
     * Default roles.
     */
    private final Collection<GrantedAuthority> authorities =  Arrays.asList(
            new SimpleGrantedAuthority("CAN_SEARCH"), 
            new SimpleGrantedAuthority("CAN_EXPORT"),
            new SimpleGrantedAuthority("CAN_IMPORT"), 
            new SimpleGrantedAuthority("CAN_EMPOWER"),
            new SimpleGrantedAuthority("CAN_DISCARD"));

     /**
     * Performs authentication with the same contract as
     * {@link AuthenticationManager#authenticate(Authentication)}
     * .
     *
     * @param authentication the authentication request object.
     * @return a fully authenticated object including credentials. May return
     * <code>null</code> if the <code>AuthenticationProvider</code> is unable to support
     * authentication of the passed <code>Authentication</code> object. In such a case,
     * the next <code>AuthenticationProvider</code> that supports the presented
     * <code>Authentication</code> class will be tried.
     * @throws AuthenticationException if authentication fails.
     */
    @Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // if(isMatch(authentication)){
        //     User user  = new User(authentication.getName(),authentication.getPrincipal().toString(),authorities.stream().collect());
        //     return new UsernamePasswordAuthenticationToken(user,authentication.getCredentials(),authorities);
        // }
        if(isMatch(authentication)){
            User user = new User(authentication.getName(),authentication.getCredentials().toString(),authorities);
            return new UsernamePasswordAuthenticationToken(user,authentication.getCredentials(),authorities);
        }
		return null;
    }
    
    /**
     * Check if the authentication should be authenticated.
     */
    private boolean isMatch(Authentication authentication){
        return (authentication.getName().equals(INIT_ADMIN_NAME) && authentication.getCredentials().equals(INIT_ADMIN_NAME));
    }

     /**
     * Returns <code>true</code> if this <Code>AuthenticationProvider</code> supports the
     * indicated <Code>Authentication</code> object.
     * <p>
     * Returning <code>true</code> does not guarantee an
     * <code>AuthenticationProvider</code> will be able to authenticate the presented
     * instance of the <code>Authentication</code> class. It simply indicates it can
     * support closer evaluation of it. An <code>AuthenticationProvider</code> can still
     * return <code>null</code> from the {@link #authenticate(Authentication)} method to
     * indicate another <code>AuthenticationProvider</code> should be tried.
     * </p>
     * <p>
     * Selection of an <code>AuthenticationProvider</code> capable of performing
     * authentication is conducted at runtime the <code>ProviderManager</code>.
     * </p>
     *
     * @param authentication
     * @return <code>true</code> if the implementation can more closely evaluate the
     * <code>Authentication</code> class presented
     */
	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}