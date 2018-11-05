package com.github.onsdigital.brian.configuration;

import org.apache.commons.lang3.StringUtils;

import static com.github.onsdigital.brian.logging.Logger.logEvent;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class Config {

    private static Config config;

    private final int port;

    private Config() {
        this.port = getEnvInteger("PORT", 8083);

        logEvent().parameter("port", port)
                .info("loading project-brian configuration");
    }

    public int getPort() {
        return this.port;
    }

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
