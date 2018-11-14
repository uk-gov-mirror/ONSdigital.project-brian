package com.github.onsdigital.brian.logging;

import ch.qos.logback.classic.Level;
import com.github.onsdigital.logging.builder.LogMessageBuilder;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;

/**
 * Implmentation of the {@link LogMessageBuilder}. Provides convenience methods for logging message and adding common
 * parameters with the intention logging should be as unobtrusive as possible.
 */
public class LogEvent extends LogMessageBuilder {

    /**
     * Construct a new {@link LogEvent} instance.
     */
    public static LogEvent logEvent() {
        return new LogEvent();
    }

    /**
     * Construct a new {@link LogEvent} instance with the {@link Throwable} type provided.
     */
    public static LogEvent logEvent(Throwable t) {
        return new LogEvent(t);
    }

    /**
     * Construct a new {@link LogEvent} instance with the {@link Throwable} type and message provided.
     */
    public static LogEvent logEvent(Throwable t, String message) {
        return new LogEvent(t, message);
    }

    /**
     * Constructor - use static methods to obtain a new instance.
     */
    private LogEvent(String message) {
        super(message);
        setNamespace("brian");
    }

    /**
     * Constructor - use static methods to obtain a new instance.
     */
    private LogEvent(Throwable t, String message) {
        super(t, message);
        setNamespace("brian");
    }

    /**
     * Constructor - use static methods to obtain a new instance.
     */
    private LogEvent() {
        this("");
    }

    /**
     * Constructor - use static methods to obtain a new instance.
     */
    private LogEvent(Throwable t) {
        this(t, "");
    }

    @Override
    public String getLoggerName() {
        return "brian";
    }

    /**
     * Log a message with any parameters that have been set at TRACE level.
     */
    public void trace(String message) {
        this.logLevel = Level.TRACE;
        this.description = message;
        log();
    }

    /**
     * Log a message with any parameters that have been set at DEBUG level.
     */
    public void debug(String message) {
        this.logLevel = Level.DEBUG;
        this.description = message;
        log();
    }

    /**
     * Log a message with any parameters that have been set at INFO level.
     */
    public void info(String message) {
        this.logLevel = Level.INFO;
        this.description = message;
        log();
    }

    /**
     * Log a message with any parameters that have been set at WARN level.
     */
    public void warn(String message) {
        this.logLevel = Level.WARN;
        this.description = message;
        log();
    }

    /**
     * Log a message with any parameters that have been set at ERROR level.
     */
    public void error(String message) {
        this.logLevel = Level.ERROR;
        this.description = message;
        log();
    }

    /**
     * Add a parameter to the LogEvent
     */
    public LogEvent parameter(String key, Object value) {
        return addParamSafe(key, value);
    }

    /**
     * Add a uri to the LogEvent
     */
    public LogEvent uri(String uri) {
        return addParamSafe("uri", uri);
    }

    /**
     * Add a path to the LogEvent
     */
    public LogEvent path(String path) {
        return addParamSafe("path", path);
    }

    /**
     * Add a path to the LogEvent
     */
    public LogEvent path(Path path) {
        if (path != null) {
            return addParamSafe("path", path.toString());
        }
        return this;
    }

    /**
     * Add an index to the LogEvent
     */
    public LogEvent index(int i) {
        return addParamSafe("index", i);
    }

    private LogEvent addParamSafe(String key, Object value) {
        if (StringUtils.isNotEmpty(key) && null != value) {
            super.addParameter(key, value);
        }
        return this;
    }
}
