package com.finny.dto;

import java.util.List;

public class ImportResponseDto {
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<String> errors;

    public ImportResponseDto() {
    }

    public ImportResponseDto(int totalRows, int successfulRows, int failedRows, List<String> errors) {
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
        this.errors = errors;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessfulRows() {
        return successfulRows;
    }

    public void setSuccessfulRows(int successfulRows) {
        this.successfulRows = successfulRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
