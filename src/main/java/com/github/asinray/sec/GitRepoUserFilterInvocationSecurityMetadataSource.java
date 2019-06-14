package com.github.asinray.sec;

import com.github.asinray.service.MemPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.asinray.service.MemPersistenceService.ANT_MATCHER_STORE_FILE;

/**
 * Git repository filter invocation security meta data source.
 * 
 * @author Asin Liu
 * @since 1.0.0
 * @see org.springframework.security.access.SecurityMetadataSource
 */
public class GitRepoUserFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private static final Logger log = LoggerFactory.getLogger(GitRepoUserFilterInvocationSecurityMetadataSource.class);
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public static final String ROLE_ROOT = "ROLE_ROOT";
    public static final String ROLE_USER = "ROLE_USER";

    private static Map<String, String> urlRoleMap = new ConcurrentHashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("/open/**", "ROLE_ANONYMOUS");
            put("/encrypt", ROLE_ROOT);
            put("/encrypt/status", ROLE_ROOT);
            put("/decrypt", ROLE_ROOT);
            put("/admin/**", ROLE_ROOT);
        }
    };


    public GitRepoUserFilterInvocationSecurityMetadataSource(){
        Map<String, String> map = MemPersistenceService.loadAntMatchers(ANT_MATCHER_STORE_FILE);
        if(map != null){
            urlRoleMap.clear();
            urlRoleMap.putAll(map);
        }
    }

    /**
     * Add new matcher and persistent to file.
     *
     * Persistent 3 !!!
     * @param url
     * @param role
     */
    public static void addMatcher(String url, String role) {
        urlRoleMap.put(url, role);
        log.info("Add matcher : {},{}",url,role);
        MemPersistenceService.updateAntMatchers(urlRoleMap);
    }


    /**
     * Remove ant matcher and re-persistent to file.
     * @param auth0
     */
    public static void removeMatcher(String auth0) {
        String role = "ROLE_".concat(auth0);
        Set<String> keys = urlRoleMap.keySet();

        for (String s : keys) {
            if(urlRoleMap.get(s).equals(role)){
                urlRoleMap.remove(s);
            }
        }
        log.info("remove matcher : {}",auth0);
        MemPersistenceService.updateAntMatchers(urlRoleMap);
    }



    /**
     * Accesses the {@code ConfigAttribute}s that apply to a given secure object.
     * @param object the object being secured.
     * @return the attributes that apply to the passed in secured object. Should return an
     * empty collection if there are on applicable attributes.
     * @throws IllegalArgumentExecption if the passed object is not of a type supported by
     *                                  the <code>SecurityMetadataSource</code> implementation.
     * 
     * @see org.springframework.security.access.SecurityConfig
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();
        //String httpMethod = fi.getRequest().getMethod();
        for (Entry<String, String> entry : urlRoleMap.entrySet()) {
            if(antPathMatcher.match(entry.getKey(),url)){
                return SecurityConfig.createList(entry.getValue());
            }
        }
        //没有匹配到,默认是要登录才能访问
        return SecurityConfig.createList("ROLE_USER");
    }

    /**
     * If available, return all of the {@code ConfigAttribute}s defined by the
     * implementing class.
     * <p>
     * This is used by the {@link AbstractSecurityInterceptor} to perform startup time
     * validation of each {@code ConfigAttribute} configured agaiinst it.
     * @return the {@code ConfigAttribute}s or {@code null} if unsupported.
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    /**
     * Indicates whether the {@code SecurityMetadataSource} implementation is able to
     * provide {@code ConfigAttribute}s for the indicated secure object type.
     *
     * @param clazz the class that is being queried
     * @return true if the implementation can process the indicated class
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

}