package com.store.demo.models;

import java.io.IOException;

public class DataNotConsistentException extends IOException {

    public DataNotConsistentException() {
    }

    public DataNotConsistentException(String message) {
        super(message);
    }
}
