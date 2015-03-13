package com.github.onsdigital.generators;

import com.github.onsdigital.api.Data;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.data.objects.TimeSeriesPoint;

import java.util.Date;
import java.util.Random;

/**
 * Created by thomasridd on 12/03/15.
 */
public class Sample {

    /**
     * GENERATE A RANDOM TIME SERIES
     *
     * @param seed
     * @param firstYear
     * @param lastYear
     * @param withMonths
     * @param withQuarters
     * @param withYears
     * @return
     */
    public static TimeSeries randomWalk(long seed, double start, double volatility, int firstYear, int lastYear, boolean withMonths, boolean withQuarters, boolean withYears) {

        TimeSeries series = new TimeSeries();


        series.taxi = String.format("RAND%05d", seed) ;
        series.name = "Random Series " + series.taxi + " from " + firstYear;
        if ( withYears ) { series = TimeSeries.merge(series, randomWalkYear(seed, start, volatility, firstYear, lastYear), true); }
        if ( withMonths ) { series = TimeSeries.merge(series, randomWalkMonths(seed, start, volatility, firstYear, lastYear), true); }
        if ( withQuarters ) { series = TimeSeries.merge(series, randomWalkQuarters(seed, start, volatility, firstYear, lastYear), true); }

        return series;
    }

    private static TimeSeries randomWalkYear(long seed, double start, double volatility, int firstYear, int lastYear) {
        Random generator = new Random(seed);
        TimeSeries series = new TimeSeries();

        double current = start;
        for(int year = firstYear; year <= lastYear; year++) {
            String timeLabel = String.format("%d", year);

            series.addPoint(new TimeSeriesPoint(timeLabel, String.format("%.2f", current)));

            for(int month = 1; month <= 12; month++) {
                current = current + (1 - 2 * generator.nextDouble()) * volatility;
            }
        }

        return series;
    }
    private static TimeSeries randomWalkQuarters(long seed, double start, double volatility, int firstYear, int lastYear) {
        Random generator = new Random(seed);
        TimeSeries series = new TimeSeries();

        double current = start;
        for(int year = firstYear; year <= lastYear; year++) {
            for(int quarter = 1; quarter <= 4; quarter++ ) {
                String timeLabel = String.format("%d Q%d", year, quarter);

                series.addPoint(new TimeSeriesPoint(timeLabel, String.format("%.2f", current)));

                for (int month = 1; month <= 3; month++) {
                    current = current + (1 - 2 * generator.nextDouble()) * volatility;
                }
            }
        }

        return series;
    }
    private static TimeSeries randomWalkMonths(long seed, double start, double volatility, int firstYear, int lastYear) {
        Random generator = new Random(seed);
        TimeSeries series = new TimeSeries();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        double current = start;
        for(int year = firstYear; year <= lastYear; year++) {
                for(int month = 1; month <= 12; month++) {
                    String timeLabel = String.format("%d %s", year, months[month - 1]);
                    series.addPoint(new TimeSeriesPoint(timeLabel, String.format("%.2f", current)));
                    current = current + (1 - 2 * generator.nextDouble()) * volatility;
                }
            }

        return series;
    }

    public static DataSet randomWalks(int count, long firstSeed, double start, double volatility, int firstYear, int lastYear, boolean withMonths, boolean withQuarters, boolean withYears) {
        DataSet dataSet = new DataSet();
        dataSet.name = "RAND%05d";
        long seed = firstSeed;

        for(int i = 0; i < count; i++) {
            TimeSeries series = randomWalk(seed, start, volatility, firstYear, lastYear, withMonths, withQuarters, withYears);
            dataSet.timeSeries.put(series.taxi, series);
            seed += 1;
        }
        return dataSet;
    }

    public static TimeSeries quickWalk(long seed) {
        return randomWalk(seed, 100, 1, 2010, 2014, true, true, true);
    }

    public static DataSet randomWalkDataSet(int count, long seedZero, double start, double volatility, int firstYear, int lastYear, boolean withMonths, boolean withQuarters, boolean withYears){
        DataSet dataSet = new DataSet();
        dataSet.name = String.format("Random DataSet with %d series and start seed %05d", count, seedZero);
        dataSet.source = "Sample random walk dataset";
        for(int i = 0; i < count; i++) {
            dataSet.addSeries(Sample.randomWalk(seedZero + i, start, volatility, firstYear, lastYear, withMonths, withQuarters, withYears));
        }
        return dataSet;
    }

    public static void main(String[] args) {
        TimeSeries series = randomWalk(100, 100, 1, 1997, 1998, true, false, true);

        System.out.println(series);
    }
}
