package com.github.onsdigital.brian.configuration;

import org.apache.commons.lang3.StringUtils;

import static com.github.onsdigital.brian.logging.LogEvent.logEvent;
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

        logEvent().parameter(PORT_KEY, port)
                .parameter(PRETTY_JSON_RESPONSE_KEY, prettyJSONFormat)
                .info("loading project-brian configuration");
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
            logEvent(e).parameter("value", value).error("could not parse env value to integer applying default");
            return defaultVal;
        }
    }

    private static String getEnvString(String key, String defaultVal) {
        String value = defaultIfBlank(System.getenv(key), System.getProperty(key));
        return defaultIfBlank(value, defaultVal);
    }

}
