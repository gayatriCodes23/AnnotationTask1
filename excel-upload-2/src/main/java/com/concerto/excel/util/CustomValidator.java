package com.concerto.excel.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.concerto.excel.bean.ExcelBean;

@Component
public class CustomValidator {

	public String dateConvertor(ExcelBean value) {
        if (value.getBirthdate() != null) {
            String inputDate = value.getBirthdate();
            String inputDateFormat = "dd/MM/yyyy"; // Specify the expected date format

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(inputDateFormat);
                inputFormat.setLenient(false);
                Date date = inputFormat.parse(inputDate);

                // Format the parsed Date object to "dd-MM-yyyy"
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
                return outputFormat.format(date);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid Birthdate: " + inputDate);
            }
        }
        return null; // Handle cases where the birthdate is null
    }
}
