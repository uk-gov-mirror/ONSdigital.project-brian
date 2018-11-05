package com.github.onsdigital.brian.publishers;

import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;
import com.github.onsdigital.content.page.base.PageDescription;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.content.partial.TimeseriesValue;
import com.github.onsdigital.content.partial.markdown.MarkdownSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by thomasridd on 09/06/15.
 */
public class TimeSeriesPublisher {

    static final String[] SHORT_MONTHS = {
            "JAN", "FEB", "MAR", "APR",
            "MAY", "JUN", "JUL", "AUG",
            "SEP", "OCT", "NOV", "DEC"};

    static final String[] LONG_MONTHS = {
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"};


    public static List<TimeSeries> convertToContentLibraryTimeSeriesList(List<TimeSeriesObject> timeSeriesObjectList) {
        List<TimeSeries> results = new ArrayList<>();
        for (TimeSeriesObject timeSeriesObject : timeSeriesObjectList) {
            results.add(convertToContentLibraryTimeSeries(timeSeriesObject));
        }
        return results;
    }

    public static TimeSeries convertToContentLibraryTimeSeries(TimeSeriesObject timeSeriesObject) {
        TimeSeries timeSeriesPage = new TimeSeries();

        PageDescription description = new PageDescription();
        description.setCdid(timeSeriesObject.taxi);
        description.setTitle(timeSeriesObject.name);

        MarkdownSection section = new MarkdownSection();
        section.setTitle("description");
        section.setMarkdown(timeSeriesObject.name);

        timeSeriesPage.setSection(new MarkdownSection());
        timeSeriesPage.setDescription(description);

        for (String key : timeSeriesObject.points.keySet()) {
            TimeSeriesPoint point = timeSeriesObject.points.get(key);
            TimeseriesValue value = convertToContentLibaryTimeseriesValue(point);

            timeSeriesPage.add(value);
        }

        TimeseriesValue value = getTimeSeriesHighlightPoint(timeSeriesPage);
        timeSeriesPage.getDescription().setNumber(value.value);
        timeSeriesPage.getDescription().setDate(value.date);

        return timeSeriesPage;
    }

    static TimeseriesValue convertToContentLibaryTimeseriesValue(TimeSeriesPoint point) {
        TimeseriesValue value = new TimeseriesValue();
        value.value = point.value;
        value.year = point.timeLabel.substring(0, 4);
        value.date = value.year;

        value.month = "";
        if (point.period.equalsIgnoreCase("months")) {
            String monthNumber = point.timeLabel.substring(point.timeLabel.length() - 2, point.timeLabel.length());
            int monthIndex = Integer.parseInt(monthNumber) - 1;

            value.month = LONG_MONTHS[monthIndex];
            value.date = value.year + " " + SHORT_MONTHS[monthIndex];
        }

        value.quarter = "";
        if (point.period.equalsIgnoreCase("quarters")) {
            value.quarter = point.timeLabel.substring(point.timeLabel.length() - 2, point.timeLabel.length());
            value.date = value.year + " " + value.quarter;
        }

        return value;
    }

    static TimeseriesValue getTimeSeriesHighlightPoint(TimeSeries series) {
        List<TimeseriesValue> values = new ArrayList<>();

        if (series.months != null && series.months.size() > 0) {
            values.addAll(series.months);
        } else if (series.quarters != null && series.quarters.size() > 0) {
            values.addAll(series.quarters);
        } else if (series.years != null && series.years.size() > 0) {
            values.addAll(series.years);
        }

        class CustomComparator implements Comparator<TimeseriesValue> {
            @Override
            public int compare(TimeseriesValue o1, TimeseriesValue o2) {
                return o1.toDate().compareTo(o2.toDate());
            }
        }

        if (values.size() > 0) {
            Collections.sort(values, new CustomComparator());
            TimeseriesValue value = values.get(values.size() - 1);
            return value;
        }
        return null;
    }
}
