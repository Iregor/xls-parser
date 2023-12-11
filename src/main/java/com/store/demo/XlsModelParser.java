package com.store.demo;

import com.store.demo.models.*;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class XlsModelParser {
    private final ParsedData parsedData;
    private final Map<String, Category> categoryMap;


    public XlsModelParser(ParsedData parsedData) {
        this.parsedData = parsedData;

        categoryMap = new HashMap<>();
        parsedData.getCategorySet().forEach(category -> categoryMap.put(category.getName(), category));
    }

    public void parseXls(String filename) throws IOException {
        try (FileInputStream file = new FileInputStream(filename)) {
            Workbook wb = new XSSFWorkbook(file);
            for (Sheet sheet : wb) {
                parseSheet(sheet);
            }
        }
    }

    private void parseSheet(Sheet sheet) throws IOException {
        String categoryName = sheet.getSheetName();
        Category category = categoryMap.get(categoryName);      //categoryMap contains category after category parsing
        if (category == null) {
            throw new DataNotConsistentException(String.format("Category \"%s\" doesn't exist.", categoryName));
        }
        HashMap<Integer, Model> modelInColumn = new HashMap<>();
        int parseRow = 0;
        parseRow = parseModelPart(sheet, new HashMap<>());
        validateEmptyRow(sheet.getRow(parseRow++));
        parseRow = parseAttributePart(sheet, parseRow + 1, category, modelInColumn);
        validateEmptyRow(sheet.getRow(parseRow++));
        parseImagePart(sheet, parseRow, modelInColumn);
    }

    private int parseModelPart(Sheet sheet, Map<Integer, Model> modelInColumn) throws DataNotConsistentException {
        String sheetName = sheet.getSheetName();
        for (int i = 0; i <= 2; i++) {  //iterate over rows
            Row row = sheet.getRow(i);
            Cell titleCell = row.getCell(0);
            switch (i) {
                case 0 -> {
                    //model name row
                    if (!titleCell.getStringCellValue().trim().equals("Модель")) {
                        throw new DataNotConsistentException(
                                String.format("Поле листа %s строки %d ячейки %d должно иметь содержимое \"Модель\".",
                                        sheetName, i, 0));
                    }
                    for (int cellNum = 1; cellNum <= row.getLastCellNum(); cellNum++) {
                        Model model = new Model();
                        model.setName(row.getCell(cellNum).getStringCellValue());
                        modelInColumn.put(cellNum, model);
                        parsedData.getModelSet().add(model);
                    }
                }
                case 1 -> {
                    if (!titleCell.getStringCellValue().trim().equals("Описание")) {
                        throw new DataNotConsistentException(
                                String.format("Поле листа %s строки %d ячейки %d должно иметь содержимое \"Описание\".",
                                        sheetName, i, 0));
                    }
                    for (int cellNum = 1; cellNum <= row.getLastCellNum(); cellNum++) {
                        Model model = modelInColumn.get(cellNum);
                        model.setDescription(row.getCell(cellNum).getStringCellValue());
                    }
                }
                case 2 -> {
                    if (!titleCell.getStringCellValue().trim().equals("Цена")) {
                        throw new DataNotConsistentException(
                                String.format("Поле листа %s строки %d ячейки %d должно иметь содержимое \"Цена\".",
                                        sheetName, i, 0));
                    }
                    for (int cellNum = 1; cellNum <= row.getLastCellNum(); cellNum++) {
                        Model model = modelInColumn.get(cellNum);
                        model.setPrice((long) row.getCell(cellNum).getNumericCellValue());
                    }
                }
            }
        }
        return 3;
    }

    private int parseAttributePart(Sheet sheet, int beginRow, Category category, HashMap<Integer, Model> modelInColumn) {
        int parseRow = beginRow;
        Row row;
        while (notEmptyRow(row = sheet.getRow(parseRow))) {
            Cell attributeCell = row.getCell(0);
            Attribute attribute = new Attribute(attributeCell.getStringCellValue());
            parsedData.getAttributeSet().add(attribute);
            CategoryAttribute categoryAttribute = new CategoryAttribute(category, attribute, parseRow - beginRow);
            parsedData.getCategoryAttributeSet().add(categoryAttribute);
            parseRowForModelAttributes(row, categoryAttribute, modelInColumn);
            parseRow++;
        }
        return parseRow;
    }

    private void parseRowForModelAttributes(Row row, CategoryAttribute categoryAttribute, HashMap<Integer, Model> modelInColumn) {
        for (int i = 1; i <= row.getLastCellNum(); i++) {
            Model model = modelInColumn.get(i);
            String value = row.getCell(i).getStringCellValue();
            if (value.isEmpty() || value.isBlank()) {
                continue;
            }
            ModelAttribute modelAttribute = new ModelAttribute(model, categoryAttribute, value);
            parsedData.getModelAttributeSet().add(modelAttribute);
        }
    }

    private int parseImagePart(Sheet sheet, int parseRow, HashMap<Integer, Model> modelInColumn) throws DataNotConsistentException {
        cellEquals(sheet.getRow(parseRow).getCell(0), "images");
        int lastRow = sheet.getLastRowNum();
        Row row;
        while (parseRow <= lastRow) {
            row = sheet.getRow(parseRow);
            parseRowForModelImages(row, modelInColumn);
            parseRow++;
        }
        return parseRow;
    }

    private void parseRowForModelImages(Row row, HashMap<Integer, Model> modelInColumn) {
        for (int i = 1; i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String cellValue;
            if (cell == null || (cellValue = cell.getStringCellValue()).isEmpty() || cellValue.isBlank()) {
                continue;
            }
            Model model = modelInColumn.get(i);
            ModelImage modelImage = new ModelImage(model, cellValue);
            parsedData.getModelImageSet().add(modelImage);
        }
    }

    private void cellEquals(Cell cell, String value) throws DataNotConsistentException {
        if (!cell.getStringCellValue().trim().equalsIgnoreCase(value)) {
            throw new DataNotConsistentException(String.format("Cell %d in row %d must contains value \"%s\"."));
        }
    }

    private boolean notEmptyRow(Row row) {
        Cell firstCell = row.getCell(0);
        return firstCell == null || firstCell.getStringCellValue().isEmpty() || firstCell.getStringCellValue().isBlank();
    }

    private void validateEmptyRow(Row row) throws DataNotConsistentException {
        System.out.println("Last cell: " + row.getLastCellNum());
        if (false) {                //  !!! implement right logic
            throw new DataNotConsistentException(String.format("Line %d not empty.", row.getRowNum()));
        }
    }
}