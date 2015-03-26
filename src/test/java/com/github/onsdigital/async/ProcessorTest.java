package com.github.onsdigital.async;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ProcessorTest {


    @Test
    public void shouldInitialiseWithFile() throws URISyntaxException {
        // Given
        // a text file
        assertNotNull("Test File Missing", getClass().getResource("/examples/CSDB"));
        URL resource = getClass().getResource("/examples/CSDB");
        Path filePath = Paths.get(resource.toURI());

        // When
        // we initialise with a file
        Processor processor = new Processor(filePath);

        // We expect
        // the file to be saved
        assertEquals(filePath, processor.getFile());
    }

}