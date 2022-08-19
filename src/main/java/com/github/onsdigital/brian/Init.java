package com.github.onsdigital.brian;

import com.github.davidcarboni.restolino.framework.Startup;
import com.github.onsdigital.logging.v2.DPLogger;
import com.github.onsdigital.logging.v2.Logger;
import com.github.onsdigital.logging.v2.LoggerImpl;
import com.github.onsdigital.logging.v2.LoggingException;
import com.github.onsdigital.logging.v2.config.Builder;
import com.github.onsdigital.logging.v2.serializer.JacksonLogSerialiser;
import com.github.onsdigital.logging.v2.serializer.LogSerialiser;
import com.github.onsdigital.logging.v2.storage.LogStore;
import com.github.onsdigital.logging.v2.storage.MDCLogStore;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

public class Init implements Startup {

    public static final String APP_NAMESPACE = "project-brian";

    @Override
    public void init() {
        LogSerialiser serialiser = new JacksonLogSerialiser();
        LogStore store = new MDCLogStore(serialiser);
        Logger logger = new LoggerImpl(APP_NAMESPACE);

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

        info().log("starting application "+APP_NAMESPACE+" initialisation");

    }
}
