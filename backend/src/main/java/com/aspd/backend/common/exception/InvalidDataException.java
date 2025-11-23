package com.aspd.backend.common.exception;

/**
 * Exception thrown when data validation fails during processing,
 * such as invalid field values in Excel imports or API requests.
 */
public class InvalidDataException extends RuntimeException {
    private final String fieldName;
    private final Object invalidValue;

    public InvalidDataException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }

    public InvalidDataException(String fieldName, Object invalidValue, String message) {
        super(String.format("Invalid value for field '%s': %s. %s", fieldName, invalidValue, message));
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
