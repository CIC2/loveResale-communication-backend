package com.resale.loveresalecommunication.utils;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelSheetUtils {
    public List<String> extractPhoneNumbersFromExcel(MultipartFile file) throws Exception {
        List<String> numbers = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell cell = row.getCell(0); // first column only

                if (cell != null) {
                    String value = getCellValueAsString(cell).trim();
                    if (!value.isEmpty()) {
                        numbers.add(value);
                    }
                }
            }
        }

        return numbers;
    }

    public List<String> extractEmailsFromExcel(MultipartFile file) throws Exception {
        List<String> emails = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell cell = row.getCell(0);

                if (cell != null) {
                    String value = getCellValueAsString(cell).trim();
                    if (!value.isEmpty() && value.contains("@")) {
                        emails.add(value);
                    }
                }
            }
        }

        return emails;
    }

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(((Double) cell.getNumericCellValue()).longValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}


