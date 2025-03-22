package com.appointmentServicePublisher;

import java.util.List;

public interface IAppointmentService {
    boolean scheduleAppointment(Appointment appointment);
    boolean updateAppointment(Appointment appointment);
    boolean cancelAppointment(int appointmentId);
    Appointment getAppointment(int appointmentId);
    List<Appointment> getAppointmentsByPatient(int patientId);
    List<Appointment> getAppointmentsByDoctor(String doctorName);
    List<Appointment> searchAppointments(String searchTerm);
    List<Appointment> getAppointmentsByStatus(String status);
}