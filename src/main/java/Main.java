import felixstuff.HostActivator;
import felixstuff.HostApplication;
import felixstuff.Lookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Constructor;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting application...");
        HostApplication app = new HostApplication();

        Bundle[] bundles = app.getInstalledBundles();
        System.out.println();
        System.out.println("Installed bundles: ");
        for (Bundle bundle : bundles) {
            System.out.println(bundle.getBundleId() + " - " + bundle.getSymbolicName() + " from: " + bundle.getLocation() + " in state " + bundle.getState());
        }
        System.out.println();

        // Now we have the bundles all loaded, lets try to initialize one of them and run the test method which should give us some bundle specific output
        try {
            Lookup bundleOneLookupImpl = initializeBundleLookupImpl(app, "org.example.BundleOneApacheFelix", "seb.LookupImpl");
            System.out.println("Attempting to retrieve from bundle one lookup: " + bundleOneLookupImpl.lookup("key"));
        } catch (Exception e) {
            System.out.println("Failed bundle one, reason: " + e.getMessage());
        }

        try {
            Lookup bundleTwoLookupImpl = initializeBundleLookupImpl(app, "org.example.BundleTwoApacheFelix", "seb2.LookupImpl");
            System.out.println("Attempting to retrieve from bundle two lookup: " + bundleTwoLookupImpl.lookup("key"));
        } catch (Exception e) {
            System.out.println("Failed bundle two, reason: " + e.getMessage());
        }

        System.out.println("Finished application...");

        try {
            app.m_felix.waitForStop(0);
        } finally {
            System.exit(0);
        }
        app.shutdownApplication();
    }

    /**
     * Tries to initialize a known Lookup class implementation from a bundle.
     *
     * @param app
     * @param bundleName
     * @param className
     * @return
     * @throws Exception
     */
    public static Lookup initializeBundleLookupImpl(HostApplication app, String bundleName, String className) throws Exception {
        Class lookupImplClazz = app.getTestClass(bundleName, className);

        if (lookupImplClazz != null) {
            System.out.println("Loaded class from bundle: " + lookupImplClazz.getName());

            // Attempt to initialize
            Constructor constructor = lookupImplClazz.getConstructors()[0];
            if (constructor.getParameterTypes().length == 1 && constructor.getParameterTypes()[0].getName().equals(BundleContext.class.getName())) {
                return (Lookup) lookupImplClazz.getConstructors()[0].newInstance(app.context);
            } else {
                return (Lookup) lookupImplClazz.getConstructors()[0].newInstance();
            }
        } else {
            System.out.println("LookupImpl class not loaded");
            throw new Exception("LookupImpl class not loaded");
        }
    }
}
