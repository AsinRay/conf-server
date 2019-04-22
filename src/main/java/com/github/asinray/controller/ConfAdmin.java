package com.github.asinray.controller;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.SecurityExpressionHandler;
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
@RequestMapping("amdin")
public class ConfAdmin {

    private static final Logger log = LoggerFactory.getLogger(ConfAdmin.class);
    private static final int seed = 2;
    private static final int round = seed << seed;

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
    @RequestMapping("user/exist/{username}")
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

    private String genSecToken() {
        String uuid = UUID.randomUUID().toString();
        try{
            SecureRandom sr = new SecureRandom(uuid.getBytes("UTF-16"));
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(round, sr);
            String uid = uuid.replaceAll("-", "");
            String token = new StringBuilder(uid).reverse().append(":")
        }catch(Exception e){

        }
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
            return false;
        }else{
            log.error("parameter error. parameters must has text");
        }
        return false;
    }
}