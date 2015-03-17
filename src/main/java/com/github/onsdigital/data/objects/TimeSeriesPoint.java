package com.github.onsdigital.data.objects;

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
    public static final String PERIOD_MONTHS = "months";
    public static final String PERIOD_YEARS = "years";
    public static final String PERIOD_QUARTERS = "quarters";
    public static final String PERIOD_ERROR = "error";

    public String timeLabel;
    public String value;

    public Date startDate; // ONLY ISSUE IS THAT START DATE MAY NOT PARSE
    public String period;

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
        // TODO FIND OUT WHETHER LEAP YEAR CORRECTIONS ARE NECESSARY
        String[] quarters = {"q1", "q2", "q3", "q4"};
        String[] quarterMonth = {"01", "04", "07", "10"};

        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        String[] outputMonths = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        int year;
        String lowerLabel = StringUtils.lowerCase(timeLabel);
        String startMonth = outputMonths[0];
        String quarter = "q1";
        boolean isMonth = false;
        boolean isQuarter = false;


        // CHECK FOR THE QUARTER
        for (int i = 0; i < quarters.length; i++) {
            if (StringUtils.contains(lowerLabel, quarters[i])) {
                isQuarter = true;
                quarter = quarters[i];
                startMonth = quarterMonth[i];
                period = PERIOD_QUARTERS;
                break;
            }
        }

        // CHECK FOR MONTHS
        if (!isQuarter) {
            if (lowerLabel.length() > 6) { // OPTION 1 - MONTH NUMBER
                try {
                    int monthNumber = Integer.parseInt(StringUtils.right(lowerLabel, 2));
                    if ((monthNumber > 0) & (monthNumber <= 12)) {
                        isMonth = true;
                        startMonth = outputMonths[monthNumber - 1];
                        period = PERIOD_MONTHS;
                    }
                } catch (NumberFormatException e) {

                }
            }
        }
        // CHECK FOR MONTHS - OPTION 2 - MONTH LABELS
        if (!isQuarter & !isMonth) { //
            for (int i = 0; i < months.length; i++) {
                if (StringUtils.contains(lowerLabel, months[i])) {
                    isMonth = true;
                    startMonth = outputMonths[i];
                    period = PERIOD_MONTHS;
                    break;
                }
            }
        }

        // TRY AND WORK OUT THE RELEVANT YEAR - IF A YEAR IS FOUND RETURN TRUE

        Pattern yearPattern = Pattern.compile("\\d{4}");
        Matcher m1 = yearPattern.matcher(timeLabel);
        if(m1.find()) {
            year = Integer.parseInt(m1.group(0));
            if (!isMonth & !isQuarter) {
                period = PERIOD_YEARS;
            }

            // SET THE DATE
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                startDate = df.parse(year + "-" + startMonth + "-01");
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
//        for (int tryYear = 1800; tryYear < 2050; tryYear++) {
//            if (StringUtils.contains(timeLabel, tryYear + "")) {
//                year = tryYear;
//                if (!isMonth & !isQuarter) {
//                    period = PERIOD_YEARS;
//                }
//
//                // SET THE DATE
//                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//                try {
//                    startDate = df.parse(year + "-" + startMonth + "-01");
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//                // STANDARDISE THE TIME LABEL
//                timeLabel = StringUtils.trim(timeLabel);
//                if (isMonth) {
//                    timeLabel = year + "-" + StringUtils.capitalize(startMonth);
//                } else if (isQuarter) {
//                    timeLabel = year + " " + StringUtils.upperCase(quarter);
//                }
//                return true;
//            }
//        }

        // DATE OR PARTIAL DATE - RETURN FALSE
        period = PERIOD_ERROR;
        return false;
    }

    public static String parseTimeLabel(String timeLabel) {
        TimeSeriesPoint point = new TimeSeriesPoint(timeLabel, "");
        return point.timeLabel;
    }

    public static String nextTimeLabel(String timeLabel) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] numberMonths = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};


        int startYear = 0;
        int startMonth = 0;

        // CREATE A POINT (WHICH DOES ALL OUR PARSING)
        TimeSeriesPoint pt = new TimeSeriesPoint(timeLabel, "");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pt.startDate);
        startYear = calendar.get(Calendar.YEAR);
        startMonth = calendar.get(Calendar.MONTH) + 1;

        // UPDATE THE STRING
        if (pt.period.equals(TimeSeriesPoint.PERIOD_YEARS)) { // FOR YEAR LABELS
            return String.format("%d", startYear + 1);
        } else if (pt.period.equals(TimeSeriesPoint.PERIOD_MONTHS)) { // FOR MONTH LABELS
            if (startMonth == 12) {
                startYear += 1;
                startMonth = 1;
            } else {
                startMonth += 1;
            }
            return String.format("%d-%s", startYear, numberMonths[startMonth - 1]);
        } else if (pt.period.equals(TimeSeriesPoint.PERIOD_QUARTERS)) { // FOR QUARTER LABELS
            int nextQuarter = (((startMonth + 3 - 1) / 3) + 1);
            if (nextQuarter == 5) {
                nextQuarter = 1;
                startYear += 1;
            }
            return String.format("%d Q%d", startYear, nextQuarter);
        }

        return timeLabel;
    }

    public static void main(String[] args) throws ParseException {

        Pattern yearPattern = Pattern.compile("\\d{4}");
        String label1 = "I'm a little teapot 22014 Oct";

        Matcher m1 = yearPattern.matcher(label1);
        while(m1.find()) {
            System.out.println(m1.group(0));
        }
    }
}