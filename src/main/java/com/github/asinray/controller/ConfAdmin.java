package com.github.asinray.controller;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.github.asinray.sec.GitRepoUserFilterInvocationSecurityMetadataSource;

import com.github.asinray.service.ApiService;
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

    private final ApiService apiService;
    @Autowired
    public ConfAdmin(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Change the root password
     * 
     * @param oldpass   old password of root
     * @param newpass   new password to be reset to root.
     */

    @RequestMapping("pass/{oldpass}/{newpass}")
    public boolean updateRootPassword(@PathVariable("oldpass") String oldpass, @PathVariable("newpass") String newpass) {
        return apiService.updatePassword("root", oldpass, newpass);
    }

    /**
    * Generate a new token.
    */
    @RequestMapping("token")
    public String genNewToken(){
        return apiService.genSecToken();
    }

    /**
     * Assign token to specified repo.
     * @param repo
     * @param token
     * @return
     */
    @RequestMapping("add/{repo}/{token}")
    public boolean assignToken2Repo(@PathVariable("repo") String repo, @PathVariable("token") String token){
        return apiService.addRepoToken(repo, token);
    }

    /**
     * Get the token of specified repostory.
     * 
     * @param repo  repostory
     */
    @RequestMapping("{repo}/token")
    public String getRepoToken(@PathVariable("repo") String repo){
        return apiService.getRepoToken(repo);
    }

     /**
     * Delete the repo and token.
     * @param repo  repository
     */
    @RequestMapping("{repo}/del")
    public boolean removeRepoToken(@PathVariable("repo") String repo){
        return apiService.removeRepoToken(repo);
    }


    @RequestMapping("{repo}/exist")
    public boolean repoExist(@PathVariable("repo") String repo){
        return apiService.repoExists(repo);
    }

}