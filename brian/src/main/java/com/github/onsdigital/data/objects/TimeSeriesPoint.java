package com.github.onsdigital.data.objects;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    //public int year;
    public Date startDate;
    public String period;

    @Override
    public String toString() {
        try {
            return timeLabel + ": " + value;
        } catch (Exception e) {
            return timeLabel + ": " + value;
        }
    }

    public TimeSeriesPoint(String aTimeLabel, String aValue) throws ParseException {
        value = aValue;
        timeLabel = aTimeLabel;
        parseTimeLabel();
    }

    private boolean parseTimeLabel() throws ParseException {
        // TODO FIND OUT WHETHER LEAP YEAR CORRECTIONS ARE NECESSARY
        String[] quarters = {"q1", "q2", "q3", "q4"};
        String[] quarterMonth = {"jan", "apr", "jul", "oct"};

        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

        int year;
        String lowerLabel = StringUtils.lowerCase(timeLabel);
        String startMonth = "jan";
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
            for (int i = 0; i < months.length; i++) {
                if (StringUtils.contains(lowerLabel, months[i])) {
                    isMonth = true;
                    startMonth = months[i];
                    period = PERIOD_MONTHS;
                    break;
                }
            }
        }

        // TRY AND WORK OUT THE RELEVANT YEAR - IF A YEAR IS FOUND RETURN TRUE
        for (int tryYear = 1800; tryYear < 2050; tryYear++) {
            if (StringUtils.contains(timeLabel, tryYear + "")) {
                year = tryYear;
                if (!isMonth & !isQuarter) {
                    period = PERIOD_YEARS;
                }

                // SET THE DATE
                DateFormat df = new SimpleDateFormat("MMM dd yyyy");
                startDate = df.parse(startMonth + " 01 " + year);

                // STANDARDISE THE TIME LABEL
                timeLabel = StringUtils.trim(timeLabel);
                if(isMonth) {
                    timeLabel = year + " " + StringUtils.capitalize(startMonth);
                } else if(isQuarter) {
                    timeLabel = year + " " + StringUtils.upperCase(quarter);
                }
                return true;
            }
        }

        // DATE OR PARTIAL DATE - RETURN FALSE
        period = PERIOD_ERROR;
        return false;
    }

    public static String parseTimeLabel(String timeLabel) throws ParseException {
        TimeSeriesPoint point = new TimeSeriesPoint(timeLabel, "");
        return point.timeLabel;
    }

    public static String nextTimeLabel(String timeLabel) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


        int startYear = 0;
        int startMonth = 0;

        try {
            // CREATE A POINT (WHICH DOES ALL OUR PARSING)
            TimeSeriesPoint pt = new TimeSeriesPoint(timeLabel, "");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(pt.startDate);
            startYear = calendar.get(Calendar.YEAR);
            startMonth = calendar.get(Calendar.MONTH) + 1;

            // UPDATE THE STRING
            if(pt.period.equals(TimeSeriesPoint.PERIOD_YEARS)) { // FOR YEAR LABELS
                return String.format("%d", startYear + 1);
            } else if(pt.period.equals(TimeSeriesPoint.PERIOD_MONTHS)) { // FOR MONTH LABELS
                if(startMonth == 12) {
                    startYear += 1;
                    startMonth = 1;
                } else {
                    startMonth += 1;
                }
                return String.format("%d %s", startYear, months[startMonth - 1]);
            } else if(pt.period.equals(TimeSeriesPoint.PERIOD_QUARTERS)) { // FOR QUARTER LABELS
                int nextQuarter = (((startMonth + 3 - 1) / 3) + 1);
                if(nextQuarter == 5) {
                    nextQuarter = 1;
                    startYear += 1;
                }
                return String.format("%d Q%d", startYear, nextQuarter);
            }
        } catch (ParseException e) {
            return "";
        }
        return timeLabel;
    }

    public static void main(String[] args) throws ParseException {
        TimeSeriesPoint p = new TimeSeriesPoint("2014 Oct", "100");
        p.parseTimeLabel();
        System.out.println(p);
        System.out.println(TimeSeriesPoint.nextTimeLabel(p.timeLabel));

        p = new TimeSeriesPoint("2014", "100");
        p.parseTimeLabel();
        System.out.println(p);
        System.out.println(TimeSeriesPoint.nextTimeLabel(p.timeLabel));

        p = new TimeSeriesPoint("2014 q3", "100");
        p.parseTimeLabel();
        System.out.println(p);
        System.out.println(TimeSeriesPoint.nextTimeLabel(p.timeLabel));
    }
}