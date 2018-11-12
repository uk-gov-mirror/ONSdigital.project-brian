package com.github.onsdigital.brian.handlers.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {
    private static final JsonTransformer instance = new JsonTransformer();

    private Gson gson;

    private JsonTransformer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        //this.gson = new Gson();
    }

    @Override
    public String render(Object o) throws Exception {
        return gson.toJson(o);
    }

    public static JsonTransformer getInstance() {
        return instance;
    }
}
