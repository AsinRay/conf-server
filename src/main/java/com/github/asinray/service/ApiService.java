package com.github.asinray.service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ApiService for admin controller.
 */

@Service
public class ApiService {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);
    private ConcurrentHashMap<String, String> repoTokenMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> matcherRoleMap = new ConcurrentHashMap<>();

    private static Pattern TOKEN_PATTERN = Pattern.compile("^2a?[.0-9A-Za-z]{53}@$");

    private static final int seed = 2;
    private static final int round = seed << seed;

    @Resource
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    /**
     * Check if the token is exists.
     * 
     * @return
     */
    public boolean tokenExists(String token) {
        return StringUtils.hasLength(parseToken(token).get("auth0"));
    }

    /**
     * Add new repo token to conf server.
     * 
     * Step: 1. Add repo -> token mapping. 2. Add ant matcher -> role mapping. 3.
     * Add user to InMemoryUserDetailsManager.
     * 
     */

    public void addRepoToken(String repo, String token) {

    }

    /**
     * Remove specified repo -> token mapping.
     * 
     * 
     * @param repo
     */
    public void removeRepoToken(String repo) {

    }

    /**
     * Generate a secure token.
     * @return
     */
    protected String genSecToken() {
        String rtn = null;
        try {
            String time = String.valueOf(System.currentTimeMillis());
            SecureRandom sr = new SecureRandom(time.getBytes("UTF-16"));
            String uid = UUID.randomUUID().toString().replaceAll("-","");
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(round,sr);
            rtn = new StringBuilder(uid).reverse()
                .append(":")
                .append(encoder.encode(time))
                .append("@").toString();
            rtn = rtn.replaceAll("/", "").replaceAll("\\$", "");
       } catch (Exception e) {
           //TODO: handle exception
           log.error("Failed to gen security token", e);
       }
       return rtn;
    }

    /**
     * Parse token.
     * @param token token to be parse, must be formatted {@link ApiService#TOKEN_PATTERN.pattern()}
     * @return
     */
    private Map<String,String> parseToken(String token){
        Map<String,String> mp = new HashMap<>();
        if(token != null && token.trim().length()>0) {
            boolean match =  TOKEN_PATTERN.matcher(token).matches() ;
            if(!match){
                throw new IllegalArgumentException("token must be matcher with:" + TOKEN_PATTERN.pattern());
            }

            String[] tk = token.split(":");

            String auth0 = tk[0];
            String auth1 = tk[1].substring(0,tk[1].length()-1);

            mp.put("auth0", auth0);
            mp.put("auth1",auth1);

        }
        return mp;
    }

}