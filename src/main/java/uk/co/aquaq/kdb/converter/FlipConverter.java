package uk.co.aquaq.kdb.converter;

import kx.c;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlipConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlipConverter.class);

    public List<Map<String, Object>> convertFlipToRecordList(c.Flip flip){
        String columnHeaders = FlipConverter.getColumnNames(flip);
        List recordList=FlipConverter.getDataRows(flip);
        String[] parsedColumnHeaders = columnHeaders.split("[,]");

        return populateRecordList(parsedColumnHeaders,recordList, flip);
    }

    private  List<Map<String, Object>> populateRecordList(String[] columnHeaders, List<Object> records, c.Flip flip){
        List<Map<String, Object>> recordList = new ArrayList<>();
        List<Object[]> tableDataList = FlipConverter.convertToDataArrays(flip);
        for(int rowPosition = 0; rowPosition < records.size(); rowPosition++) {
            Object[] dataRow = tableDataList.get(rowPosition);
            Map<String,Object> tableRecords= createTableRecordMap(columnHeaders, dataRow);
            recordList.add(rowPosition,tableRecords);
        }

        return recordList;
    }

    private  Map<String, Object> createTableRecordMap(String[] columnHeaders, Object[] dataRow) {
        Map<String, Object> tableRecords = new HashMap<>();
        for(int elementPosition = 0; elementPosition < columnHeaders.length; elementPosition++){
            tableRecords.put(columnHeaders[elementPosition], dataRow[elementPosition]);
        }

        return tableRecords;
    }

    public static List<Object[]> convertToDataArrays(c.Flip flip) {
        List<Object[]> dataObjectArrays = new ArrayList<>();
        if (flip != null) {
            int numberOfColumns = flip.x.length;
            try {
                addRowsToDataObjects(flip, dataObjectArrays, numberOfColumns);
            } catch (UnsupportedEncodingException uee) {
                String message = String.format("UnsupportedEncodingException occurred, named character set is not supported. Message: %s", uee.getMessage());
                LOGGER.warn(message);
            }
        }

        return dataObjectArrays;
    }

    private static void addRowsToDataObjects(c.Flip flip, List<Object[]> dataArrays, int numberOfColumns) throws UnsupportedEncodingException {
        for (int row = 0; row < c.n(flip.y[0]); row++) {
            Object[] dataRowArray = new Object[numberOfColumns];
            for (int columnCount= 0; columnCount < numberOfColumns; columnCount++) {
                dataRowArray[columnCount] = c.at(flip.y[columnCount], row);
            }
            dataArrays.add(dataRowArray);
        }
    }

    private static String getColumnNames(c.Flip flip) {
        StringBuilder columnNamesStringBuilder = new StringBuilder();
        int numberOfColumns = flip.x.length;
        for (int columnCount = 0; columnCount < numberOfColumns; columnCount++) {
            columnNamesStringBuilder.append(columnCount > 0 ? "," : "").append(flip.x[columnCount]);
        }

        return columnNamesStringBuilder.toString();
    }

    private static List<String> getDataRows(c.Flip flip) {
        List<String> dataRows = new ArrayList<>();
        try {
            int numberOfRows = c.n(flip.y[0]);
            for (int rowCount = 0; rowCount < numberOfRows; rowCount++) {
                dataRows.add(getDataRowValues(flip, rowCount));
            }
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            String message = String.format("UnsupportedEncodingException occurred, named character set is not supported. Message: %s", unsupportedEncodingException.getMessage());
            LOGGER.warn(message);
        }

        return dataRows;
    }

    private static String getDataRowValues(c.Flip flip, int row) {
        StringBuilder rowStringBuilder = new StringBuilder();
        int numberOfColumns = flip.x.length;
        for (int columnCount = 0; columnCount < numberOfColumns; columnCount++) {
            rowStringBuilder.append(columnCount > 0 ? "," : "").append(c.at(flip.y[columnCount], row));
        }

        return rowStringBuilder.toString();
    }

}
