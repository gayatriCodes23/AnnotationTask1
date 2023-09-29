package com.concerto.excel.util;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.concerto.excel.annotation.ExcelColumn;
import com.concerto.excel.bean.ExcelBean;

public class ExcelReader {

	private static DataFormatter dataFormatter = new DataFormatter();
	private static CustomValidator lCustomValidator = new CustomValidator();

	public static List<Map<String, String>> readExcelData(String filePath, int sheetIndex) {
		List<Map<String, String>> excelData = new ArrayList<>();

		try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
				Workbook workbook = WorkbookFactory.create(fileInputStream)) {

			Sheet sheet = workbook.getSheetAt(sheetIndex);

			// Get the header row to validate column names
			Row headerRow = sheet.getRow(0);
			Map<String, Field> fieldMap = getFieldMap(ExcelBean.class);

			// Validate column names
			for (Cell cell : headerRow) {
				String columnName = cell.getStringCellValue().trim();
				Field field = fieldMap.get(columnName);

				if (field == null) {
					throw new IllegalArgumentException("Column name unavailable" + columnName);
				}

			}

			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext()) {
				rowIterator.next();
			}

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Map<String, String> rowData = new HashMap<>();

				for (Cell cell : row) {
					String columnName = dataFormatter.formatCellValue(sheet.getRow(0).getCell(cell.getColumnIndex()));
					String cellValue = dataFormatter.formatCellValue(cell);
					rowData.put(columnName, cellValue);
				}

				excelData.add(rowData);
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

	private static Map<String, Field> getFieldMap(Class<?> clazz) {
		Map<String, Field> fieldMap = new HashMap<>();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			if (field.isAnnotationPresent(ExcelColumn.class)) {
				ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
				String columnName = excelColumn.columnName();
				fieldMap.put(columnName, field);
			}
		}

		return fieldMap;
	}
	
	
/*//	?
	
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
	import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
	            
	            if (rowIterator.hasNext()) {
	                Row headerRow = rowIterator.next();
	                // Get the column names from the header row
	                List<String> columnNames = new ArrayList<>();
	                for (Cell headerCell : headerRow) {
	                	String columnName = dataFormatter.formatCellValue(headerCell);
//	                    if (columnName.isEmpty()) {
//	                        // Stop reading columns when an empty column header is encountered
//	                        break;
//	                    }
	                	if (!columnName.isEmpty()) {
	                        columnNames.add(columnName);
	                    }
	                }
	                
	                // Check if column names match ExcelBean fields
	                if (!areColumnNamesMatching(columnNames)) {
	                    throw new IllegalArgumentException("Column names in Excel do not match ExcelBean fields.");
	                }
	            }
	            
//	            if (rowIterator.hasNext()) {
//					rowIterator.next();
//				}

	            while (rowIterator.hasNext()) {
	                Row row = rowIterator.next();
	                Map<String, String> rowData = new HashMap<>();

	                for (Cell cell : row) {
	                    String columnName = dataFormatter.formatCellValue(sheet.getRow(0).getCell(cell.getColumnIndex()));
	                    String cellValue = dataFormatter.formatCellValue(cell);
	                    rowData.put(columnName, cellValue);
	                }
	                
	             // Check if all cells in this row are empty; if they are, skip this row
	                boolean isRowEmpty = rowData.values().stream().allMatch(String::isEmpty);
	                
	                if (!isRowEmpty) {
	                    excelData.add(rowData);
	                }

//	                excelData.add(rowData);
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
	
*/
}