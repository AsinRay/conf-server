package com.github.asinray.controller;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.github.asinray.sec.GitRepoUserFilterInvocationSecurityMetadataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API of the config server.
 * 
 * @author Asin Ray
 * @since 1.0.0
 */
@RestController
@RequestMapping("admin")
public class ConfAdmin {

    private static final Logger log = LoggerFactory.getLogger(ConfAdmin.class);
    private static final int seed = 2;
    private static final int round = seed << seed;

    //private static Pattern TOKEN_PATTERN = Pattern.compile("^2a?[.0-9A-Za-z]{53}@$");
    private static Pattern TOKEN_PATTERN = Pattern.compile("[0-9a-z]{32}:[$0-9A-Za-z.]{20,}@$");
    
    // Cached mappings (repo -> token)
    private static final ConcurrentHashMap<String, String> cachedRepoToken = new ConcurrentHashMap<>();

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public ConfAdmin(InMemoryUserDetailsManager manager) {
        this.inMemoryUserDetailsManager = manager;
    }

    /**
     * Check if the user name is used by others.
     * 
     * @param username username which to be checked.
     * @return
     */
    @RequestMapping("user/exists/{username}")
    public boolean userExists(@PathVariable("username") String username) {
        return inMemoryUserDetailsManager.userExists(username);
    }

    @RequestMapping("user/{username}/{oldpass}/{newpass}")
    public boolean updateUserPassword(@PathVariable("username") String username,
            @PathVariable("oldpass") String oldpass, @PathVariable("newpass") String newpass) {
        return changePassword(username, oldpass, newpass);
    }

    @RequestMapping("pass/{oldpass}/{newpass}")
    public boolean updateRootPassword(@PathVariable("oldpass") String oldpass,
            @PathVariable("newpass") String newpass) {
        return changePassword("root", oldpass, newpass);
    }

    @RequestMapping("add/{repo}/{token}")
    public String addRopeToken(@PathVariable("repo") String repo, @PathVariable("token") String token){
        String msg = "ok";

        // if(!TOKEN_PATTERN.matcher(token).matches()){
        //     msg = "Token format is wrong. is it a boot-conf token?";
        //     log.warn(msg);
        //     return msg;
        // }

        String[] tk = token.split(":");
        if(tk.length != seed){
            msg = "Failed to bind token, token has wrong format.";
            log.error(msg);
            return msg;
        }

        try{
            String p = tk[1].substring(0,tk[1].length()-1);
            addNewRepoToken(repo, tk[0], p);
            setCacheRepoToken(repo, token);
        }catch(Exception e){
            msg = "Add repo token failed: ".concat(e.getMessage());
        }
        return msg;
    }


    /**
     * Generate a new token.
     */
    @RequestMapping("token")
    public String genNewToken(){
        try{
            return genSecToken();
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return "failed: ".concat(e.getMessage());
        }
    }


    /**
     * Get the token of specified repostory.
     * 
     * @param repo  repostory
     */
    @RequestMapping("{repo}/token")
    public String getRepoToken(@PathVariable("repo") String repo){
        return cachedRepoToken.get(repo);
    }





    /**
     * Add a new repo with given user and password.
     * @param repo      repo to be added.
     * @param user      repo's user
     * @param password  repo's password
     */
    private void addNewRepoToken(String repo,String user, String password){
        if(!hasText(repo) || !hasText(user) || !hasText(password)){
            throw new IllegalArgumentException("Param repo, user and password must not be null");
        }
        String role = "ROLE_".concat(user);
        String url = "/".concat(repo).concat("/**");
        SimpleGrantedAuthority sga = new SimpleGrantedAuthority(role);
        Collection<? extends GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(){{add(sga);}};
        inMemoryUserDetailsManager.createUser(new User(user,password,authorities));
        GitRepoUserFilterInvocationSecurityMetadataSource.addMatcher(url, role);
    }

    /**
     * Cache the repo's token, for query only.
     * this method should call after the method {@link #addNewRepoToken(String repo, String user, String password)}
     * @param repo  repository.
     * @param token token of the repository.
     */
    private void setCacheRepoToken(String repo,String token){
        String rst = cachedRepoToken.putIfAbsent(repo, token);
        if(rst != null){
            log.warn("The token is already exist in the cache map.");
        }
    }


    /**
     * Generate a security token.
     * this token must be once and only once.
     * the token separate to two parts, first part must be unique then followed by : and then
     * followed by second part.
     * @return
     * @throws UnsupportedEncodingException
     */
    private String genSecToken() {
        String uuid = UUID.randomUUID().toString();
        try{
            SecureRandom sr = new SecureRandom(uuid.getBytes("UTF-16"));
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(round, sr);
            String uid = uuid.replaceAll("-", "");
            String token = new StringBuilder(uid).reverse()
                                .append(":")
                                .append(encoder.encode(uid))
                                .append("@").toString();

            return token.replaceAll("/", "").replaceAll("\\$", "");
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }
        return null;
    }




    /**
     * Change the password of the specified user.
     * @param username      user's name
     * @param oldPasswd     old password
     * @param newPasswd     new password
     * @return              true if the password changed or false otherwise.
     */
    private boolean changePassword(String username,String oldPasswd,String newPasswd){
        if(StringUtils.hasText(username) && StringUtils.hasText(oldPasswd) && StringUtils.hasText(newPasswd)){
            try{
                UserDetails ud = inMemoryUserDetailsManager.loadUserByUsername(username);
                if(ud.getPassword().equals(oldPasswd)){
                    inMemoryUserDetailsManager.updatePassword(ud, newPasswd);
                    return true;
                }
            }catch(Exception e){
                log.error(e.getMessage(),e);
            }
        }else{
            log.error("parameter error. parameters must has text");
        }
        return false;
    }

    
  
    /**
     * Check if the string has text.
     */
    private boolean hasText(String text){
        return StringUtils.hasText(text);
    }
}