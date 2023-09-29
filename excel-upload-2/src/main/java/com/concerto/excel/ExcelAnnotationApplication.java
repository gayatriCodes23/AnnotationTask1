package com.concerto.excel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExcelAnnotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExcelAnnotationApplication.class, args);
	}
	
//	@Bean
//    public CommandLineRunner processExcelData(ExcelService excelService) {
//        return args -> {
//            try {
//                List<Map<String, String>> excelData = excelService.readExcelData("D:\\SampleExcelData\\ExcelBean.xlsx", 0);
//                // Process the excelData as needed
//                // Now, map the data to your beans using ExcelColumn annotations
//                List<ExcelBean> dataList = excelService.mapDataToBeans(excelData);
//                // Process the dataList as needed
//                for (ExcelBean data : dataList) {
//                    System.out.println(data.toString());
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        };
//    }

}
