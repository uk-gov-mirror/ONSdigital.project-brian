package com.github.onsdigital.brian;

import com.github.davidcarboni.cryptolite.Keys;
import com.github.onsdigital.brian.configuration.Config;
import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.exception.handler.BadRequestExceptionHandler;
import com.github.onsdigital.brian.exception.handler.CatchAllExceptionHandler;
import com.github.onsdigital.brian.exception.handler.DataBlockExceptionHandler;
import com.github.onsdigital.brian.exception.handler.InternalServerErrorHandler;
import com.github.onsdigital.brian.filter.RequestCompleteFilter;
import com.github.onsdigital.brian.filter.RequestReceivedFilter;
import com.github.onsdigital.brian.handlers.CsdbHandler;
import com.github.onsdigital.brian.handlers.CsvHandler;
import com.github.onsdigital.brian.handlers.FileUploadHelper;
import com.github.onsdigital.brian.handlers.TimeSeriesConverter;
import com.github.onsdigital.brian.handlers.responses.JsonTransformer;
import com.github.onsdigital.brian.handlers.responses.Message;
import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.brian.readers.csdb.DataBlockException;
import com.github.onsdigital.brian.readers.csdb.DataSetReaderCSDB;
import com.github.onsdigital.logging.v2.DPLogger;
import com.github.onsdigital.logging.v2.Logger;
import com.github.onsdigital.logging.v2.LoggerImpl;
import com.github.onsdigital.logging.v2.LoggingException;
import com.github.onsdigital.logging.v2.config.Builder;
import com.github.onsdigital.logging.v2.serializer.JacksonLogSerialiser;
import com.github.onsdigital.logging.v2.serializer.LogSerialiser;
import com.github.onsdigital.logging.v2.storage.LogStore;
import com.github.onsdigital.logging.v2.storage.MDCLogStore;
import spark.Route;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.function.Supplier;

import static com.github.onsdigital.brian.configuration.Config.getConfig;
import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

/**
 * The main class - start all the things.
 */
public class Application {

    public static void main(String[] args) throws Exception {
        createApp();
    }

    /**
     * The root class of the application. Defines the request filters, exception handlers, API routes etc.
     */
    private static void createApp() throws Exception {
        initLogging();

        Config appConfig = getConfig();
        info().data("configuration", appConfig.get()).log("initialising project-brian");

        port(appConfig.getPort());

        before("/*", new RequestReceivedFilter());

        RequestCompleteFilter requestCompleteFilter = new RequestCompleteFilter();
        after("/*", requestCompleteFilter);

        exception(BadRequestException.class, new BadRequestExceptionHandler(requestCompleteFilter));
        exception(DataBlockException.class, new DataBlockExceptionHandler(requestCompleteFilter));
        exception(Exception.class, new CatchAllExceptionHandler());

/*        internalServerError((request, response) -> new InternalServerErrorHandler(requestCompleteFilter)
                .handle(request, response));*/

        registerRoutes();

        info().log("initialisation of project-brian API completed");
    }

    private static void registerRoutes() {
        TimeSeriesConverter timeSeriesConverter = new TimeSeriesConverter();
        FileUploadHelper fileUploadHelper = new FileUploadHelper();

        Supplier<SecretKey> encryptionKeySupplier = () -> Keys.newSecretKey();
        DataSetReader csdbReader = new DataSetReaderCSDB();
        Route csdbRoute = new CsdbHandler(fileUploadHelper, timeSeriesConverter, encryptionKeySupplier, csdbReader);
        Route csvRoute = new CsvHandler(fileUploadHelper, timeSeriesConverter, encryptionKeySupplier, csdbReader);

        JsonTransformer transformer = JsonTransformer.getInstance();

        // API routes
        get("/healthcheck", (req, resp) -> new Message("Healthcheck response"), transformer);

        path("/Services", () -> {
            post("/ConvertCSDB", (req, resp) -> csdbRoute.handle(req, resp), transformer);
            post("/ConvertCSV", (req, resp) -> csvRoute.handle(req, resp), transformer);
        });

        info().data("routes", new HashMap() {{
            put("/Services/ConvertCSDB", "POST");
            put("/Services/ConvertCSV", "POST");
            put("/healthcheck", "GET");
        }}).log("registered API routes");
    }

    private static void initLogging() {
        LogSerialiser serialiser = new JacksonLogSerialiser(true);
        LogStore store = new MDCLogStore(serialiser);
        Logger logger = new LoggerImpl("project-brian");

        try {
            DPLogger.init(new Builder()
                    .serialiser(serialiser)
                    .logStore(store)
                    .logger(logger)
                    .create());
        } catch (LoggingException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }
}
