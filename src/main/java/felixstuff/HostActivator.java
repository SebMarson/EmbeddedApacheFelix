package felixstuff;

import org.apache.felix.fileinstall.internal.FileInstall;
import org.osgi.framework.*;
import org.osgi.framework.wiring.BundleWiring;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

        Map configMap = new HashMap();
        configMap.put("felix.fileinstall.dir", new File("autoLoadBundles").getAbsolutePath());
        configMap.put("felix.fileinstall.noInitialDelay", "true");
        System.out.println("Registering file install service...");
        // m_context.registerService(FileInstall.class.getName(), new FileInstall(), configMap);
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

    /**
     * Used to check if a bundle includes an activator. When you start a bundle with an activator class it will start running
     * code inside the start method immediately. So you can use this to check what bundles may be actively running tasks
     *
     * @param bundle
     */
    public boolean checkBundleIncludesActivator(Bundle bundle) {
        String activatorClassName = bundle.getHeaders().get(Constants.BUNDLE_ACTIVATOR);
        if (activatorClassName != null) {
            System.out.println("Bundle " + bundle.getSymbolicName() + " has an activator class: " + activatorClassName);
            return true;
        } else {
            return false;
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
        if (printClasses) {
            printClasses(bundle.getSymbolicName());
        }
    }

    public void installAndStartBundle(String location, InputStream bytes, boolean printClasses) throws Exception {
        System.out.println();
        System.out.println("Attempting to install: " + location);
        Bundle bundle = m_context.installBundle(location, bytes);
        System.out.println("Successfully installed");
        bundle.start();
        // System.out.println("Bundle started in transient state");
        if (printClasses) {
            printClasses(bundle.getSymbolicName());
        }
    }

    public BundleContext getContext()
    {
        return m_context;
    }
}