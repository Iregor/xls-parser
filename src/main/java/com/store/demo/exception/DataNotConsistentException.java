package com.store.demo.exception;

import java.io.IOException;

public class DataNotConsistentException extends IOException {

    public DataNotConsistentException() {
    }

    public DataNotConsistentException(String message) {
        super(message);
    }
}
