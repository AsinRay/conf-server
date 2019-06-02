package com.github.asinray.service;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.github.asinray.sec.GitRepoUserFilterInvocationSecurityMetadataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
     * Reset the password of specified user.
     * @param userName
     * @param oldPassword
     * @param newPassword
     */
    public boolean updatePassword(String userName, String oldPassword, String newPassword) {
        if(!StringUtils.hasText(userName) || ! StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)){
            log.error("userName, oldPassword and newPassword must not be null");
            return false;
        }

        try {
            UserDetails ud = inMemoryUserDetailsManager.loadUserByUsername(userName);
            if(ud != null && ud.getPassword().equals(oldPassword)){
                inMemoryUserDetailsManager.updatePassword(ud, newPassword);
                MemPersistenceService.updateUsers(extractPersistenceUsers(inMemoryUserDetailsManager), USER_STORE_FILE);
                return true;
            }
        } catch (Exception e) {
            //TODO: handle exception
            log.error(e.getMessage(),e);
        }
        return false;
    }
     

    /**
     * Generate a security token.
     * this token must be once and only once.
     * the token separate to two parts, first part must be unique then followed by : and then
     * followed by second part.
     * @return
     * @throws UnsupportedEncodingException
     */
    public String genSecToken(){
        try {
            String time = String.valueOf(System.currentTimeMillis());
            SecureRandom sr = new SecureRandom(time.getBytes("UTF-16"));
            String uid = UUID.randomUUID().toString().replaceAll("-", "");
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(round, sr);
            String str = new StringBuilder(uid)
                    .reverse().append(":")
                    .append(encoder.encode(time))
                    .append("@").toString();
            return str.replaceAll("/", "")
                    .replaceAll("\\$","");
        } catch (Exception e) {
            log.error("Failed to gen security token",e);
            return "failed: ".concat(e.getMessage());
        }
    }

    /**
     * Extract user from InMemoryUserDetailsManager
     * 
     * @param inMemoryUserDetailsManager
     * @return
     */
    public static List<User> extractPersistenceUsers(InMemoryUserDetailsManager inMemoryUserDetailsManager){
        List<User> userList = null;

        try{
            Field field = InMemoryUserDetailsManager.class.getDeclaredField("users");
            field.setAccessible(true);
            HashMap map = (HashMap) field.get(inMemoryUserDetailsManager);
            map.values().stream().forEach(o->{
                UserDetails ud = (UserDetails)o;
                User u = new User(ud.getUsername(), ud.getPassword(), ud.getAuthorities());
                userList.add(u);
            });
            /*
            userList = map.values.stream().map(o->{
                UserDetails ud = (UserDetails)o;
                User u = new User(ud.getUsername(),ud.getPassword(),ud.getAuthorities());
                return u;
            }).collect(Collectors.toList());
            */
        }catch(NoSuchFieldException noSuchFieldError) {
            // TODO: 
            log.error("No such field error:",noSuchFieldError);
        }catch(IllegalAccessException iae){
            log.error("IllegalArgumentException:", iae);
        }
        return userList;
    }

    /**
     * Check if the token is exists.
     * 
     * @return
     */
    public boolean repoExists(String repo) {
        String token = repoTokenMap.get(repo);
        return TOKEN_PATTERN.matcher(token).matches() ;
    }

    /**
     * Add new repo token to conf server.
     * 
     * Step: 
     * 1. Add repo -> token mapping. 
     * 2. Add ant matcher -> role mapping. 
     * 3. Add user to InMemoryUserDetailsManager.
     * 
     *
     * @param repo      repo to be added.
     * @param token     repo's token
     *
     */
    public boolean addRepoToken(String repo,String token){

        String msg = "ok";
        /* if (!token.equals(newestToken)) {
            msg = "Token is expired, please renew a new one.";
            return msg;
        }*/

       /* if (!TOKEN_PATTERN.matcher(token).matches()) {
            msg = "Token does not look like a boot2-conf token";
            logger.warn(msg);
            return msg;
        }*/

        String[] tk = token.split(":");
        if (tk.length != seed){
            msg = "Failed to bind token, token has wrong format.";
            log.error(msg);
            return false;
        }
        String p = tk[1].substring(0, tk[1].length() - 1);
        //TODO: if the keys contains repo, return;
        repoTokenMap.put(repo, token);
        addUserRepo(repo,tk[0],p);
        return true;
    }

    /**
     * Add a new repo with given user and password.
     * @param repo      repo to be added.
     * @param user      repo's username     if the username has added to memory, an exception will throw,
     *                  see  {org.springframework.security.provisioning#createUser()}
     * @param password  repo's password
     *
     */
    private void addUserRepo(String repo,String user,String password) {
        if (!StringUtils.hasText(repo) || !StringUtils.hasText(user) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("repo, username and password must not be null.");
        }
        String r = "ROLE_".concat(user);
        String url = "/".concat(repo).concat("/**");
        SimpleGrantedAuthority sga = new SimpleGrantedAuthority(r);
        Collection<? extends GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(){{add(sga);}};
        inMemoryUserDetailsManager.createUser(new User(user, password,authorities));
        MemPersistenceService.updateUsers(extractPersistenceUsers(inMemoryUserDetailsManager),USER_STORE_FILE);
        GitRepoUserFilterInvocationSecurityMetadataSource.addMatcher(url,r);
    }


    /**
     * Get token of the specified repo.
     */
    public String getRepoToken(String repo){
        return repoTokenMap.get(repo);
    }

    /**
     * Remove specified repo -> token mapping.
     * 
     * 
     * @param repo
     */
    public boolean removeRepoToken(String repo) {
        try {
            repoTokenMap.remove(repo);
        } catch (Exception e) {
            //TODO: handle exception
            log.error("Remove token failed. msg: {}", e.getMessage(), e);
            return false;
        }
        return true;
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