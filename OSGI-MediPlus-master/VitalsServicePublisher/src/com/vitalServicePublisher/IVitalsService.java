package com.vitalServicePublisher;


public interface IVitalsService {
    boolean recordVitals(int patientId,Vitals vital);
    
    Vitals getVitals(int patientId);
    
    boolean updateVitals(int patientId,Vitals vital);
    
    boolean deleteVitals(int patientId);
}