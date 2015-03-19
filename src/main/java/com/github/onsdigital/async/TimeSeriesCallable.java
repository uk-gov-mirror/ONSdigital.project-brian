package com.github.onsdigital.async;

import com.github.onsdigital.data.TimeSeries;

import java.util.concurrent.*;

/**
 * Created by thomasridd on 18/03/15.
 */
public class TimeSeriesCallable implements Callable<TimeSeries> {

    TimeSeries timeSeries;

  public TimeSeriesCallable(TimeSeries timeSeries) {
      this.timeSeries = timeSeries;
  }

    @Override
    public TimeSeries call() throws Exception {
        while (!timeSeries.isComplete()) {
            Thread.yield();
//            Thread.sleep(1000);
        }
        return timeSeries;
    }
}
