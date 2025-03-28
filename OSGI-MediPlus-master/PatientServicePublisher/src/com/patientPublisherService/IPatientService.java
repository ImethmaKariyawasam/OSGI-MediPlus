package com.patientPublisherService;

import java.util.List;

public interface IPatientService {
 boolean addPatient(Patient patient );
 boolean updatePatient(Patient patient);
 boolean deletePatient(int id);
 boolean getPatient(int id);
 String getPatientDetails(int id);
 List<Patient> searchPatients(String term);
 List<Patient> getAllPatients();
 Patient getPatientById(int id);
}