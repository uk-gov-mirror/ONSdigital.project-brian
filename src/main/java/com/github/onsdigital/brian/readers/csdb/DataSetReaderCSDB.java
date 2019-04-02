package com.github.onsdigital.brian.readers.csdb;

import com.github.davidcarboni.cryptolite.Crypto;
import com.github.onsdigital.brian.data.TimeSeriesDataSet;
import com.github.onsdigital.brian.readers.DataSetReader;
import org.apache.commons.io.FileUtils;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Parse a <i>.csdb</i> file and convert it to a {@link TimeSeriesDataSet}
 */
public class DataSetReaderCSDB implements DataSetReader {

    private static final Pattern p = Pattern.compile("^[0-9]{2}$");
    private static final String CSDB_FILE_CHAR_SET = "cp1252";


    /**
     * Parse a <i>.csdb</i> file and convert it to a {@link TimeSeriesDataSet}
     *
     * @param filePath the path to the <i>.csdb</i> file to parse.
     * @param key      the {@link SecretKey} to use to decrypt the file.
     */
    public TimeSeriesDataSet readFile(Path filePath, SecretKey key) throws IOException {
        TimeSeriesDataSet timeSeriesDataSet = null;

        try (
                InputStream fis = Files.newInputStream(filePath);
                InputStream decryptedIS = decryptIfNecessary(fis, key);
                InputStreamReader isReader = new InputStreamReader(decryptedIS, CSDB_FILE_CHAR_SET);
                BufferedReader bufR = new BufferedReader(isReader);
        ) {
            info().data("file_path", filePath.toString()).log("generating time series dataset from CSDB file");

            timeSeriesDataSet = generateTimeSeriesDataSet(bufR);

            info().data("file_path", filePath.toString())
                    .data("name", timeSeriesDataSet.name)
                    .data("file_size", FileUtils.byteCountToDisplaySize(filePath.toFile().length()))
                    .log("time series dataset successfully generated from CSDB file");

            return timeSeriesDataSet;

        } catch (DataBlockException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException("error while attempting to parse CSDB file", e);
        }
    }


    private TimeSeriesDataSet generateTimeSeriesDataSet(BufferedReader reader) throws IOException {
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

        dataBlockParser.flush(timeSeriesDataSet);
        return timeSeriesDataSet;
    }

    private InputStream decryptIfNecessary(InputStream stream, SecretKey key) throws IOException {
        if (key == null) {
            info().log("encryption key null reading file with unencrypted stream");
            return stream;
        } else {
            info().log("encryption key not null reading file with crypto stream");
            return new Crypto().decrypt(stream, key);
        }
    }
}
