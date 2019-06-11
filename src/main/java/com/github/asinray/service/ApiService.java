package com.github.asinray.service;

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

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * ApiService for admin controller.
 */

@Service
public class ApiService {

    private static final Logger log = LoggerFactory.getLogger(ApiService.class);

    private static final String AUTH0 = "auth0";
    private static final String AUTH1 = "auth1";

    private static ConcurrentHashMap<String, String> repoTokenMap = new ConcurrentHashMap<>();

    public static Pattern TOKEN_PATTERN = Pattern.compile("^[0-9A-Za-z]{32}:2a08?[.0-9A-Za-z]{52,53}@$");

    private static final int seed = 2;
    private static final int round = seed << seed;

    @Resource
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    /**
     * Reset the password of specified user.
     *
     * @param userName
     * @param oldPassword
     * @param newPassword
     */
    public boolean updatePassword(String userName, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(userName) || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            log.error("userName, oldPassword and newPassword must not be null");
            return false;
        }

        try {
            UserDetails ud = inMemoryUserDetailsManager.loadUserByUsername(userName);
            if (ud != null && ud.getPassword().equals(oldPassword)) {
                inMemoryUserDetailsManager.updatePassword(ud, newPassword);
                MemPersistenceService.updateUsers(extractPersistenceUsers(inMemoryUserDetailsManager));
                return true;
            }
        } catch (Exception e) {
            //TODO: handle exception
            log.error(e.getMessage(), e);
        }
        return false;
    }


    /**
     * Generate a security token.
     * this token must be once and only once.
     * the token separate to two parts, first part must be unique then followed by : and then
     * followed by second part.
     *
     * @return
     */
    public String genSecToken() {
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
                    .replaceAll("\\$", "");
        } catch (Exception e) {
            log.error("Failed to gen security token", e);
            return "failed: ".concat(e.getMessage());
        }
    }

    /**
     * Extract user from InMemoryUserDetailsManager
     *
     * @param inMemoryUserDetailsManager
     * @return
     */
    public static List<User> extractPersistenceUsers(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        List<User> userList = new ArrayList<>();

        try {
            Field field = InMemoryUserDetailsManager.class.getDeclaredField("users");
            field.setAccessible(true);
            HashMap map = (HashMap) field.get(inMemoryUserDetailsManager);
            map.values().stream().forEach(o -> {
                UserDetails ud = (UserDetails) o;
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
        } catch (NoSuchFieldException noSuchFieldError) {
            // TODO:
            log.error("No such field error:", noSuchFieldError);
        } catch (IllegalAccessException iae) {
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
        return repoTokenMap.keySet().contains(repo);
    }

    private boolean tokenExists(String token){
        return repoTokenMap.values().contains(token);
    }

    /**
     * Add new repo token to conf server.
     * <p>
     * Step:
     * 1. Add repo -> token mapping.
     * 2. Add ant matcher -> role mapping.
     * 3. Add user to InMemoryUserDetailsManager.
     *
     * @param repo  repo to be added.
     * @param token repo's token
     */
    public boolean addRepoToken(String repo, String token) {

        if (!hasText(repo) || !hasText(token)) {
            log.warn("Repo and token must not be null");
            return false;
        }

        if (!TOKEN_PATTERN.matcher(token).matches()) {
            log.warn("Token does not look like a boot2-conf token");
            return false;
        }

        if (tokenExists(token)) {
            log.warn("Token has been assigned to another repo, please renew token.");
            return false;
        }


        //TODO: if the keys contains repo, return;
        addRepoTokenMapping(repo, token);     // step : 01

        Map<String, String> map = parseToken(token);
        String user = map.get(AUTH0);
        String pass = map.get(AUTH1);

        addUser(user, pass);                   // step : 02

        String url = "/".concat(repo).concat("/**");
        String r = "ROLE_".concat(user);
        GitRepoUserFilterInvocationSecurityMetadataSource.addMatcher(url, r);    // step : 3
        return true;
    }

    /**
     * Add a new repo with given user and password.
     *
     * @param user     repo's username     if the username has added to memory, an exception will throw,
     *                 see  {org.springframework.security.provisioning#createUser()}
     * @param password repo's password
     */
    private void addUser(String user, String password) {
        if (!hasText(user) || !hasText(password)) {
            throw new IllegalArgumentException("username and password must not be null.");
        }
        String r = "ROLE_".concat(user);
        SimpleGrantedAuthority sga = new SimpleGrantedAuthority(r);
        Collection<? extends GrantedAuthority> authorities = new ArrayList<GrantedAuthority>() {{
            add(sga);
        }};
        inMemoryUserDetailsManager.createUser(new User(user, password, authorities));
        MemPersistenceService.updateUsers(extractPersistenceUsers(inMemoryUserDetailsManager));
    }


    /**
     * Get token of the specified repo.
     */
    public String getRepoToken(String repo) {
        return repoTokenMap.get(repo);
    }

    private void addRepoTokenMapping(String repo, String token) {
        repoTokenMap.put(repo, token);
        MemPersistenceService.updateRepoTokenMap(repoTokenMap);
    }

    private void removeRepoTokenMapping(String repo) {
        repoTokenMap.remove(repo);
        MemPersistenceService.updateRepoTokenMap(repoTokenMap);
    }

    /**
     * Remove specified repo -> token mapping.
     *
     * @param repo
     */
    public boolean removeRepoToken(String repo) {
        try {
            String token = repoTokenMap.get(repo);
            String auth0 = parseToken(token).get(AUTH0);

            inMemoryUserDetailsManager.deleteUser(auth0);       // step : 1
            GitRepoUserFilterInvocationSecurityMetadataSource.removeMatcher(auth0); // step : 2
            removeRepoTokenMapping(repo);   // step : 3
        } catch (Exception e) {
            //TODO: handle exception
            log.error("Remove token failed. msg: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Parse token.
     *
     * @param token token to be parse, must be formatted {@link ApiService#TOKEN_PATTERN.pattern()}
     * @return
     */
    private Map<String, String> parseToken(String token) {
        Map<String, String> mp = new HashMap<>();
        if (token != null && token.trim().length() > 0) {
            boolean match = TOKEN_PATTERN.matcher(token).matches();
            if (!match) {
                throw new IllegalArgumentException("Token must be matcher with:" + TOKEN_PATTERN.pattern());
            }

            String[] tk = token.split(":");

            String auth0 = tk[0];
            String auth1 = tk[1].substring(0, tk[1].length() - 1);

            mp.put(AUTH0, auth0);
            mp.put(AUTH1, auth1);

        }
        return mp;
    }


    /**
     * Get all keys which has the value of specified object.
     *
     * @param <K>   the key
     * @param <V>
     * @param map   map to be parse
     * @param value specified value.
     * @return a list which contains all keys with the specified value.
     */
    public static <K, V> List<K> getMapKeyViaValue(Map<K, V> map, V value) {

        List<K> valueList = new ArrayList<K>();
        for (K k : map.keySet()) {
            if (value.equals(map.get(k))) {
                valueList.add(k);
            }
        }
        return valueList;
    }


    /**
     * Check if the string has text.
     */
    private boolean hasText(String text) {
        return StringUtils.hasText(text);
    }
}