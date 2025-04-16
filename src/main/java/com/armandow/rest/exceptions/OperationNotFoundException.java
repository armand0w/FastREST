package com.armandow.rest.exceptions;

import com.armandow.rest.interfaces.IHTTPStatusNotifiable;
import lombok.Getter;

import java.io.Serial;

@Getter
public class OperationNotFoundException extends Exception implements IHTTPStatusNotifiable {
    @Serial
    private static final long serialVersionUID = -2026030435893464724L;
    private final transient Object data;

    public OperationNotFoundException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public OperationNotFoundException(String message) {
        super(message);
        this.data = null;
    }
    
    public Integer getHTTPStatus() {
        return 405;
    }
}
