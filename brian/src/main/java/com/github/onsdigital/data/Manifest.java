package com.github.onsdigital.data;

import com.github.onsdigital.data.objects.ManifestForSeries;
import com.github.onsdigital.data.objects.TimeSeriesPair;

import java.util.ArrayList;

/**
 * Created by Tom.Ridd on 05/03/15.
 */

public class Manifest {
    ArrayList<ManifestForSeries> manifestForSerieses = new ArrayList<>();

    public Manifest(Mapping mapping) {
        for(TimeSeriesPair pair: mapping.map) {
            ManifestForSeries seriesManifest = new ManifestForSeries(pair.series, pair.masterSeries);
            manifestForSerieses.add(seriesManifest);
        }
    }
}
