package com.patientPublisherService;

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
        System.out.println("Patient Service Publisher Started");
        IPatientService patientService = new PatientServiceImplementation(context);
        publishServiceRegistration = context.registerService(
            IPatientService.class.getName(), 
            patientService, 
            null
        );
        System.out.println("Patient Service Registered Successfully.");
    }
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
            System.out.println("Patient Service Publisher Stopped");
            if (publishServiceRegistration != null) {
                publishServiceRegistration.unregister();
                publishServiceRegistration = null;
            }
        } finally {
            Activator.context = null;  // Ensure context is always cleared
        }
    }

}
