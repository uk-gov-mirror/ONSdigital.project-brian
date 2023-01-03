package com.github.onsdigital.brian.exception;

/**
 *  Abstract exception class representing an exception withing Brian
 */
public abstract class BrianException extends Exception {
    /**
     *
     * Create a Brian exception with the supplied message
     *
     * @param msg  The message to be thrown
     */
    public BrianException(String msg) {
        super(msg);
    }

    /**
     * Returns the http status to return for the given exception
     *
     * @return  A valid http status code for the exception
     */
    public abstract int getHttpStatus();
}
