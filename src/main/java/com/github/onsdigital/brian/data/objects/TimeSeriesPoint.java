package com.github.onsdigital.brian.data.objects;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tom.Ridd on 03/03/15.
 */
public class TimeSeriesPoint {

    private static final String[] QUARTERS = {"q1", "q2", "q3", "q4"};
    private static final String[] QUARTER_MONTH = {"01", "04", "07", "10"};

    private static final String[] MONTHS = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
    private static final String[] OUTPUT_MONTHS = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

    public static final String PERIOD_MONTHS = "months";
    public static final String PERIOD_YEARS = "years";
    public static final String PERIOD_QUARTERS = "quarters";
    public static final String PERIOD_ERROR = "error";

    private static final Pattern QUARTER_REGEX = Pattern.compile("q\\d{1}");
    private static final Pattern MONTHS_REGEX = Pattern.compile("[a-z]{3}");
    private static final Pattern YEARS_REGEX = Pattern.compile("\\d{4}");

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public String timeLabel;
    public String value;

    public Date startDate; // ONLY ISSUE IS THAT START DATE MAY NOT PARSE
    public String period;

    public Date recordDate;

    @Override
    public String toString() {
        try {
            return timeLabel + ": " + value;
        } catch (Exception e) {
            return timeLabel + ": " + value;
        }
    }

    public TimeSeriesPoint(String aTimeLabel, String aValue) {
        value = aValue;
        timeLabel = aTimeLabel;
        parseTimeLabel();
    }

    /**
     * PARSES AN INPUT TIME LABEL
     *
     * @return success
     */
    private boolean parseTimeLabel() {
        int year;
        String lowerLabel = StringUtils.lowerCase(timeLabel);
        String startMonth = OUTPUT_MONTHS[0];
        String quarter = "q1";
        boolean isMonth = false;
        boolean isQuarter = false;


        // CHECK FOR THE QUARTER USING A REGEX
        Matcher matcher = QUARTER_REGEX.matcher(lowerLabel);
        if (matcher.find()) {
            quarter = matcher.group(0).toUpperCase();
            startMonth = QUARTER_MONTH[Integer.parseInt(quarter.substring(1)) - 1];

            isQuarter = true;
            period = PERIOD_QUARTERS;
        }

        // CHECK FOR MONTHS
        if (!isQuarter) {
            if (lowerLabel.length() > 6) { // OPTION 1 - MONTH NUMBER
                try {
                    int monthNumber = Integer.parseInt(StringUtils.right(lowerLabel, 2));
                    if ((monthNumber > 0) & (monthNumber <= 12)) {
                        isMonth = true;
                        startMonth = OUTPUT_MONTHS[monthNumber - 1];
                        period = PERIOD_MONTHS;
                    }
                } catch (NumberFormatException e) {

                }
            }
        }

        // CHECK FOR MONTHS - OPTION 2 - MONTH LABELS
        if (!isQuarter & !isMonth) { //
            matcher = MONTHS_REGEX.matcher(lowerLabel); // SPEED THINGS UP BY CHECKING WHETHER A MONTH CAN EXIST
            if (matcher.find()) {
                for (int i = 0; i < MONTHS.length; i++) {
                    if (StringUtils.contains(lowerLabel, MONTHS[i])) {
                        isMonth = true;
                        startMonth = OUTPUT_MONTHS[i];
                        period = PERIOD_MONTHS;
                        break;
                    }
                }
            }
        }

        // TRY AND WORK OUT THE RELEVANT YEAR - IF A YEAR IS FOUND RETURN TRUE
        matcher = YEARS_REGEX.matcher(timeLabel);

        if (matcher.find()) {
            year = Integer.parseInt(matcher.group(0));
            if (!isMonth & !isQuarter) {
                period = PERIOD_YEARS;
            }

            // SET THE DATE
            try {
                startDate = dateFormat.parse(year + "-" + startMonth + "-01");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // STANDARDISE THE TIME LABEL
            timeLabel = StringUtils.trim(timeLabel);
            if (isMonth) {
                timeLabel = year + "-" + StringUtils.capitalize(startMonth);
            } else if (isQuarter) {
                timeLabel = year + " " + StringUtils.upperCase(quarter);
            }
            return true;
        }

        // DATE OR PARTIAL DATE - RETURN FALSE
        period = PERIOD_ERROR;
        return false;
    }

    public static String parseTimeLabel(String timeLabel) {
        TimeSeriesPoint point = new TimeSeriesPoint(timeLabel, "");
        return point.timeLabel;
    }

    public static String nextTimeLabel(String timeLabel) {
        int startYear = 0;
        int startMonth = 0;

        // CREATE A POINT (WHICH DOES ALL OUR PARSING)
        TimeSeriesPoint pt = new TimeSeriesPoint(timeLabel, "");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pt.startDate);
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH) + 1;

        // UPDATE THE STRING
        if (pt.period.equals(PERIOD_YEARS)) { // FOR YEAR LABELS
            return String.format("%d", startYear + 1);
        } else if (pt.period.equals(PERIOD_MONTHS)) { // FOR MONTH LABELS
            if (startMonth == 12) {
                startYear += 1;
                startMonth = 1;
            } else {
                startMonth += 1;
            }
            return String.format("%d-%s", startYear, OUTPUT_MONTHS[startMonth - 1]);
        } else if (pt.period.equals(PERIOD_QUARTERS)) { // FOR QUARTER LABELS
            int nextQuarter = (((startMonth + 3 - 1) / 3) + 1);
            if (nextQuarter == 5) {
                nextQuarter = 1;
                startYear += 1;
            }
            return String.format("%d Q%d", startYear, nextQuarter);
        }

        return timeLabel;
    }

}