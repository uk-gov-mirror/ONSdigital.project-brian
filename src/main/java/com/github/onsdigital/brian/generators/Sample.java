package com.github.onsdigital.brian.generators;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;

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
    public static TimeSeriesObject randomWalk(long seed, double start, double volatility, int firstYear, int lastYear, boolean withMonths, boolean withQuarters, boolean withYears) {

        TimeSeriesObject series = new TimeSeriesObject();


        series.taxi = String.format("RAND%05d", seed) ;
        series.name = "Random Series " + series.taxi + " from " + firstYear;
        if ( withYears ) { series = TimeSeriesObject.merge(series, randomWalkYear(seed, start, volatility, firstYear, lastYear), true); }
        if ( withMonths ) { series = TimeSeriesObject.merge(series, randomWalkMonths(seed, start, volatility, firstYear, lastYear), true); }
        if ( withQuarters ) { series = TimeSeriesObject.merge(series, randomWalkQuarters(seed, start, volatility, firstYear, lastYear), true); }

        return series;
    }

    private static TimeSeriesObject randomWalkYear(long seed, double start, double volatility, int firstYear, int lastYear) {
        Random generator = new Random(seed);
        TimeSeriesObject series = new TimeSeriesObject();

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
    private static TimeSeriesObject randomWalkQuarters(long seed, double start, double volatility, int firstYear, int lastYear) {
        Random generator = new Random(seed);
        TimeSeriesObject series = new TimeSeriesObject();

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
    private static TimeSeriesObject randomWalkMonths(long seed, double start, double volatility, int firstYear, int lastYear) {
        Random generator = new Random(seed);
        TimeSeriesObject series = new TimeSeriesObject();
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

    /**
     * GENERATE A DATASET OF RANDOM WALKS
     * @param count
     * @param firstSeed
     * @param start
     * @param volatility
     * @param firstYear
     * @param lastYear
     * @param withMonths
     * @param withQuarters
     * @param withYears
     * @return
     */
    public static TimeSeriesDataSet randomWalks(int count, long firstSeed, double start, double volatility, int firstYear, int lastYear, boolean withMonths, boolean withQuarters, boolean withYears) {
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        timeSeriesDataSet.name = "RAND%05d";
        long seed = firstSeed;

        for(int i = 0; i < count; i++) {
            TimeSeriesObject series = randomWalk(seed, start, volatility, firstYear, lastYear, withMonths, withQuarters, withYears);
            timeSeriesDataSet.timeSeries.put(series.taxi, series);
            seed += 1;
        }
        return timeSeriesDataSet;
    }

    public static TimeSeriesObject quickWalk(long seed) {
        return randomWalk(seed, 100, 1, 2010, 2014, true, true, true);
    }

    public static TimeSeriesDataSet quickWalks(int count, long seedZero){
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        timeSeriesDataSet.name = String.format("Random DataSet with %d series starting with RAND%05d", count, seedZero);
        timeSeriesDataSet.source = "Sample.quickWalks()";
        for(int i = 0; i < count; i++) {
            timeSeriesDataSet.addSeries(Sample.quickWalk(seedZero + i));
        }
        return timeSeriesDataSet;
    }

}
