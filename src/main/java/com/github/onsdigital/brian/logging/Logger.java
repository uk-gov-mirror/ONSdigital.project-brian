package com.github.onsdigital.brian.logging;

import ch.qos.logback.classic.Level;
import com.github.onsdigital.logging.builder.LogMessageBuilder;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;

public class Logger extends LogMessageBuilder {

    public static Logger logEvent() {
        return new Logger();
    }

    public static Logger logEvent(Throwable t) {
        return new Logger(t);
    }

    public static Logger logEvent(Throwable t, String message) {
        return new Logger(t, message);
    }

    private Logger(String message) {
        super(message);
        setNamespace("brian");
    }

    private Logger(Throwable t, String message) {
        super(t, message);
        setNamespace("brian");
    }

    private Logger() {
        this("");
    }

    private Logger(Throwable t) {
        this(t, "");
    }

    @Override
    public String getLoggerName() {
        return "brian";
    }

    public void trace(String message) {
        this.logLevel = Level.TRACE;
        this.description = message;
        log();
    }

    public void debug(String message) {
        this.logLevel = Level.DEBUG;
        this.description = message;
        log();
    }

    public void info(String message) {
        this.logLevel = Level.INFO;
        this.description = message;
        log();
    }

    public void warn(String message) {
        this.logLevel = Level.WARN;
        this.description = message;
        log();
    }

    public void error(String message) {
        this.logLevel = Level.ERROR;
        this.description = message;
        log();
    }

    public Logger parameter(String key, Object value) {
        return addParamSafe(key, value);
    }

    public Logger uri(String uri) {
        return addParamSafe("uri", uri);
    }

    public Logger path(String path) {
        return addParamSafe("path", path);
    }

    public Logger path(Path path) {
        if (path != null) {
            return addParamSafe("path", path.toString());
        }
        return this;
    }

    public Logger index(int i) {
        return addParamSafe("index", i);
    }

    private Logger addParamSafe(String key, Object value) {
        if (StringUtils.isNotEmpty(key) && null != value) {
            super.addParameter(key, value);
        }
        return this;
    }
}
