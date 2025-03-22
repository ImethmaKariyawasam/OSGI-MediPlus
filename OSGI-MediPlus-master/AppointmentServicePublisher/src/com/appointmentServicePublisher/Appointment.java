package com.appointmentServicePublisher;

import java.util.Date;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private String doctorName;
    private Date appointmentDate;
    private String reason;
    private String status; // e.g., Scheduled, Completed, Cancelled
    private int duration; // in minutes

    // Constructors
    public Appointment() {}

    public Appointment(int appointmentId, int patientId, String doctorName, Date appointmentDate, String reason, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
        this.status = status;
        this.duration = 30; // Default duration of 30 minutes
    }

    // Constructors with duration
    public Appointment(int appointmentId, int patientId, String doctorName, Date appointmentDate, String reason, String status, int duration) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.reason = reason;
        this.status = status;
        this.duration = duration;
    }

    // Getters and Setters
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    @Override
    public String toString() {
        return String.format(
            "Appointment ID: %d\nPatient ID: %d\nDoctor: %s\nDate: %s\nDuration: %d minutes\nReason: %s\nStatus: %s",
            appointmentId, patientId, doctorName, appointmentDate, duration, reason, status
        );
    }
}