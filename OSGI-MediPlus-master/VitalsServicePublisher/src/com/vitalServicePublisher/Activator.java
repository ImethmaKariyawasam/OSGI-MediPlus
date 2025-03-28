package com.vitalServicePublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.hospital.core.database.IDatabaseService;

public class Activator implements BundleActivator {

	 private ServiceRegistration<?> serviceRegistration;
	    
	    @Override
	    public void start(BundleContext context) throws Exception {
	        System.out.println("Vitals Service Publisher Started");
	        
	        // Get database service
	        ServiceReference<?> dbServiceRef = context.getServiceReference(IDatabaseService.class.getName());
	        if (dbServiceRef != null) {
	            IDatabaseService databaseService = (IDatabaseService) context.getService(dbServiceRef);
	            
	            // Create and register vitals service
	            IVitalsService vitalsService = new VitalsServiceImplementation(databaseService);
	            serviceRegistration = context.registerService(
	                IVitalsService.class.getName(), 
	                vitalsService, 
	                null
	            );
	            
	        System.out.println("Vitals Service Registered Successfully.");
	        } else {
	            throw new Exception("Database service not found");
	        }
	    }
	    
	    @Override
	    public void stop(BundleContext context) throws Exception {
	        System.out.println("Vitals Service Publisher Stopped");
	        if (serviceRegistration != null) {
	            serviceRegistration.unregister();
	        }
	    }

}
