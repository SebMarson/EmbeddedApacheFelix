package felixstuff;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acts as the go-between for main application and the felix framework. If we need anything from Felix we should use this
 * class
 */
public class HostApplication
{
    private HostActivator m_activator = null;
    private Felix m_felix = null;
    private Map m_lookupMap = new HashMap();
    private ServiceTracker m_tracker = null;

    public HostApplication()
    {
        /* Custom config for the launcher */
        // Create a configuration property map.
        Map configMap = new HashMap();

        // When set to true it tells Felix it is embedded in an application, not running everything standalone
        // REMOVED IN APACHE FELIX FRAMEWORK 2.0.0
        /*configMap.put("felix.embedded.execution",
                "true");*/

        // Directory to use as the bundle cache, relative to the working directory
        configMap.put(Constants.FRAMEWORK_STORAGE,
                "felix-cache");

        // Flushes the bundle cache on startup
        configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN,
                Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        // Buffer size that the cache can use (default is 4096)
        configMap.put("felix.cache.bufsize",
                "1024");

        // Add core OSGi packages to be exported from the class path
        // via the system bundle.
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
                "org.osgi.framework; version=1.3.0," +
                        "org.osgi.service.packageadmin; version=1.2.0," +
                        "org.osgi.service.startlevel; version=1.0.0," +
                        "org.osgi.service.url; version=1.0.0," +
                        "felixstuff; version=1.0.0");

        // Specifies which class loader to use fo boot delegation, default is boot, but I changed to app because this is embedded so think we want that...
        configMap.put(Constants.FRAMEWORK_BUNDLE_PARENT,
                Constants.FRAMEWORK_BUNDLE_PARENT_APP);

        // Specifies the auto-deploy directory from which bundles are automatically deployed at framework startup. So for UV that would be the appdata/plugins dir. Defauly is bundle/
        configMap.put("felix.auto.deploy.dir",
                "loadBundles/");

        // Create host activator;
        m_activator = new HostActivator();
        List list = new ArrayList();
        list.add(m_activator);

        // List of bundle activators to start when the system bundle is started, they received the bundlecontext when invoked
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);

        try
        {
            // Now create an instance of the framework with
            // our configuration properties.
            m_felix = new Felix(configMap);
            // Now start Felix instance.
            m_felix.start();

            // try to load the latest commons io bundle
            File commonsIo = new File("S:\\workspace\\EmbeddedApacheFelix\\loadBundles\\commons-io-2.11.0.jar");
            FileInputStream commonsStream = new FileInputStream(commonsIo);
            m_activator.installBundle(commonsIo.getAbsolutePath(), commonsStream);

            // try to load a very early commons io bundle
            File commonsOldIo = new File("S:\\workspace\\EmbeddedApacheFelix\\loadBundles\\commons-io-1.4.jar");
            FileInputStream commonsOldStream = new FileInputStream(commonsOldIo);
            m_activator.installBundle(commonsOldIo.getAbsolutePath(), commonsOldStream);

            // try to load the one bundle
            File bundleOne = new File("S:\\workspace\\EmbeddedApacheFelix\\loadBundles\\BundleOneApacheFelix-1.1-SNAPSHOT.jar");
            FileInputStream bundleOneStream = new FileInputStream(bundleOne);
            m_activator.installBundle(bundleOne.getAbsolutePath(), bundleOneStream);

            // try to load the two bundle
            File bundleTwo = new File("S:\\workspace\\EmbeddedApacheFelix\\loadBundles\\BundleTwoApacheFelix-1.0-SNAPSHOT.jar");
            FileInputStream bundleTwoStream = new FileInputStream(bundleTwo);
            m_activator.installBundle(bundleTwo.getAbsolutePath(), bundleTwoStream);

        }
        catch (Exception ex)
        {
            System.err.println("Could not create framework: " + ex);
            ex.printStackTrace();
        }

        System.out.println("Initialized HostApplication..");

    }

    public Bundle[] getInstalledBundles()
    {
        // Use the system bundle activator to gain external
        // access to the set of installed bundles.
        return m_activator.getBundles();
    }

    public boolean execute(String name, String commandline)
    {
        // See if any of the currently tracked command services
        // match the specified command name, if so then execute it.
        Object[] services = m_tracker.getServices();
        for (int i = 0; (services != null) && (i < services.length); i++) {
            try {
                if (((Command) services[i]).getName().equals(name)) {
                    return ((Command) services[i]).execute(commandline);
                }
            }
            catch (Exception ex) {
                // Since the services returned by the tracker could become
                // invalid at any moment, we will catch all exceptions, log
                // a message, and then ignore faulty services.
                System.err.println(ex);
            }
        }
        return false;
    }

    public void shutdownApplication() throws Exception {
        // Shut down the felix framework when stopping the
        // host application.
        m_felix.stop();
        m_felix.waitForStop(0);
    }

    /**
     * Checks the given bundle for the given class. If it exists then pass back the Class object for it.
     *
     * @param bundleName
     * @param clazzName
     * @return
     * @throws Exception
     */
    public Class getTestClass(String bundleName, String clazzName) throws Exception {
        for (Bundle bundle : m_activator.getContext().getBundles()) {
            if (bundleName.equals(bundle.getSymbolicName())) {
                return bundle.loadClass(clazzName);
            }
        }
        // If we can't find the right bundle throw an error
        throw new Exception("Failed to find class " + clazzName + " in bundle org.example.BundleOneApacheFelix");
    }
}