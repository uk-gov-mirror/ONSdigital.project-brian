package com.github.onsdigital.brian.readers.csdb;

import java.io.IOException;

import static java.lang.String.format;

public class CSDBLineParseException extends IOException {

    public static final String LINE_LENGTH_ERROR = "error processing line type: %d - line length less than expected." +
            " Expected length: >= %d actual: %d, line index: %d";

    public static final String LINE_LENGTH_ERROR_UNKNOWN_TYPE = "error processing line type: unknown - line length " +
            "less than expected. Expected length: >= %d actual: %d, line index: %d";

    public static final String LINE_TYPE_INT_PARSE_ERROR = "error parsing CSDB line type to integer. Value: %s, line" +
            " index: %d";

    public CSDBLineParseException(String message, Object...args) {
        super(format(message,args));
    }
}
