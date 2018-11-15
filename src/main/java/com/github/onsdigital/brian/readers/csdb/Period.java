package com.github.onsdigital.brian.readers.csdb;

import org.apache.commons.lang3.StringUtils;

/**
 * Constants representing each of the period values in a <i>.csdb</i> file.
 */
public enum Period {
    MONTH("M"),

    QUARTER("Q"),

    ANNUAL("A"),

    YEAR("Y");

    private final String value;

    Period(String s) {
        this.value = s;
    }

    /**
     * Extract the {@link Period} value from the <i>.csdb</i> file line.
     *
     * @param line the line to extract the value from.
     * @return the period for the value defined.
     * @throws DataBlockException if the line is null, less than the expected length or is not a recognised value.
     */
    public static Period fromLine(String line) throws DataBlockException {
        if (StringUtils.isBlank(line) || line.length() < 3) {
            throw new DataBlockException("Datelabel period value invalid");
        }

        String val = line.substring(2, 3);
        switch (val) {
            case "Y":
                return YEAR;
            case "A":
                return ANNUAL;
            case "M":
                return MONTH;
            case "Q":
                return QUARTER;
        }
        throw new DataBlockException("unknown Datelabel period, value: %s", val);
    }
}
