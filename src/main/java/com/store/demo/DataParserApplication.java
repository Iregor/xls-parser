package com.store.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class DataParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataParserApplication.class, args);
        System.out.println("App has started. ");
        String filePath = "Категории и характеристики_v3.xlsx"; //file in working directory
        XlsCategoryParser parser = new XlsCategoryParser();
        List<Category> categories = parser.getCategories(filePath);
        for (Category category : categories) {
            System.out.println(category.getDescription());
        }
    }
}
