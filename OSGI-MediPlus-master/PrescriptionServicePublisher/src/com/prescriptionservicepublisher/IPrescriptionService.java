package com.prescriptionservicepublisher;

import java.util.*;

public interface IPrescriptionService {
    // CRUD operations
    int createPrescription(Prescription prescription) throws Exception;
    Prescription getPrescriptionById(int id) throws Exception;
    List<Prescription> getAllPrescriptions() throws Exception;
    boolean updatePrescription(Prescription prescription) throws Exception;
    boolean deletePrescription(int id) throws Exception;
    
    // Validation methods
    boolean isValidPatient(int patientId) throws Exception;
    boolean isValidDoctor(int doctorId) throws Exception;
    boolean isValidMedication(int medicationId) throws Exception;
    
}