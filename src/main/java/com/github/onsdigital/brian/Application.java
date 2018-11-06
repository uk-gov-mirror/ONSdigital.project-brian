package com.github.onsdigital.brian;

import com.github.davidcarboni.cryptolite.Keys;
import com.github.onsdigital.brian.configuration.Config;
import com.github.onsdigital.brian.exception.BadRequestException;
import com.github.onsdigital.brian.exception.handler.BadRequestExceptionHandler;
import com.github.onsdigital.brian.filter.AfterHandleFilter;
import com.github.onsdigital.brian.filter.BeforeHandleFilter;
import com.github.onsdigital.brian.handlers.CsdbHandler;
import com.github.onsdigital.brian.handlers.CsvHandler;
import com.github.onsdigital.brian.handlers.FileUploadHelper;
import com.github.onsdigital.brian.handlers.TimeSeriesConverter;
import com.github.onsdigital.brian.handlers.responses.JsonTransformer;
import com.github.onsdigital.brian.handlers.responses.Message;
import com.github.onsdigital.brian.readers.DataSetReader;
import com.github.onsdigital.brian.readers.DataSetReaderCSDB;
import spark.Route;

import javax.crypto.SecretKey;
import java.util.function.Supplier;

import static com.github.onsdigital.brian.configuration.Config.getConfig;
import static com.github.onsdigital.brian.logging.Logger.logEvent;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;

public class Application {

    public static void main(String[] args) throws Exception {
        createApp();
    }

    private static void createApp() throws Exception {
        logEvent().info("initialising project-brian");
        Config appConfig = getConfig();

        port(appConfig.getPort());

        before("/*", new BeforeHandleFilter());

        AfterHandleFilter afterHandleFilter = new AfterHandleFilter();
        after("/*", afterHandleFilter);

        exception(BadRequestException.class, new BadRequestExceptionHandler(afterHandleFilter));

        registerRoutes();

        logEvent().info("initialisation of project-brian API completed");
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
    }
}
