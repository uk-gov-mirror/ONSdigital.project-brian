package com.github.onsdigital.api;

import com.github.davidcarboni.restolino.framework.Home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Tom.Ridd on 08/03/15.
 */
public class HomePage implements Home {
    @Override
    public Object get(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        return("catnip");
    }
}
