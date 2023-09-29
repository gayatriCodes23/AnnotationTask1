package com.concerto.excel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.concerto.excel.bean.ExcelBean;
import com.concerto.excel.service.ExcelReader;

@RestController
public class UploadController {

	@PostMapping("/upload")
	public void upload(@RequestHeader String filePath, @RequestHeader int sheetIndex) {
		// This annotated method will trigger the Excel data reading logic
		List<Map<String, String>> excelData = ExcelReader.readExcelData(filePath, sheetIndex);
		
		System.out.println("\n");
		
		for (Map<String, String> row : excelData) {
			System.out.println(row);
		}
		
		System.out.println("\n");
		
		List<ExcelBean> bean = ExcelReader.mapDataToBeans(excelData);
		
		for (ExcelBean row : bean) {
			System.out.println(row);
		}
		
	}

}
