package com.store.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataParserApplication.class, args);
        XlsParser parser = new XlsParser();
        parser.start();
    }
}