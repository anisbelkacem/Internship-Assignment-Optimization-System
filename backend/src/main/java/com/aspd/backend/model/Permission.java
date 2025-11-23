package com.aspd.backend.model;

/**
 * Defines the set of permissions available.
 * <ul>
 *   <li>{@link #VIEW} – Allows reading or viewing resources.</li>
 *   <li>{@link #EDIT} – Allows modifying existing resources.</li>
 *   <li>{@link #MANAGE_USERS} – Grants user management capabilities.
 *       Typically assigned only to administrators.</li>
 * </ul>
 */
public enum Permission {
    VIEW,
    EDIT,
    MANAGE_USERS
}
