package com.github.onsdigital.async;

import com.github.onsdigital.data.TimeSeriesObject;

import java.util.concurrent.*;

/**
 * Created by thomasridd on 18/03/15.
 */
public class TimeSeriesCallable implements Callable<TimeSeriesObject> {

    TimeSeriesObject timeSeriesObject;

  public TimeSeriesCallable(TimeSeriesObject timeSeriesObject) {
      this.timeSeriesObject = timeSeriesObject;
  }

    @Override
    public TimeSeriesObject call() throws Exception {
        while (!timeSeriesObject.isComplete()) {
            Thread.yield();
//            Thread.sleep(1000);
        }
        return timeSeriesObject;
    }
}
