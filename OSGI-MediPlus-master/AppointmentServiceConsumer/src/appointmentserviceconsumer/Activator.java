package appointmentserviceconsumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.appointmentServicePublisher.IAppointmentService;

public class Activator implements BundleActivator {
    private ServiceReference<?> appointmentServiceRef;
    private IAppointmentService appointmentService;
    private AppointmentUI appointmentUI;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Appointment Consumer Started");

        // Get the Appointment Service
        appointmentServiceRef = context.getServiceReference(IAppointmentService.class.getName());
        if (appointmentServiceRef != null) {
            appointmentService = (IAppointmentService) context.getService(appointmentServiceRef);
            System.out.println("Appointment Service Retrieved");

            // Start the Appointment UI
            appointmentUI = new AppointmentUI(appointmentService);
            appointmentUI.open();
        } else {
            System.err.println("Appointment Service Not Found");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Appointment Consumer Stopped");

        // Clean up
        if (appointmentUI != null) {
            appointmentUI.close();
        }
        if (appointmentServiceRef != null) {
            context.ungetService(appointmentServiceRef);
        }
    }
}
