package com.appointmentServicePublisher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hospital.core.database.IDatabaseService;

public class AppointmentServiceImplementation implements IAppointmentService {
    private IDatabaseService databaseService;

    public AppointmentServiceImplementation(IDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public boolean scheduleAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (patient_id, doctor_name, appointment_date, reason, status, duration) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setString(2, appointment.getDoctorName());
            pstmt.setTimestamp(3, new java.sql.Timestamp(appointment.getAppointmentDate().getTime()));
            pstmt.setString(4, appointment.getReason());
            pstmt.setString(5, appointment.getStatus());
            pstmt.setInt(6, appointment.getDuration());
            
            int result = pstmt.executeUpdate();
            
            // Get the generated appointment ID and set it to the appointment object
            if (result > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    appointment.setAppointmentId(generatedKeys.getInt(1));
                }
            }
            
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error scheduling appointment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateAppointment(Appointment appointment) {
        String sql = "UPDATE appointments SET patient_id=?, doctor_name=?, appointment_date=?, reason=?, status=?, duration=? WHERE appointment_id=?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setString(2, appointment.getDoctorName());
            pstmt.setTimestamp(3, new java.sql.Timestamp(appointment.getAppointmentDate().getTime()));
            pstmt.setString(4, appointment.getReason());
            pstmt.setString(5, appointment.getStatus());
            pstmt.setInt(6, appointment.getDuration());
            pstmt.setInt(7, appointment.getAppointmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelAppointment(int appointmentId) {
        // In this implementation, we'll update the status to 'Cancelled' rather than deleting
        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE appointment_id=?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error canceling appointment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Appointment getAppointment(int appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointment_id=?";
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Appointment appointment = new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("patient_id"),
                    rs.getString("doctor_name"),
                    rs.getTimestamp("appointment_date"),
                    rs.getString("reason"),
                    rs.getString("status")
                );
                appointment.setDuration(rs.getInt("duration"));
                return appointment;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error fetching appointment: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        String sql = "SELECT * FROM appointments WHERE patient_id=? ORDER BY appointment_date DESC";
        return getAppointments(sql, patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(String doctorName) {
        String sql = "SELECT * FROM appointments WHERE doctor_name=? ORDER BY appointment_date DESC";
        return getAppointments(sql, doctorName);
    }
    
    @Override
    public List<Appointment> searchAppointments(String searchTerm) {
        String sql = "SELECT * FROM appointments WHERE " +
                     "patient_id LIKE ? OR " +
                     "doctor_name LIKE ? OR " +
                     "reason LIKE ? OR " +
                     "status LIKE ? " +
                     "ORDER BY appointment_date DESC";
        
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment appointment = new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("patient_id"),
                    rs.getString("doctor_name"),
                    rs.getTimestamp("appointment_date"),
                    rs.getString("reason"),
                    rs.getString("status")
                );
                appointment.setDuration(rs.getInt("duration"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            System.err.println("Error searching appointments: " + e.getMessage());
        }
        return appointments;
    }
    
    @Override
    public List<Appointment> getAppointmentsByStatus(String status) {
        String sql = "SELECT * FROM appointments WHERE status=? ORDER BY appointment_date DESC";
        return getAppointments(sql, status);
    }

    private List<Appointment> getAppointments(String sql, Object param) {
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = databaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (param instanceof Integer) {
                pstmt.setInt(1, (Integer) param);
            } else if (param instanceof String) {
                pstmt.setString(1, (String) param);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Appointment appointment = new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("patient_id"),
                    rs.getString("doctor_name"),
                    rs.getTimestamp("appointment_date"),
                    rs.getString("reason"),
                    rs.getString("status")
                );
                appointment.setDuration(rs.getInt("duration"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
        }
        return appointments;
    }
}