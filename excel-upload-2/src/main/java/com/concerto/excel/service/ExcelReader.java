package com.concerto.excel.service;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.concerto.excel.annotation.ExcelColumn;
import com.concerto.excel.bean.ExcelBean;
import com.concerto.excel.util.CustomValidator;

public class ExcelReader {

	private static DataFormatter dataFormatter = new DataFormatter();
	private static CustomValidator lCustomValidator = new CustomValidator();

	public static List<Map<String, String>> readExcelData(String filePath, int sheetIndex) {
        List<Map<String, String>> excelData = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(sheetIndex); // Assuming you're working with the first sheet

            Iterator<Row> rowIterator = sheet.iterator();
            List<String> columnNames = null;
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                // Get the column names from the header row, ignoring empty header cells
                 columnNames = new ArrayList<>();
                for (Cell headerCell : headerRow) {
                    String columnName = dataFormatter.formatCellValue(headerCell);
                    if (!columnName.isEmpty()) {
                        columnNames.add(columnName);
                    }
                }
                
                // Check if column names match ExcelBean fields
                if (!areColumnNamesMatching(columnNames)) {
                    throw new IllegalArgumentException("Column names in Excel do not match ExcelBean fields.");
                }
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowData = new HashMap<>();

                for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                    Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String columnName = columnNames.get(columnIndex);
                    String cellValue = dataFormatter.formatCellValue(cell);
                    // Only add cell values to rowData if they are not empty or null
                    if (!cellValue.isEmpty()) {
                        rowData.put(columnName, cellValue);
                    }
                }

                // Check if rowData has any non-empty values before adding it to excelData
                if (!rowData.isEmpty()) {
                    excelData.add(rowData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return excelData;
    }

	public static List<ExcelBean> mapDataToBeans(List<Map<String, String>> excelData) {
		List<ExcelBean> dataList = new ArrayList<>();

		for (Map<String, String> row : excelData) {
			ExcelBean dataObject = new ExcelBean();

			for (Field field : ExcelBean.class.getDeclaredFields()) {
				if (field.isAnnotationPresent(ExcelColumn.class)) {
					ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
					String columnName = annotation.columnName();
					String cellValue = row.get(columnName);
					if (cellValue != null) {
						field.setAccessible(true);
						try {
							field.set(dataObject, cellValue);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}

			String date = lCustomValidator.dateConvertor(dataObject);
			dataObject.setBirthdate(date);
			dataList.add(dataObject);
		}

		return dataList;
	}

	private static boolean areColumnNamesMatching(List<String> columnNames) {
        // Get the field names from ExcelBean class and compare with column names
        Field[] fields = ExcelBean.class.getDeclaredFields();
        List<String> fieldNames = Arrays.stream(fields)
            .filter(field -> field.isAnnotationPresent(ExcelColumn.class))
            .map(field -> field.getAnnotation(ExcelColumn.class).columnName())
            .collect(Collectors.toList());

        return columnNames.containsAll(fieldNames);
    }

}