package com.github.onsdigital.brian.readers.csdb;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.readers.DataSetReader;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.github.onsdigital.brian.logging.Logger.logEvent;

/**
 * Created by thomasridd on 10/03/15.
 * <p>
 * METHODS TO READ DATA FROM CSDB STANDARD TEXT FILES
 */
public class DataSetReaderCSDB implements DataSetReader {

    private static final Pattern p = Pattern.compile("^[0-9]{2}$");
    private static final String CSDB_FILE_CHAR_SET = "cp1252";

    /**
     * READS A DATASET FROM A RESOURCE FILE GIVEN AN ABSOLUTE PATH
     *
     * @param filePath - THE PATH NAME
     * @return - THE DATASET REPRESENTATION
     * @throws IOException
     */
    public TimeSeriesDataSet readFile(Path filePath, SecretKey key) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = null;

        try (
                InputStream fis = Files.newInputStream(filePath);
                InputStream decryptedIS = decryptIfNecessary(fis, key);
                InputStreamReader isReader = new InputStreamReader(decryptedIS, CSDB_FILE_CHAR_SET);
                BufferedReader bufR = new BufferedReader(isReader);
        ) {
            logEvent().path(filePath).info("generating Time series dataset from CSDB file");

            timeSeriesDataSet = parseCSDBFile(bufR);

            logEvent().path(filePath).info("time series dataset successfully generated from CSDB file");
            return timeSeriesDataSet;

        } catch (Exception e) {
            logEvent(e).path(filePath).error("error while attempting to parse CSDB file");
            throw e;
        }
    }


    private TimeSeriesDataSet parseCSDBFile(BufferedReader reader) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = new TimeSeriesDataSet();
        DataBlockParser dataBlockParser = new DataBlockParser();

        int index = 1; // lines in a file are indexed from 1...
        String line = null;

        while ((line = reader.readLine()) != null) {
            boolean blockCompleted = dataBlockParser.parseLine(line, index);
            if (blockCompleted) {
                dataBlockParser.complete(timeSeriesDataSet);

                dataBlockParser = new DataBlockParser();
                dataBlockParser.parseLine(line, index);
            }

            index++;
        }

        // flush any remaining entry
        if (dataBlockParser.isOpen() && !dataBlockParser.isComplete()) {
            dataBlockParser.complete(timeSeriesDataSet);
        }

        return timeSeriesDataSet;
    }

    private InputStream decryptIfNecessary(InputStream stream, SecretKey key) throws IOException {
        if (key == null) {
            logEvent().trace("encryption key null reading file with unencrypted stream");
            return stream;
        } else {
            logEvent().trace("encryption key not null reading file with crypto stream");
            return new Crypto().decrypt(stream, key);
        }
    }
}
