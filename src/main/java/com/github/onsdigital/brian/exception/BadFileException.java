package com.github.onsdigital.brian.exception;

import org.eclipse.jetty.http.HttpStatus;

/**
 * An exception to be thrown when the uploaded file is invalid
 */
public class BadFileException extends BrianException {

    /***
     * Construct an exception when the uploaded file is invalid
     *
     * @param msg  A message explaining why the file is invalid
     */
    public BadFileException(String msg){
        super(msg);
    }

    /**
     * A http status explaining the uploaded file was bad
     *
     * @return  HTTP status 400 (Bad request)
     */
    @Override
    public int getHttpStatus() {
        return HttpStatus.BAD_REQUEST_400;
    }
}
