package com.store.demo;

import com.store.demo.exception.DataNotConsistentException;
import com.store.demo.models.*;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter
public class XlsModelParser {
    private final ParsedData parsedData;
    private final Map<String, Category> categoryMap;

    public XlsModelParser(ParsedData parsedData) {
        this.parsedData = parsedData;
        categoryMap = parsedData.getCategoryMap();
    }

    public void parseModels(Path path) throws IOException {
        try (FileInputStream file = new FileInputStream(path.toFile())) {
            Workbook wb = new XSSFWorkbook(file);
            for (Sheet sheet : wb) {
                parseSheet(sheet);
            }
        }
        System.out.println("File parsed.");
    }

    private void parseSheet(Sheet sheet) throws IOException {
        String categoryName = sheet.getSheetName().trim();
        Category category = categoryMap.get(categoryName);      //categoryMap contains category after category parsing
        if (category == null) {
            throw new DataNotConsistentException(String.format("Category \"%s\" doesn't exist.", categoryName));
        }
        HashMap<Integer, Model> modelInColumn = new HashMap<>();
        int parseRow = 0;
        parseRow = parseModelPart(sheet, modelInColumn);        //it is 3
        validateEmptyRow(sheet.getRow(parseRow++));
        parseRow = parseAttributePart(sheet, parseRow, category, modelInColumn);
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
                    for (int cellNum = 1; cellNum < row.getLastCellNum(); cellNum++) {
                        Model model = new Model();
                        String data = getCellValueAsString(row.getCell(cellNum));
                        if (data == null || data.isEmpty() || data.isBlank()) {
                            break;  //no empty columns should be presented
                        }
                        model.setName(data);
                        model.setCategory(categoryMap.get(sheetName));
                        modelInColumn.put(cellNum, model);
                        parsedData.getModelMap().put(model.getName(), model);
                    }
                }
                case 1 -> {
                    if (!titleCell.getStringCellValue().trim().equals("Описание")) {
                        throw new DataNotConsistentException(
                                String.format("Поле листа %s строки %d ячейки %d должно иметь содержимое \"Описание\".",
                                        sheetName, i, 0));
                    }
                    for (int cellNum = 1; cellNum < row.getLastCellNum() && cellNum <= modelInColumn.size(); cellNum++) {
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
                    for (int cellNum = 1; cellNum < row.getLastCellNum() && cellNum <= modelInColumn.size(); cellNum++) {
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
            String attributeName = attributeCell.getStringCellValue();
            Attribute attribute = parsedData.getAttributeMap().get(attributeName);
            if (attribute == null) {
                attribute = new Attribute(null, attributeName);
                parsedData.getAttributeMap().put(attribute.getName(), attribute);
            }
            CategoryAttribute categoryAttribute = new CategoryAttribute(null, category, attribute, parseRow - beginRow, "ONE_TYPE");
            parsedData.getCategoryAttributeSet().add(categoryAttribute);
            parseRowForModelAttributes(row, categoryAttribute, modelInColumn);
            parseRow++;
        }
        return parseRow;
    }

    private void parseRowForModelAttributes(Row row, CategoryAttribute categoryAttribute, HashMap<Integer, Model> modelInColumn) {
        for (int i = 1; i < row.getLastCellNum(); i++) {
            Model model = modelInColumn.get(i);
            Cell cell = row.getCell(i);
            String value = "";
            switch (cell.getCellType()) {
                case STRING -> value = cell.getStringCellValue();
                case NUMERIC -> value = String.valueOf((int)cell.getNumericCellValue());
            }
            if (value.isEmpty() || value.isBlank()) {
                continue;
            }
            ModelAttribute modelAttribute = new ModelAttribute(null, model, categoryAttribute, value);
            parsedData.getModelAttributeSet().add(modelAttribute);  //todo
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
        for (int i = 1; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String cellValue;
            if (cell == null || (cellValue = cell.getStringCellValue()).isEmpty() || cellValue.isBlank()) {
                continue;
            }
            Model model = modelInColumn.get(i);
            ModelImage modelImage = new ModelImage(null, model, cellValue);
            parsedData.getModelImageSet().add(modelImage);
        }
    }

    private void cellEquals(Cell cell, String value) throws DataNotConsistentException {
        if (!cell.getStringCellValue().trim().equalsIgnoreCase(value)) {
            throw new DataNotConsistentException(String.format("Cell %d in row %d must contains value \"%s\".", cell.getColumnIndex(), cell.getRowIndex(), value));
        }
    }

    private boolean notEmptyRow(Row row) {
        Cell firstCell = row.getCell(0);
        return firstCell != null && !firstCell.getStringCellValue().isEmpty() && !firstCell.getStringCellValue().isBlank();
    }

    private void validateEmptyRow(Row row) throws DataNotConsistentException {
        Cell firstCell = row.getCell(0);
        boolean isEmpty = firstCell == null || firstCell.getStringCellValue().isEmpty() || firstCell.getStringCellValue().isBlank();
        if (!isEmpty) {                //  !!! implement right logic
            throw new DataNotConsistentException(String.format("Line %d not empty.", row.getRowNum()));
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case STRING -> cell.getStringCellValue();
            default -> "";
        };
    }
}