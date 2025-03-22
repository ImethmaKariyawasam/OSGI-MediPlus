package com.hospital.core.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseService {
    /**
     * Gets a database connection. Creates a new one if none exists or if the existing one is closed.
     * @return An active database connection
     * @throws SQLException if connection fails
     */
    Connection getConnection() throws SQLException;
    
    /**
     * Closes the current database connection if one exists.
     */
    void closeConnection();
}