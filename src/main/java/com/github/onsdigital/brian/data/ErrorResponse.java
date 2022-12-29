package com.github.onsdigital.brian.data;

import com.github.onsdigital.brian.exception.BrianException;

/**
 * Data Tranfer Object representing the body of an error response
 */
public class ErrorResponse {
    private int code;
    private String description;

    public ErrorResponse(int code, String description) {
        setCode(code);
        setDescription(description);
    }

    public ErrorResponse(BrianException be) {
        this(be.getHttpStatus(),be.getMessage());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
