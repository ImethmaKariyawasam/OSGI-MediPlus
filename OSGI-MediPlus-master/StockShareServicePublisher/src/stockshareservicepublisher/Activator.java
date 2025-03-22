package stockshareservicepublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<StockShareService> serviceRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("Stock Share Service Publisher Started");

        // Correctly instantiate the StockServiceShareImplementation with BundleContext
        StockShareService stockShareService = new StockServiceShareImplementation(bundleContext);
        
        // Register the service properly
        serviceRegistration = bundleContext.registerService(StockShareService.class, stockShareService, null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("Stock Share Service Publisher Stopped");

        // Unregister the service to clean up resources
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}
