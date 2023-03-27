package felixstuff;

import org.apache.felix.fileinstall.internal.FileInstall;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LoggerFactory;
import org.osgi.util.tracker.ServiceTracker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Acts as the go-between for main application and the felix framework. If we need anything from Felix we should use this
 * class
 */
public class HostApplication
{
    private HostActivator m_activator = null;
    public Felix m_felix = null;
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
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                        "felixstuff; version=1.0.0");

        // Specifies which class loader to use fo boot delegation, default is boot, but I changed to app because this is embedded so think we want that...
        configMap.put(Constants.FRAMEWORK_BUNDLE_PARENT,
                Constants.FRAMEWORK_BUNDLE_PARENT_APP);

        // Specifies the auto-deploy directory from which bundles are automatically deployed at framework startup. So for UV that would be the appdata/plugins dir. Defauly is bundle/
        configMap.put("felix.auto.deploy.dir",
                new File("autoLoadBundles").getAbsolutePath());

        // Specifies the auto-deploy directory from which bundles are automatically deployed at framework startup. So for UV that would be the appdata/plugins dir. Defauly is bundle/
        configMap.put("felix.auto.deploy.action",
                "install");

        configMap.put("felix.fileinstall.dir", new File("autoLoadBundles").getAbsolutePath());
        configMap.put("felix.fileinstall.noInitialDelay", "true");
        configMap.put("felix.fileinstall.bundles.new.start", "false");
        configMap.put("felix.log.level", "4");
        configMap.put("felix.log.file", "S:\\workspace\\EmbeddedApacheFelix\\felix.log");

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

            BundleContext  context = m_felix.getBundleContext();

            // Create bundle listener
            context.addBundleListener(new MyBundleListener(context));

            // Create File Installer
            installAndStartBundle("S:\\workspace\\EmbeddedApacheFelix\\loadBundles\\org.apache.felix.fileinstall-3.7.4.jar");
            Dictionary<String, String> fileConfig = new Hashtable();
            // fileConfig.put("felix.fileinstall.dir", new File("autoLoadBundles").getAbsolutePath());
            // fileConfig.put("felix.fileinstall.noInitialDelay", "true");
            // fileConfig.put("felix.fileinstall.bundles.new.start", "false");
            // fileConfig.put("felix.fileinstall.start.level", "-1");
            context.registerService(FileInstall.class.getName(), new FileInstall(), fileConfig);

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

    public boolean installAndStartBundle(String bundlePath) {
        try {
            // try to load the one bundle
            File bundleOne = new File(bundlePath);
            FileInputStream bundleOneStream = new FileInputStream(bundleOne);
            m_activator.installAndStartBundle(bundleOne.getAbsolutePath(), bundleOneStream, false);
            return true;
        } catch (Exception e) {
            System.out.println("Failed to install " + bundlePath + ", reason: " + e.getMessage());
        }
        return false;
    }

    public boolean installBundle(String bundlePath) {
        try {
            // try to load the one bundle
            File bundleOne = new File(bundlePath);
            FileInputStream bundleOneStream = new FileInputStream(bundleOne);
            m_activator.installBundle(bundleOne.getAbsolutePath(), bundleOneStream, true);
            return true;
        } catch (Exception e) {
            System.out.println("Failed to install " + bundlePath + ", reason: " + e.getMessage());
        }
        return false;
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