package com.hospital.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseServiceImplementation implements IDatabaseService {
    private Connection connection = null;
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/hospital";
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    
    @Override
    public Connection getConnection() throws SQLException {
        try {
            // Check if connection is null or closed
            if (connection == null || connection.isClosed()) {
                // Load the JDBC driver
                try {
                    Class.forName(DRIVER_NAME);
                } catch (ClassNotFoundException e) {
                    throw new SQLException("MySQL JDBC Driver not found. Add the missing dependency to your manifest.", e);
                }
                
                // Create new connection
                connection = DriverManager.getConnection(
                    DATABASE_URL,
                    DATABASE_USER,
                    DATABASE_PASSWORD
                );
                
                // Set auto-commit to true for simplicity
                connection.setAutoCommit(true);
                
                if (connection != null) {
                    System.out.println("Database Connected Successfully!");
                }
            }
            
            // Test the connection before returning
            if (!connection.isValid(5)) { // 5 second timeout
                System.out.println("Connection invalid, reconnecting...");
                connection.close();
                connection = null;
                return getConnection(); // Recursive call to establish new connection
            }
            
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            // Close the invalid connection if exists
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    // Ignore close errors
                }
                connection = null;
            }
            throw e;
        }
    }
    
    @Override
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database Connection Closed!");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}