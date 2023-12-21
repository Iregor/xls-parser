package com.store.demo;

import com.store.demo.models.Category;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class XlsCategoryParser {
    private final Map<String, Category> categoryMap;

    public XlsCategoryParser(ParsedData parsedData) {
        this.categoryMap = parsedData.getCategoryMap();
    }

    public void parseCategoriesData(Path path) throws IOException {
        try (FileInputStream file = new FileInputStream(path.toFile())) {
            Workbook wb = new XSSFWorkbook(file);
            Sheet categoryHierarchySheet = wb.getSheetAt(0);
            Sheet categoryImagesSheet = wb.getSheetAt(1);
            parseCategoryHierarchySheet(categoryHierarchySheet);
            parseCategoryImagesSheet(categoryImagesSheet);
            System.out.println("File parsed.");
        }
    }

    private void parseCategoryHierarchySheet(Sheet sheet) {
        PriorityQueue<CellRangeAddress> cellAddressesQueue = getMergedRegionsInQueue(sheet);
        int lastCategoryRow = 2;    //we know it with accepted data structure
        int lastCategoryColumn = sheet.getRow(lastCategoryRow).getLastCellNum() - 1;
        parseCategories(sheet,
                0, 0, lastCategoryRow, lastCategoryColumn,
                cellAddressesQueue, null);
    }

    private void parseCategoryImagesSheet(Sheet sheet) {
        //parsing would break if empty category name row arise
        for (Row row : sheet) {
            Cell categoryNameCell = row.getCell(0);
            if (categoryNameCell == null) {
                break;
            }
            String categoryName = categoryNameCell.getStringCellValue();
            if (categoryName.isBlank()) {
                break;
            }

            Cell linkCell = row.getCell(1);
            if (linkCell == null) {
                break;
            }
            String imageLink = linkCell.getStringCellValue();
            Category category = categoryMap.get(categoryName);
            if (category == null) {
                System.out.printf("Category %s not found while parsing category image links.%n");
            } else {
                category.setImageLink(imageLink);
            }
        }
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
                                 Category parentCategory) {
        //this method based on recursion algorithm firstly parsing down looking for childs,
        // and then parsing to the right looking for siblings
        if (firstRow > lastRow || firstColumn > lastColumn) {
            return; // here are recursion base occasion
        }
//        System.out.println("Parsing: " + firstRow + " " + firstColumn + " with parent: " + parentCategory); //to track parsing story
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
            data = cell.getStringCellValue().trim();
            nextRowToParseBelow = firstRow + 1;
            rightColumnToParseBelow = firstColumn;
            leftColumnToParseHor = firstColumn + 1;
        }
        if (!data.isEmpty()) {
            category = new Category(null, data, parentCategory, null);
            categoryMap.put(category.getName(), category);
        } else {
            //if field is empty we skip it and go forth with previous parent category
            category = parentCategory;
        }
        //parse below for child categories
        parseCategories(sheet, nextRowToParseBelow, firstColumn, lastRow, rightColumnToParseBelow,
                addressesQueue, category);

        //now time to parse horizontally to the right for siblings
        parseCategories(sheet, firstRow, leftColumnToParseHor, lastRow, lastColumn,
                addressesQueue, parentCategory);
    }
}