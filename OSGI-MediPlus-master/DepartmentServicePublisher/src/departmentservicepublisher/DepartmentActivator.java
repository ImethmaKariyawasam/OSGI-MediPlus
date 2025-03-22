package departmentservicepublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class DepartmentActivator implements BundleActivator {
    private ServiceRegistration<?> publishServiceRegistration;
    private static BundleContext context;

    public static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        DepartmentActivator.context = bundleContext;  // Use class name to avoid shadowing
        System.out.println("Department Service Publisher Started");

        // Create an instance of the Department service implementation
        IDepartmentService departmentService = new DepartmentServiceImpl(context);

        // Register the Department service
        publishServiceRegistration = context.registerService(
            IDepartmentService.class.getName(), 
            departmentService, 
            null
        );

        System.out.println("Department Service Registered Successfully.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
            System.out.println("Department Service Publisher Stopped");

            // Unregister the Department service
            if (publishServiceRegistration != null) {
                publishServiceRegistration.unregister();
                publishServiceRegistration = null;
            }
        } finally {
            DepartmentActivator.context = null;  
        }
    }
}