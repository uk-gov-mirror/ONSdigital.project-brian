package com.github.onsdigital.brian.readers.csdb;

import java.io.IOException;

import static java.lang.String.format;

/**
 * An exception occurred while attempting to process a CSDB file.
 */
public class DataBlockException extends IOException {

    public static final String LINE_LENGTH_ERROR = "error processing line type: %d - line length less than expected." +
            " Expected length: >= %d actual: %d, line index: %d";

    public static final String LINE_LENGTH_ERROR_UNKNOWN_TYPE = "error processing line type: unknown - line length " +
            "less than expected. Expected length: >= %d actual: %d, line index: %d";

    public static final String LINE_TYPE_INT_PARSE_ERROR = "error parsing CSDB line type to integer. Value: %s, line" +
            " index: %d";

    /**
     * Construct a new Exception
     *
     * @param message the exception message.
     * @param args    any addition details.
     */
    public DataBlockException(String message, Object... args) {
        super(format(message, args));
    }
}
