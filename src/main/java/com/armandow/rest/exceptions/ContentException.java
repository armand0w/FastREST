package com.armandow.rest.exceptions;

import com.armandow.rest.interfaces.IHTTPStatusNotifiable;
import lombok.Getter;

import java.io.Serial;

@Getter
public class ContentException extends Exception implements IHTTPStatusNotifiable {
    @Serial
    private static final long serialVersionUID = 8197739982128153563L;
    private final transient Object data;

    public ContentException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public ContentException(String message) {
        super(message);
        this.data = null;
    }

    public Integer getHTTPStatus() {
        return 400;
    }

}
