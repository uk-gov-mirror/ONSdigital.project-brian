package com.github.onsdigital.brian.readers;

import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.exception.BrianException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by thomasridd on 2/3/16.
 */
public interface DataSetReader {

    /**
     * READS A DATASET FROM A RESOURCE FILE GIVEN AN ABSOLUTE PATH
     *
     * @param filePath - THE PATH NAME
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public TimeSeriesDataSet readFile(Path filePath, SecretKey key) throws IOException, BrianException;

}
