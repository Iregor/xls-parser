package com.store.demo;

import com.store.demo.models.Category;
import com.store.demo.models.ParsedData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class DataParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataParserApplication.class, args);
        System.out.println("App has started. ");
        String filePath = "Категории и характеристики_v3.xlsx"; //file in working directory
        ParsedData parsedData = new ParsedData();
        XlsCategoryParser parser = new XlsCategoryParser(parsedData);
        List<Category> categories = parser.getCategories(filePath);
        for (Category category : categories) {
            System.out.println(category.getDescription());
        }
    }
}