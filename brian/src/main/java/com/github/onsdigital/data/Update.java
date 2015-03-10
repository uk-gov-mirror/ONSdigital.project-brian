package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.UpdateForSeries;
import com.github.onsdigital.data.objects.TimeSeriesPair;

import java.util.ArrayList;

/**
 * Created by Tom.Ridd on 05/03/15.
 */

public class Update {
    ArrayList<UpdateForSeries> updateForSerieses = new ArrayList<>();

    public Update(Mapping mapping) {
        for(TimeSeriesPair pair: mapping.map) {
            UpdateForSeries seriesUpdate = new UpdateForSeries(pair.series, pair.masterSeries);
            updateForSerieses.add(seriesUpdate);
        }
    }
}
