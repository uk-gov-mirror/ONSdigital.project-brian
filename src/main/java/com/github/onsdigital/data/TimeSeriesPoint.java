package com.github.onsdigital.data;

import com.sun.javafx.binding.SelectBinding;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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
    public int year;
    public Date startDate;

    public String period;

    @Override
    public String toString() {
        SimpleDateFormat ft =
                new SimpleDateFormat("MMM yyyy");
        try {
            return timeLabel + " - " + value + " (" + period + " starting at " + ft.format(startDate) + ")";
        } catch (Exception e) {
            return timeLabel + " - " + value;
        }
    }

    public TimeSeriesPoint(String aTimeLabel, String aValue) {
        value = aValue;
        timeLabel = aTimeLabel;
    }

    public boolean parseTimeLabel() throws ParseException {
        // TODO FIND OUT WHETHER LEAP YEAR CORRECTIONS ARE NECESSARY
        String[] quarters = {"q1", "q2", "q3", "q4"};
        String[] quarterMonth = {"jan", "apr", "jul", "oct"};

        String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sept", "oct", "nov", "dec"};

        String lowerLabel = StringUtils.lowerCase(timeLabel);
        String startMonth = "jan";
        boolean isMonth = false;
        boolean isQuarter = false;


        // CHECK FOR THE QUARTER
        for (int i = 0; i < quarters.length; i++) {
            if (StringUtils.contains(lowerLabel, quarters[i])) {
                isQuarter = true;
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
        for (int tryYear = 1841; tryYear < 2050; tryYear++) {
            if (StringUtils.contains(timeLabel, tryYear + "")) {
                year = tryYear;
                if (!isMonth & !isQuarter) {
                    period = PERIOD_YEARS;
                }
                DateFormat df = new SimpleDateFormat("MMM dd yyyy");
                startDate = df.parse(startMonth + " 01 " + year);

                return true;
            }
        }

        // DATE OR PARTIAL DATE - RETURN FALSE
        period = PERIOD_ERROR;
        return false;
    }

    public static void main(String[] args) throws ParseException {
        TimeSeriesPoint p = new TimeSeriesPoint("2014 Oct", "100");
        p.parseTimeLabel();
        System.out.println(p);

        p = new TimeSeriesPoint("Mid August 2014", "100");
        p.parseTimeLabel();
        System.out.println(p);

        p = new TimeSeriesPoint("2014", "100");
        p.parseTimeLabel();
        System.out.println(p);

        p = new TimeSeriesPoint("2014 q3", "100");
        p.parseTimeLabel();
        System.out.println(p);
    }
}