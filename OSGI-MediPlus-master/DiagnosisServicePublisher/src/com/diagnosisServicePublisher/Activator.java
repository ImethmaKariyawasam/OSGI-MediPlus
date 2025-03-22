package com.diagnosisServicePublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import com.hospital.core.database.IDatabaseService;

public class Activator implements BundleActivator {
    private ServiceRegistration<?> serviceRegistration;
    
    // Remove static context variable, using the one passed in the start() method.
    
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("Diagnosis Service Publisher Started");

        // Use the correct bundleContext parameter here
        ServiceReference<?> dbServiceRef = bundleContext.getServiceReference(IDatabaseService.class.getName());
        
        if (dbServiceRef != null) {
            IDatabaseService databaseService = (IDatabaseService) bundleContext.getService(dbServiceRef);
            
            // Create and register the diagnosis service
            IDiagnosisService diagnosisService = new DiagnosisServiceImplementation(databaseService);
            serviceRegistration = bundleContext.registerService(
            	IDiagnosisService.class.getName(), 
                diagnosisService, 
                null
            );
            
            System.out.println("Diagnosis Service Registered Successfully.");
        } else {
            throw new Exception("Database service not found");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Diagnosis Service Publisher Stopped");
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}
