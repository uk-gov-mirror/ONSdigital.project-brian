package com.github.onsdigital.readers;

import com.github.davidcarboni.restolino.json.Serialiser;
import com.github.onsdigital.data.DataSet;
import com.github.onsdigital.data.TimeSeries;
import com.github.onsdigital.writers.objects.ListedTimeSeries;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by thomasridd on 13/03/15.
 */
public class SeriesReaderJSON {
    public static TimeSeries readFileFromListedJSON(String resourceName) throws IOException, URISyntaxException {
        URL resource = DataSet.class.getResource(resourceName);

        Path filePath = Paths.get(resource.toURI());
        try(InputStream stream = Files.newInputStream(filePath)) {
            ListedTimeSeries listedTimeSeries = Serialiser.deserialise(stream, ListedTimeSeries.class);
            return listedTimeSeries.toTimeSeries();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
