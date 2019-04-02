package com.github.onsdigital.brian.configuration;

import org.apache.commons.lang3.StringUtils;
import sun.rmi.transport.ObjectTable;

import java.util.HashMap;
import java.util.Map;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.error;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Application configuration.
 */
public class Config {

    private static Config config;
    private static final String PORT_KEY = "PORT";
    private static final String PRETTY_JSON_RESPONSE_KEY = "PRETTY_JSON_FORMAT";

    private final int port;
    private final boolean prettyJSONFormat;

    private Config() {
        this.port = getEnvInteger(PORT_KEY, 8083);
        this.prettyJSONFormat = Boolean.valueOf(getEnvString(PRETTY_JSON_RESPONSE_KEY, "false"));
    }

    /**
     * @return the port to run the application on.
     */
    public int getPort() {
        return this.port;
    }

    public boolean isPrettyJSONFormat() {
        return prettyJSONFormat;
    }

    public Map<String, Object> get() {
        return new HashMap() {{
           put("port", port);
        }};
    }

    /**
     * Return a singleton instance of the configuration object.
     */
    public static Config getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new Config();
                }
            }
        }
        return config;
    }

    private static int getEnvInteger(String key, int defaultVal) {
        String value = defaultIfBlank(System.getenv(key), System.getProperty(key));
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            error().exception(e).data("value", value).log("could not parse env value to integer applying default");
            return defaultVal;
        }
    }

    private static String getEnvString(String key, String defaultVal) {
        String value = defaultIfBlank(System.getenv(key), System.getProperty(key));
        return defaultIfBlank(value, defaultVal);
    }

}
