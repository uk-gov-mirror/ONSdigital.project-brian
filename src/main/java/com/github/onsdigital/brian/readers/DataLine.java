package com.github.onsdigital.brian.readers;

import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import org.apache.commons.lang3.StringUtils;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

public class DataLine {

    public static int getLineType(String line) {
        if (StringUtils.isBlank(line)) {
            logEvent().error("TODO");
            throw new RuntimeException("TODO");
        }
        if (StringUtils.isBlank(line)) {
            logEvent().error("TODO");
            throw new RuntimeException("TODO");
        }

        if (line.length() < 2) {
            logEvent().error("csdb file line less than the expected length");
            throw new RuntimeException("csdb file line less than the expected length");
        }

        String lineTypeStr = line.substring(0, 2);
        if (lineTypeStr.contains(" ")) {
            logEvent().warn("line containing space");
        }
        lineTypeStr = lineTypeStr.trim();

        try {
            return Integer.parseInt(lineTypeStr);
        } catch (NumberFormatException e) {
            logEvent(e).parameter("value", lineTypeStr)
                    .error("error attempting to parse CSDB line type to integer");
            throw e;
        }
    }

    public static String processLineType92(String line) {
        if (line.length() < 6) {
            throw new RuntimeException("line length not as expected");
        }
        return line.substring(2, 6);
    }

    public static String processLineType93(String line) {
        if (line.length() < 2) {
            throw new RuntimeException("line length not as expected");
        }
        return line.substring(2);
    }

    public static DateLabel processLineType96(String line) {
        if (line.length() < 11) {
            throw new RuntimeException("line length not as expected");
        }

        int startInd = Integer.parseInt(line.substring(9, 11).trim());
        String mqa = line.substring(2, 3);
        int year = Integer.parseInt(line.substring(4, 8));

        return new DateLabel(year, startInd, mqa);
    }

    public static void processLineType97(TimeSeriesObject series, String line, DateLabel dateLabel) {
        if (line.length() < 9) {
            throw new RuntimeException("line length not as expected");
        }

        String values = line.substring(2);
        while (values.length() > 9) {
            String oneValue = values.substring(0, 10).trim();

            TimeSeriesPoint point = new TimeSeriesPoint(dateLabel.getNextIteration(), oneValue);
            series.addPoint(point);

            values = values.substring(10);
        }
    }
}
