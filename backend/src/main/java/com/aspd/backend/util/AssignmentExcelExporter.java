package com.aspd.backend.util;

import com.aspd.backend.model.InternshipAssignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting internship assignments to Excel format.
 */
public class AssignmentExcelExporter {

    public static byte[] exportAssignmentsToExcel(List<InternshipAssignment> assignments) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Internship Assignments");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderCellStyle(workbook);
            
            String[] headers = {
                "No.",
                "Student Name",
                "Student Matriculation",
                "Student Email",
                "Student School Type",
                "Praktikum Type",
                "Course",
                "Teacher Name",
                "Teacher Email",
                "School Name",
                "Status",
                "School Year"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Populate data rows
            int rowNum = 1;
            for (InternshipAssignment assignment : assignments) {
                Row dataRow = sheet.createRow(rowNum++);
                
                // Add counter starting from 1
                dataRow.createCell(0).setCellValue(rowNum - 1);
                
                String studentName = assignment.getStudentConfig().getStudent().getFirstName() + " " +
                        assignment.getStudentConfig().getStudent().getLastName();
                dataRow.createCell(1).setCellValue(studentName);
                
                dataRow.createCell(2).setCellValue(assignment.getStudentConfig().getStudent().getMatriculationNbr());
                dataRow.createCell(3).setCellValue(assignment.getStudentConfig().getStudent().getEmail());
                
                if (assignment.getStudentConfig().getSchoolType() != null) {
                    dataRow.createCell(4).setCellValue(assignment.getStudentConfig().getSchoolType().toString());
                } else {
                    dataRow.createCell(4).setCellValue("");
                }
                
                dataRow.createCell(5).setCellValue(assignment.getPraktikumType().toString());
                
                if (assignment.getCourse() != null) {
                    dataRow.createCell(6).setCellValue(assignment.getCourse().getName());
                } else {
                    dataRow.createCell(6).setCellValue("");
                }
                
                if (assignment.getTeacher() != null) {
                    String teacherName = assignment.getTeacher().getFirstName() + " " +
                            assignment.getTeacher().getLastName();
                    dataRow.createCell(7).setCellValue(teacherName);
                    dataRow.createCell(8).setCellValue(assignment.getTeacher().getEmail() != null ? 
                            assignment.getTeacher().getEmail() : "");
                } else {
                    dataRow.createCell(7).setCellValue("");
                    dataRow.createCell(8).setCellValue("");
                }
                
                if (assignment.getSchool() != null) {
                    dataRow.createCell(9).setCellValue(assignment.getSchool().getName());

                } else {
                    dataRow.createCell(9).setCellValue("");
                }

                dataRow.createCell(10).setCellValue(assignment.getStatus().toString());
                dataRow.createCell(11).setCellValue(assignment.getSchoolYear() != null ? 
                        assignment.getSchoolYear() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private static CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
