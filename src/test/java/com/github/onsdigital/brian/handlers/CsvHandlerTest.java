package com.github.onsdigital.brian.handlers;

import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import org.apache.commons.fileupload.FileUploadException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CsvHandlerTest {

    @Mock
    private FileUploadHelper fileUploadHelper;

    @Mock
    private TimeSeriesConverter timeSeriesConverter;

    @Mock
    private SecretKey secretKey;

    @Mock
    private DataSetReader dataSetReader;

    @Mock
    private TimeSeries timeSeries;

    @Mock
    private Request request;

    @Mock
    private HttpServletRequest rawRequest;

    @Mock
    private Response response;

    private Route route;
    private Supplier<SecretKey> encryptionKeySupplier;
    private Path uploadFile;
    private List<TimeSeries> generatedTimeSeries;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        encryptionKeySupplier = () -> secretKey;

        route = new CsdbHandler(fileUploadHelper, timeSeriesConverter, encryptionKeySupplier, dataSetReader);
        uploadFile = Paths.get("/home/uploads/csv.csv");

        generatedTimeSeries = new ArrayList<>();
        generatedTimeSeries.add(timeSeries);
    }

    @Test
    public void shouldReturnTimeSeriesIfHandleSucessful() throws Exception {
        when(request.raw())
                .thenReturn(rawRequest);
        when(fileUploadHelper.getFileUploadPath(rawRequest, secretKey))
                .thenReturn(uploadFile);
        when(timeSeriesConverter.convert(uploadFile, dataSetReader, secretKey))
                .thenReturn(generatedTimeSeries);

        List<TimeSeries> result = (List<TimeSeries>) route.handle(request, response);

        assertThat(result, equalTo(generatedTimeSeries));

        verify(request, times(1)).raw();
        verify(fileUploadHelper, times(1)).getFileUploadPath(rawRequest, secretKey);
        verify(timeSeriesConverter, times(1)).convert(uploadFile, dataSetReader, secretKey);
    }

    @Test(expected = BadRequestException.class)
    public void shouldPropegateBadRequestExFromFileUploadHelper() throws Exception {
        when(request.raw())
                .thenReturn(rawRequest);
        when(fileUploadHelper.getFileUploadPath(rawRequest, secretKey))
                .thenThrow(new BadRequestException());

        try {
            route.handle(request, response);
        } catch (BadRequestException e) {
            verify(request, times(1)).raw();
            verify(fileUploadHelper, times(1)).getFileUploadPath(rawRequest, secretKey);
            verifyZeroInteractions(timeSeriesConverter);
            throw e;
        }
    }

    @Test(expected = IOException.class)
    public void shouldPropegateIOExFromFileUploadHelper() throws Exception {
        when(request.raw())
                .thenReturn(rawRequest);
        when(fileUploadHelper.getFileUploadPath(rawRequest, secretKey))
                .thenThrow(new IOException());

        try {
            route.handle(request, response);
        } catch (IOException e) {
            verify(request, times(1)).raw();
            verify(fileUploadHelper, times(1)).getFileUploadPath(rawRequest, secretKey);
            verifyZeroInteractions(timeSeriesConverter);
            throw e;
        }
    }

    @Test(expected = FileUploadException.class)
    public void shouldPropegateFileUploadExFromFileUploadHelper() throws Exception {
        when(request.raw())
                .thenReturn(rawRequest);
        when(fileUploadHelper.getFileUploadPath(rawRequest, secretKey))
                .thenThrow(new FileUploadException());

        try {
            route.handle(request, response);
        } catch (FileUploadException e) {
            verify(request, times(1)).raw();
            verify(fileUploadHelper, times(1)).getFileUploadPath(rawRequest, secretKey);
            verifyZeroInteractions(timeSeriesConverter);
            throw e;
        }
    }

    @Test(expected = IOException.class)
    public void shouldPropegateIOExFromService() throws Exception {
        when(request.raw())
                .thenReturn(rawRequest);
        when(fileUploadHelper.getFileUploadPath(rawRequest, secretKey))
                .thenReturn(uploadFile);
        when(timeSeriesConverter.convert(uploadFile, dataSetReader, secretKey))
                .thenThrow(new IOException());

        try {
            route.handle(request, response);
        } catch (IOException e) {
            verify(request, times(1)).raw();
            verify(fileUploadHelper, times(1)).getFileUploadPath(rawRequest, secretKey);
            verify(timeSeriesConverter, times(1)).convert(uploadFile, dataSetReader, secretKey);
            throw e;
        }
    }

    @Test(expected = BadRequestException.class)
    public void shouldPropegateBadRequestExFromService() throws Exception {
        when(request.raw())
                .thenReturn(rawRequest);
        when(fileUploadHelper.getFileUploadPath(rawRequest, secretKey))
                .thenReturn(uploadFile);
        when(timeSeriesConverter.convert(uploadFile, dataSetReader, secretKey))
                .thenThrow(new BadRequestException());

        try {
            route.handle(request, response);
        } catch (BadRequestException e) {
            verify(request, times(1)).raw();
            verify(fileUploadHelper, times(1)).getFileUploadPath(rawRequest, secretKey);
            verify(timeSeriesConverter, times(1)).convert(uploadFile, dataSetReader, secretKey);
            throw e;
        }
    }

}
