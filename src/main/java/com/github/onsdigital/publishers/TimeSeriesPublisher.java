package com.github.onsdigital.publishers;

import com.github.onsdigital.content.statistic.data.TimeSeries;
import com.github.onsdigital.data.TimeSeriesObject;

/**
 * Created by thomasridd on 09/06/15.
 */
public class TimeSeriesPublisher {
    public static TimeSeries timeSeriesObjectAsTimeSeries(TimeSeriesObject timeSeriesObject) {
        TimeSeries timeSeriesPage = new TimeSeries();
        timeSeriesPage.cdid = timeSeriesObject.taxi;
        timeSeriesPage.description = timeSeriesObject.name;

        // Stuff for seasonal adjustment

        

        return null;
    }
}
