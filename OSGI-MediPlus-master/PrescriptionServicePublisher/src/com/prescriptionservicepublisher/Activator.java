package com.prescriptionservicepublisher;

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
        System.out.println("Prescription Service Publisher Started");
        IPrescriptionService prescriptionService = new PrescriptionServiceImplementation(context);
        publishServiceRegistration = context.registerService(
            IPrescriptionService.class.getName(), 
            prescriptionService, 
            null
        );
        System.out.println("Prescription Service Registered Successfully.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
            System.out.println("Prescription Service Publisher Stopped");
            if (publishServiceRegistration != null) {
                publishServiceRegistration.unregister();
                publishServiceRegistration = null;
            }
        } finally {
            Activator.context = null;  // Ensure context is always cleared
        }
    }
}