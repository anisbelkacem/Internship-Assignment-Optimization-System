package com.aspd.backend.common.exception;

public class NotFoundException extends RuntimeException {
    private final String resourceType;
    private final Object resourceId;

    public NotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public NotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public NotFoundException(String resourceType, Object resourceId, String additionalMessage) {
        super(String.format("%s not found with id: %s. %s", resourceType, resourceId, additionalMessage));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
