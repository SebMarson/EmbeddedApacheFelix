package felixstuff;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    public Bundle getBundle(String name) {
        for (Bundle bundle : m_context.getBundles()) {
            if (bundle.getSymbolicName().equals(name)) {
                return bundle;
            }
        }
        return null;
    }

    public List<String> getClasses(String bundleName) throws Exception {
        Bundle bundle = getBundle(bundleName);
        // The adapt method lets you 'adapt' one object into another type or interface. In this case we turn a bundle int oa bundlewiring class, which we need this bundlewiring object to know the wiring of the bundle into the framework and the class loader, etc.
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            List<String> classNames = new ArrayList<String>();

            // I tried to use seb/ as the path (first argument) because that's what the path of the classes we're loading are. For UV it would start with UnaVista
            // In the end I had to change LISTRESOURCES_LOCAL to _RECURSE, and then it shows everything, but it seem to include classes for other bundles that are wired to this one...
            for (String className : wiring.listResources("/", "*.class", BundleWiring.LISTRESOURCES_RECURSE)) {
                className = className.substring(0, className.length() - 6).replace("/", ".");
                classNames.add(className);
            }

            return classNames;
        } else {
            throw new Exception("BundleWiring is null, you may not have resolved the bundle yet");
        }
    }

    public void printClasses(String bundleName) throws Exception {
        System.out.println("List of loaded classes from :" + bundleName);
        for (String clazzName : getClasses(bundleName)) {
            System.out.println(clazzName);
        }
    }

    public void installBundle(String location, InputStream bytes, boolean printClasses) throws Exception {
        System.out.println();
        System.out.println("Attempting to install: " + location);
        Bundle bundle = m_context.installBundle(location, bytes);
        System.out.println("Successfully installed");
        bundle.start(Bundle.START_TRANSIENT);
        System.out.println("Bundle started in transient state");
        if (printClasses) {
            printClasses(bundle.getSymbolicName());
        }
    }

    public BundleContext getContext()
    {
        return m_context;
    }
}