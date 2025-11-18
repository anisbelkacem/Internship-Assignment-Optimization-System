package com.aspd.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class SchoolImportResult {
    private int totalRows;
    private int importedCount;
    private int errorCount;
    private List<String> errors = new ArrayList<>();

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public void addError(String error) { this.errors.add(error); }
}
