package com.hospital.core.database;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    private ServiceRegistration<?> publishServiceRegistration;
    private static BundleContext context;
    
    public static BundleContext getContext() {
        return context;
    }
    
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;  // Use class name to avoid shadowing
        
        try {
            System.out.println("Database Publisher Started");
            IDatabaseService databaseService = new DatabaseServiceImplementation();
            publishServiceRegistration = bundleContext.registerService(
                IDatabaseService.class.getName(), 
                databaseService, 
                null
            );
        } catch (Exception e) {
            System.err.println("Error starting database service: " + e.getMessage());
            throw e;  // Re-throw to properly signal bundle start failure
        }
    }
    
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
            System.out.println("Database Publisher Stopped");
            if (publishServiceRegistration != null) {
                publishServiceRegistration.unregister();
                publishServiceRegistration = null;
            }
        } finally {
            Activator.context = null;  // Ensure context is always cleared
        }
    }
}