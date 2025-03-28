package com.diagnosisServicePublisher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.hospital.core.database.IDatabaseService;

public class DiagnosisServiceImplementation implements IDiagnosisService {
    private IDatabaseService databaseService;

    public DiagnosisServiceImplementation(IDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public boolean recordDiagnosis(int patientId, Diagnosis diagnosis) {
        String sql = "INSERT INTO diagnosis (patientId, doctorName, diagnosis, type, ICD10, symptoms, level, date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            pstmt.setString(2, diagnosis.getDoctorName());
            pstmt.setString(3, diagnosis.getDiagnosis());
            pstmt.setString(4, diagnosis.getType());
            pstmt.setString(5, diagnosis.getIcd10Code());
            pstmt.setString(6, diagnosis.getSymptoms());
            pstmt.setString(7, diagnosis.getSeverityLevel());
            pstmt.setDate(8, new java.sql.Date(diagnosis.getDiagnosisDate().getTime()));
            
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error recording diagnosis: " + e.getMessage());
            return false;
        }
    }

    public Diagnosis getDiagnosis(int patientId, String doctorName) {
        String sql = "SELECT * FROM diagnosis WHERE patientId = ? AND doctorName = ? ORDER BY date DESC LIMIT 1";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            pstmt.setString(2, doctorName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Diagnosis(
                	rs.getInt("patientId"),
                    rs.getString("doctorName"),
                    rs.getString("diagnosis"),
                    rs.getString("type"),
                    rs.getString("ICD10"),
                    rs.getString("symptoms"),
                    rs.getString("level"),
                    rs.getDate("date")
                );
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Error retrieving diagnosis: " + e.getMessage());
            return null;
        }
    }

    public boolean updateDiagnosis(int patientId,String doctorName, Diagnosis diagnosis) {
        String sql = "UPDATE diagnosis SET doctorName = ?, diagnosis = ?, type = ?, ICD10 = ?, symptoms = ?, level = ?, date = ? WHERE patientId = ? AND doctorName = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, diagnosis.getDoctorName());
            pstmt.setString(2, diagnosis.getDiagnosis());
            pstmt.setString(3, diagnosis.getType());
            pstmt.setString(4, diagnosis.getIcd10Code());
            pstmt.setString(5, diagnosis.getSymptoms());
            pstmt.setString(6, diagnosis.getSeverityLevel());
            pstmt.setDate(7, new java.sql.Date(diagnosis.getDiagnosisDate().getTime()));
            pstmt.setInt(8, patientId);
            pstmt.setString(9,doctorName);
            
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating diagnosis: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteDiagnosis(int patientId, String doctorName) {
        String sql = "DELETE FROM diagnosis WHERE patientId = ? AND doctorName = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            pstmt.setString(2, doctorName);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting diagnosis: " + e.getMessage());
            return false;
        }
    }

}
