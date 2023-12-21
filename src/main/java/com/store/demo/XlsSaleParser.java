package com.store.demo;

import com.store.demo.models.Model;
import com.store.demo.models.Sale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class XlsSaleParser {
    private final Map<String, Model> modelMap;
    private final Map<String, Sale> saleMap;


    public XlsSaleParser(ParsedData parsedData) {
        this.modelMap = parsedData.getModelMap();
        this.saleMap = parsedData.getSaleMap();
    }

    public void parseSales(Path path) throws IOException {
        try (FileInputStream file = new FileInputStream(path.toFile())) {
            Workbook wb = new XSSFWorkbook(file);
            for (Sheet sheet : wb) {
                parseSheet(sheet);
            }
        }
        System.out.println("File parsed.");
    }

    private void parseSheet(Sheet sheet) {
        for (Row row : sheet) {
            if (row == null) {
                continue;
            }
            Cell cell = row.getCell(0);
            if (cell == null) {
                continue;
            }
            String modelName = cell.getStringCellValue();
            if (modelName == null || modelName.isBlank()) {
                continue;
            }
            cell = row.getCell(1);
            if (cell == null) {
                continue;
            }
            int percent = (int)cell.getNumericCellValue();
            Model model = modelMap.get(modelName);
            Sale sale = new Sale(null, model, percent);
            saleMap.put(modelName, sale);
        }
    }
}
