package felixstuff;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.InputStream;
import java.util.Map;

/**
 * This class exists as a way for the host application to interact with the felix framework easily. So when the host
 * wants to lookup what bundles exist for instance, it will call the method in this class. As this class has easy access
 * to the bundle context.
 */
public class HostActivator implements BundleActivator
{
    private BundleContext m_context = null;
    private ServiceRegistration serviceRegistration = null;

    public HostActivator()
    {
    }

    public void start(BundleContext context)
    {
        // Save a reference to the bundle context.
        m_context = context;
    }

    public void stop(BundleContext context)
    {
        // Unregister the property lookup service.
        serviceRegistration.unregister();
        m_context = null;
    }

    public Bundle[] getBundles()
    {
        if (m_context != null)
        {
            return m_context.getBundles();
        }
        return null;
    }

    public void installBundle(String location, InputStream bytes) throws Exception {
        System.out.println("Attempting to install: " + location);
        m_context.installBundle(location, bytes);
        System.out.println("Successfully installed");
    }

    public BundleContext getContext()
    {
        return m_context;
    }
}