package com.vitalServicePublisher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.hospital.core.database.IDatabaseService;

public class VitalsServiceImplementation implements IVitalsService {
    private IDatabaseService databaseService;
    
    public VitalsServiceImplementation(IDatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    @Override
    public boolean recordVitals(int patientId, Vitals vital) {
        String sql = "INSERT INTO vitals (patient_id, temperature, heart_rate, " +
                    "blood_pressure_systolic, blood_pressure_diastolic, respiratory_rate) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            pstmt.setDouble(2, vital.getTemperature());
            pstmt.setInt(3, vital.getHeartRate());
            pstmt.setInt(4, vital.getBloodPressureSystolic());
            pstmt.setInt(5, vital.getBloodPressureDiastolic());
            pstmt.setInt(6, vital.getRespiratoryRate());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error recording vitals: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Vitals getVitals(int patientId) {
        String sql = "SELECT * FROM vitals WHERE patient_id = ? ORDER BY recorded_at DESC LIMIT 1";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Vitals(
                    rs.getDouble("temperature"),
                    rs.getInt("heart_rate"),
                    rs.getInt("blood_pressure_systolic"),
                    rs.getInt("blood_pressure_diastolic"),
                    rs.getInt("respiratory_rate")
                );
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Error getting vitals: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean updateVitals(int patientId, Vitals vital ) {
        String sql = "UPDATE vitals SET temperature = ?, heart_rate = ?, " +
                    "blood_pressure_systolic = ?, blood_pressure_diastolic = ?, " +
                    "respiratory_rate = ? WHERE patient_id = ?";
                    
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, vital.getTemperature());
            pstmt.setInt(2, vital.getHeartRate());
            pstmt.setInt(3, vital.getBloodPressureSystolic());
            pstmt.setInt(4, vital.getBloodPressureDiastolic());
            pstmt.setInt(5, vital.getRespiratoryRate());
            pstmt.setInt(6, patientId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating vitals: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteVitals(int patientId) {
        String sql = "DELETE FROM vitals WHERE patient_id = ?";
        
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting vitals: " + e.getMessage());
            return false;
        }
    }
}