package com.github.onsdigital.data.objects;

import java.util.Comparator;

/**
 * Created by thomasridd on 13/03/15.
 */
public class TimeSeriesPointComparator implements Comparator<TimeSeriesPoint>{
    @Override
    public int compare(TimeSeriesPoint o1, TimeSeriesPoint o2) {
        return o1.timeLabel.compareTo(o2.timeLabel);
    }
}
