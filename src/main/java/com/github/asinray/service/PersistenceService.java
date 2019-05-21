package com.github.asinray.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import  org.springframework.security.core.userdetails.UserDetails;

/**
 * Persistence Service persistence the data to file .
 * 
 * @author: Asin Liu
 */


public interface PersistenceService {

    static final Logger log = LoggerFactory.getLogger(PersistenceService.class);
   
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String DEF_SEC_PATH = USER_HOME.concat("/.sec/");
    public static final String USER_STORE_FILE = DEF_SEC_PATH.concat(".udl");
    public static final String APP_TOKEN_STORE_FILE = DEF_SEC_PATH.concat(".atm");
    public static final String ANT_MATCHER_STORE_FILE = DEF_SEC_PATH.concat(".am");



    public static ArrayList<User> loadUsers(String filePath){
        return loadFromFile(filePath);
    }
    public static boolean saveUsers(List<User> list,String filePath){
        return persistenceObject2File(list, filePath);
    }

    public static Map<String, String> loadAntMatchers(String filePath){
        return loadFromFile(filePath);
    };
    public static boolean saveAntMatchers(Map<String,String> antMatchers,String filePath){
        return persistenceObject2File(antMatchers, filePath);
    }

    public static Map<String, String> loadRepoTokenMap(String filePath){
        return loadFromFile(filePath);
    };
    public static boolean saveRepoTokenMap(Map<String,String> repoTokenMap,String filePath){
        return persistenceObject2File(repoTokenMap, filePath);
    }



    /**
     * Extract user from InMemoryUserDetailsManager
     * 
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

         /*   userList = map.values.stream().map(o->{
                UserDetails ud = (UserDetails)o;
                User u = new User(ud.getUsername(),ud.getPassword(),ud.getAuthorities());
                return u;
            }).collect(Collectors.toList());*/
        }catch(NoSuchFieldException noSuchFieldError) {
            // TODO: 
            log.error("No such field error:",noSuchFieldError);
        }catch(IllegalAccessException iae){
            log.error("IllegalArgumentException:", iae);
        }
        return userList;
    }
    

     /**
     * load data from file
     * @param filePath
     * @param <T>
     * @return
     */
    public static <T> T loadFromFile(String filePath){
        if(filePath == null || filePath.trim().length()<1 || !Files.exists(Paths.get(filePath))){
            log.warn("Can't load data from {}, please check filename",filePath);
            return null;
        }
        T t = null;
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))){
            Object obj = ois.readObject();
            t = (T)obj;
        } catch (IOException e) {
            log.error("read data to {} failed, ", filePath,e);
            return null;
        } catch (ClassNotFoundException e) {
            log.error("read data to {} failed, ", filePath,e);
            return null;
        }
        return t;
    }

    /**
     * Persistence the data to file.
     * @param obj          data to be persistence.
     * @param filePath      uri in which the data to be stored.
     * @return
     */
    public static <T> boolean  persistenceObject2File(T obj, String filePath){
        if(obj == null || filePath.trim().length()<1){
            log.warn("Can't persistence data to {}, please check the user list and filename",filePath);
            return false;
        }

        if (!checkAndCreateDir(filePath)) {
            return false;
        }
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))){
            oos.writeObject(obj);
        } catch (IOException e) {
            log.error("Persistence data to {} failed, ", filePath,e);
            return false;
        }
        return true;
    }
   

    /**
     * Check if the dir is exists, if not create it.
     * @param filePath
     * @return
     */ 
    public static boolean checkAndCreateDir(String filePath) {
        String fp = filePath.substring(0, filePath.lastIndexOf("/"));
        Path p = Paths.get(fp);
        try {
            if (!Files.exists(p))
                Files.createDirectories(p);
        } catch (IOException e) {
            log.error("Can't create dir {}", p.toUri().toString(),e);
            return false;
        }
        return true;
    }
}