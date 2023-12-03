package com.store.demo;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class XlsCategoryParser {
    public List<Category> getCategories(String filePath) {
        List<Category> categories = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(filePath)) {
            Workbook wb = new XSSFWorkbook(file);
            Sheet sheet = wb.getSheetAt(0);
            PriorityQueue<CellRangeAddress> cellAddressesQueue = getMergedRegionsInQueue(sheet);
            int lastCategoryRow = 2;    //we know it with accepted data structure
            int lastCategoryColumn = sheet.getRow(lastCategoryRow).getLastCellNum() - 1;
            parseCategories(sheet,
                    0, 0, lastCategoryRow, lastCategoryColumn,
                    cellAddressesQueue, null, categories);
            return categories;
        } catch (IOException exc) {
            System.out.println("Exception in reading file.");
        }
        return categories;
    }

    private PriorityQueue<CellRangeAddress> getMergedRegionsInQueue(Sheet sheet) {
        List<CellRangeAddress> cellAddressesList = sheet.getMergedRegions();
        PriorityQueue<CellRangeAddress> cellAddressesQueue = new PriorityQueue<>(new CellRangeAddressComparator());
        cellAddressesQueue.addAll(cellAddressesList);
        return cellAddressesQueue;
    }

    private void parseCategories(Sheet sheet,
                                        int firstRow,
                                        int firstColumn,
                                        int lastRow,
                                        int lastColumn,
                                        PriorityQueue<CellRangeAddress> addressesQueue,
                                        Category parentCategory,
                                        List<Category> categories) {
        //this method based on recursion algorithm firstly parsing down looking for childs,
        // and then parsing to the right looking for siblings
        if (firstRow > lastRow || firstColumn > lastColumn) {
            return; // here are recursion base occasion
        }
        System.out.println("Parsing: " + firstRow + " " + firstColumn); //to track parsing story
        Cell cell;
        Category category;
        String data;
        CellRangeAddress mergedRegion = addressesQueue.peek();
        if (mergedRegion != null && mergedRegion.getFirstRow() == firstRow && mergedRegion.getFirstColumn() == firstColumn) {
            mergedRegion = addressesQueue.poll();
        } else {
            mergedRegion = null;
        }
        int nextRowToParseBelow;
        int rightColumnToParseBelow;
        int leftColumnToParseHor;

        //firstly parse data as merged region or cell if region not found
        if (mergedRegion != null) {
            //parse as merged region
            int mergedRegionLastRow = mergedRegion.getLastRow();
            int mergedRegionLastColumn = mergedRegion.getLastColumn();
            cell = sheet.getRow(firstRow).getCell(firstColumn);
            data = cell.getStringCellValue();
            nextRowToParseBelow = mergedRegionLastRow + 1;
            rightColumnToParseBelow = mergedRegionLastColumn;
            leftColumnToParseHor = mergedRegionLastColumn + 1;
        } else {
            //region not found, now parse as cell
            cell = sheet.getRow(firstRow).getCell(firstColumn);
            data = cell.getStringCellValue();
            nextRowToParseBelow = firstRow + 1;
            rightColumnToParseBelow = firstColumn;
            leftColumnToParseHor = firstColumn + 1;
        }
        if (!data.isEmpty()) {
            category = new Category(data, parentCategory, new ArrayList<>());
            categories.add(category);
        } else {
            //if field is empty we skip it and go forth with previous parent category
            category = parentCategory;
        }
        //parse below for child categories
        parseCategories(sheet, nextRowToParseBelow, firstColumn, lastRow, rightColumnToParseBelow,
                addressesQueue, category, categories);

        //here we try to parse Attributes if we are dealing with last category row
        if (nextRowToParseBelow == 3) {
            parseAttributes(sheet, category, nextRowToParseBelow, firstColumn);
        }

        //now time to parse horizontally to the right for siblings
        parseCategories(sheet, firstRow, leftColumnToParseHor, lastRow, lastColumn,
                addressesQueue, parentCategory, categories);
    }

    private void parseAttributes(Sheet sheet, Category category, int firstRow, int column) {
        int lastRow = sheet.getLastRowNum();
        for (int rowNum = firstRow; rowNum <= lastRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                break;
            }
            Cell cell = row.getCell(column);
            String data;
            if (cell == null || (data = cell.getStringCellValue()).isEmpty()) {
                break;
            }
            Attribute attribute = new Attribute(data, cell.getRowIndex() - 2);
            category.getAttributes().add(attribute);
        }
    }
}
