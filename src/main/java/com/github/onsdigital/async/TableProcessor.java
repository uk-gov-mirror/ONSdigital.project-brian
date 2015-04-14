package com.github.onsdigital.async;

import com.github.onsdigital.data.DataCube;
import com.github.onsdigital.readers.DataCubeReaderTable;
import com.github.onsdigital.readers.DataCubeReaderWDA;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by thomasridd on 31/03/15.
 */
public class TableProcessor {

    public static final ExecutorService pool = Executors.newFixedThreadPool(100);

    private Path file;

    public TableProcessor(Path file) {
        this.file = file;
    }

    public Future<DataCube> getCube() {
        Callable<DataCube> task = new Callable<DataCube>() {
            @Override
            public DataCube call() throws Exception {

                return DataCubeReaderTable.readFile(file);

            }
        };
        return pool.submit(task);
    }

    public static void shutdown() {
        // Shut down the pools
        TableProcessor.pool.shutdown();
    }
}
