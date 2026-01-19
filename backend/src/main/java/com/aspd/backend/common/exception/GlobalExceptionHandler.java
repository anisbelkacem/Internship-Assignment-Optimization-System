package com.aspd.backend.common.exception;

import com.aspd.backend.validation.AssignmentValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<?> handleInvalidData(InvalidDataException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        if (ex.getFieldName() != null) {
            body.put("field", ex.getFieldName());
            body.put("invalidValue", ex.getInvalidValue());
        }
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException.class)
    public ResponseEntity<?> handleNotXlsx(org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Invalid file format: please upload a .xlsx file");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.apache.poi.ooxml.POIXMLException.class)
    public ResponseEntity<?> handlePoi(org.apache.poi.ooxml.POIXMLException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Corrupted or unsupported Excel file");
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<?> handleIo(java.io.IOException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "I/O error while reading Excel file: " + ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Cannot delete record because it is referenced by other records. Please delete dependent records first.");
        String details = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        body.put("details", details);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AssignmentValidationException.class)
    public ResponseEntity<?> handleAssignmentValidation(AssignmentValidationException ex) {
        return ResponseEntity.badRequest().body(ex.getResult());
    }

}
