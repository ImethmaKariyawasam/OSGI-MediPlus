package com.appointmentServicePublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.hospital.core.database.IDatabaseService;

public class Activator implements BundleActivator {
    private ServiceRegistration<?> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Appointment Service Publisher Started");

        // Get database service
        ServiceReference<?> dbServiceRef = context.getServiceReference(IDatabaseService.class.getName());
        if (dbServiceRef != null) {
            IDatabaseService databaseService = (IDatabaseService) context.getService(dbServiceRef);

            // Create and register appointment service
            IAppointmentService appointmentService = new AppointmentServiceImplementation(databaseService);
            serviceRegistration = context.registerService(
                IAppointmentService.class.getName(),
                appointmentService,
                null
            );

            System.out.println("Appointment Service Registered Successfully.");
        } else {
            throw new Exception("Database service not found");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Appointment Service Publisher Stopped");
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}