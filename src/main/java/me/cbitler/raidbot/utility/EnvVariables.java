package me.cbitler.raidbot.utility;

import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

/**
 * Class for loading variables from .env file
 * @author Christopher Bitler
 */
public class EnvVariables {
    private static final Logger log = LogManager.getLogger(EnvVariables.class);

    private static HashMap<String,String> variables = loadEnvVars();

    /**
     * Set a variable for usage within the application.
     * Attempts to register an empty or null variable name will be ignored.
     * @param key The variable name to register a value for
     * @param value The variable value to register for the given name
     */
    public static void setValue(String key, String value) {
        if(key == null || key == "") return;
        variables.put(key, value);
    }

    /**
     * Get a variable that was set in the .env file
     * @param key The variable name to get the value of
     * @return The value of the variable
     */
    public static String getValue(String key) {
        // remove sensitive token from static context once it has been retrieved
        if(key=="DISCORD_TOKEN") {
            String value = variables.get(key);
            variables.put(key, "<removed>");
            return value;
        }
        if(variables.containsKey(key)) return variables.get(key);
        else return null;
    }

    public static String getContextPath() {
        var path = System.getenv("INIT_WORKDIR");
        if(StringUtils.isBlank(path)) {
            path = Paths.get("") .toAbsolutePath() .toString();
        }
        return path;
    }

    /**
     * Load variables from .env file and token file to write them to the EnvVariables static context on startup
     */
    private static HashMap<String,String> loadEnvVars() {
        log.debug("loading env vars");
        Dotenv env;
        try{
            env = Dotenv.configure().directory(getContextPath()).ignoreIfMissing().load();
        }catch(Exception e){
            log.error("Loading env vars failed.");
            log.debug("Loading env vars failed with error.", e);
            throw new RuntimeException("Loading env vars failed.");
        }
        HashMap<String,String> vars = new HashMap<>();
        for (DotenvEntry e : env.entries()) {
            vars.put(e.getKey(), e.getValue());
        }
        return vars;
    }
}
