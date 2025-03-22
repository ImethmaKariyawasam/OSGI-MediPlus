package com.diagnosisServicePublisher;

public interface IDiagnosisService {
	
    boolean recordDiagnosis(int patientId,Diagnosis diagnosis);
    
    Diagnosis getDiagnosis(int patientId,String doctorName);
    
    boolean updateDiagnosis(int patientId,String doctorName,Diagnosis diagnosis);
    
    boolean deleteDiagnosis(int patientId,String doctorName);
    
}
