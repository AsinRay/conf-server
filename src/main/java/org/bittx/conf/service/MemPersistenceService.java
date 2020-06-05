package org.bittx.conf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Persistence Service persistence the data to file .
 *
 * @version 2.0
 * @author: Asin Liu
 */
public interface MemPersistenceService extends Persistencer {
    Logger log = LoggerFactory.getLogger(MemPersistenceService.class);

    String USER_HOME = System.getProperty("user.home");
    String DEF_SEC_PATH = USER_HOME.concat("/.sec/");
    // user info store
    String USER_STORE_FILE = DEF_SEC_PATH.concat(".udl");
    // repo store
    String REPO_TOKEN_STORE_FILE = DEF_SEC_PATH.concat(".atm");
    // ant matcher store
    String ANT_MATCHER_STORE_FILE = DEF_SEC_PATH.concat(".am");

    /**
     * load user from user info store
     * @param filePath
     * @return
     */
    static List<User> loadUsers(String filePath) {
        return loadFromFile(filePath);
    }

    /**
     * Update user info on running collection, and then persist to user info store
     * @param list
     * @param filePath  user's defined user info store
     * @return
     */
    static boolean updateUsers(List<User> list, String filePath) {
        return persistenceObject2File(list, filePath);
    }

    /**
     * Update user info, and then persist to default user info store
     * @param list
     * @return
     */
    static boolean updateUsers(List<User> list) {
        return persistenceObject2File(list, USER_STORE_FILE);
    }

    /**
     * Load ant matchers from specified file.
     * @param filePath  file which store ant matchers
     * @return
     */
    static Map<String, String> loadAntMatchers(String filePath) {
        return loadFromFile(filePath);
    }


    /**
     * Update ant matchers and persist it to file
     * @param antMatchers   ant matchers to be persist.
     * @param filePath      the file which ant matchers persist to.
     * @return
     */
    static boolean updateAntMatchers(Map<String, String> antMatchers, String filePath) {
        return persistenceObject2File(antMatchers, filePath);
    }

    /**
     * Update ant matchers and persist it to default ant matcher store.
     * @param antMatchers   ant matchers to be persist.
     * @return
     */
    static boolean updateAntMatchers(Map<String, String> antMatchers) {
        return persistenceObject2File(antMatchers, ANT_MATCHER_STORE_FILE);
    }

    /**
     * Load repo store from file
     * @param filePath  file
     * @return
     */
    static Map<String, String> loadRepoTokenMap(String filePath) {
        return loadFromFile(filePath);
    }


    /**
     * Update repo store and persist it to specified repo token store.
     * @param repoTokenMap  repo token store to be persist.
     * @param filePath
     * @return
     */
    static boolean updateRepoTokenMap(Map<String,String> repoTokenMap, String filePath) {
        return persistenceObject2File(repoTokenMap, filePath);
    }

    /**
     * Update repo store and persist it to default repo token store.
     * @param repoTokenMap  repo store map to be persist.
     * @return
     */
    static boolean updateRepoTokenMap(Map<String,String> repoTokenMap) {
        return persistenceObject2File(repoTokenMap, REPO_TOKEN_STORE_FILE);
    }


    /**
     * Load data from file.
     *
     * @param filePath
     * @param <T>
     * @return Object which can be deserialized from file, or null if the error occered.
     */
    static <T> T loadFromFile(String filePath) {
        if (filePath == null || filePath.trim().length() < 1 || !Files.exists(Paths.get(filePath))) {
            log.warn("Can't load data from {}, please check filename", filePath);
            return null;
        }
        T t = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            Object obj = ois.readObject();
            t = (T) obj;
        } catch (IOException e) {
            log.error("read data to {} failed, ", filePath, e);
            return null;
        } catch (ClassNotFoundException e) {
            log.error("read data to {} failed, ", filePath, e);
            return null;
        }
        return t;
    }

    /**
     * Persistence the data to file.
     *
     * @param obj      data to be persistence.
     * @param filePath uri in which the data to be stored.
     * @return
     */
    static <T> boolean persistenceObject2File(T obj, String filePath) {
        if (obj == null || filePath == null || filePath.trim().length() < 1) {
            log.warn("Can't persistence data to {}, please check the user list and filename", filePath);
            return false;
        }

        if (!checkAndCreateDir(filePath)) {
            return false;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(obj);
        } catch (IOException e) {
            log.error("Persistence data to {} failed, ", filePath, e);
            return false;
        }
        return true;
    }


    /**
     * Check if the dir is exists, if not create it.
     *
     * @param filePath
     * @return
     */
    static boolean checkAndCreateDir(String filePath) {
        String fp = filePath.substring(0, filePath.lastIndexOf("/"));
        Path p = Paths.get(fp);
        try {
            if (!Files.exists(p))
                Files.createDirectories(p);
        } catch (IOException e) {
            log.error("Can't create dir {}", p.toUri().toString(), e);
            return false;
        }
        return true;
    }


    /**
     * 将udl转换为List<User>
     *
     * @return
     */
    static ArrayList<User> transUdl2UserList(List<UserDetails> udl) {
        return udl.stream().collect(
                () -> new ArrayList<>(),
                (list, ud) -> list.add(new User(ud.getUsername(), ud.getPassword(), ud.getAuthorities())),
                (m, n) -> m.addAll(n)
        );
    }


    /**
     * Get user home
     *
     * @return
     */
    static String getUserHome() {
        return System.getProperty("user.home").concat("/.sec/");
    }
}
