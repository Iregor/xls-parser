package com.store.demo;

import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Comparator;

public class CellRangeAddressComparator implements Comparator<CellRangeAddress> {
    //we firstly need to see merged regions by column
    //and then by rows
    //so we are going from left up to right bottom of category area

    @Override
    public int compare(CellRangeAddress o1, CellRangeAddress o2) {
        if (o1.getFirstColumn() - o2.getFirstColumn() != 0) {
            return o1.getFirstColumn() - o2.getFirstColumn();
        } else if (o1.getFirstRow() - o2.getFirstRow() != 0) {
            return o1.getFirstRow() - o2.getFirstRow();
        } else {
            return 0;
        }
    }
}
