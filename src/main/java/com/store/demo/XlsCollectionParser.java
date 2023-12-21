package com.store.demo;

import com.store.demo.models.Collection;
import com.store.demo.models.Model;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XlsCollectionParser {
    private final Map<String, Model> modelMap;
    private final Set<Collection> collectionSet;

    public XlsCollectionParser(ParsedData parsedData) {
        this.modelMap = parsedData.getModelMap();
        this.collectionSet = parsedData.getCollectionSet();
    }

    public void parseCollections(Path path) throws IOException {
        try (FileInputStream file = new FileInputStream(path.toFile())) {
            Workbook wb = new XSSFWorkbook(file);
            for (Sheet sheet : wb) {
                parseSheet(sheet);
            }
        }
        System.out.println("File parsed.");
    }

    private void parseSheet(Sheet sheet) {
        Map<Integer, Collection> collectionInColumn = new HashMap<>();
        parseCollectionNameRow(sheet.getRow(0), collectionInColumn);
        parseCollectionLinkRow(sheet.getRow(1), collectionInColumn);
        parseModelRows(sheet, collectionInColumn);
    }

    private void parseCollectionNameRow(Row row, Map<Integer, Collection> collectionInColumn) {
        for (Cell cell : row) {
            String data = cell.getStringCellValue();
            if (data.isBlank()) {       // break parsing if cell is blank
                break;
            }
            Collection collection = new Collection(null, data, null, new ArrayList<Model>());
            collectionSet.add(collection);
            collectionInColumn.put(cell.getColumnIndex(), collection);
        }
    }

    private void parseCollectionLinkRow(Row row, Map<Integer, Collection> collectionInColumn) {
        //if image link cell is empty - continue parsing
        for (Cell cell : row) {
            String data = cell.getStringCellValue();
            if (data.isBlank()) {
                continue;
            }
            Collection collection = collectionInColumn.get(cell.getColumnIndex());
            if (collection != null) {
                collection.setImageLink(data);
            } else {
                System.out.printf("Collection in column %d not exist.", cell.getColumnIndex());
            }
        }
    }

    private void parseModelRows(Sheet sheet, Map<Integer, Collection> collectionInColumn) {
        int lastRow = sheet.getLastRowNum();
        for (int i = 2; i < lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                break;
            }
            parseModelRow(row, collectionInColumn);
        }
    }

    private void parseModelRow(Row row, Map<Integer, Collection> collectionInColumn) {
        for (Cell cell : row) {
            String modelName = cell.getStringCellValue();
            if (!modelName.isBlank()) {
                Collection collection = collectionInColumn.get(cell.getColumnIndex());
                if (collection != null) {
                    collection.getModels().add(modelMap.get(modelName));
                }
            }
        }
    }
}
