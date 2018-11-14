package com.github.onsdigital.brian.readers.csdb;

import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;

@FunctionalInterface
public interface TimeSeriesPointGenerator {

    TimeSeriesPoint create(DateLabel dateLabel, String pointValue);
}
