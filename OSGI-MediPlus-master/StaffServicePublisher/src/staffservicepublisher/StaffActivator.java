package staffservicepublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class StaffActivator implements BundleActivator {
    private ServiceRegistration<?> publishServiceRegistration;
    private static BundleContext context;

    public static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        StaffActivator.context = bundleContext;  
        System.out.println("Staff Service Publisher Started");

        
        IStaffService staffService = new StaffServiceImpl(context);

        
        publishServiceRegistration = context.registerService(
            IStaffService.class.getName(), 
            staffService, 
            null
        );

        System.out.println("Staff Service Registered Successfully.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
            System.out.println("Staff Service Publisher Stopped");

            
            if (publishServiceRegistration != null) {
                publishServiceRegistration.unregister();
                publishServiceRegistration = null;
            }
        } finally {
            StaffActivator.context = null;  
        }
    }
}