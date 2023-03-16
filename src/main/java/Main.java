import felixstuff.HostActivator;
import felixstuff.HostApplication;
import felixstuff.Lookup;
import org.osgi.framework.Bundle;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting application...");
        HostApplication app = new HostApplication();

        Bundle[] bundles = app.getInstalledBundles();
        System.out.println();
        System.out.println("Installed bundles: ");
        for (Bundle bundle : bundles) {
            System.out.println(bundle.getBundleId() + " - " + bundle.getSymbolicName() + " from: " + bundle.getLocation());
        }
        System.out.println();

        // todo - What's the easiest way to find a list of classes in the bundles? UV needs to know what it has loaded, can't hardcode it all the time..
        // Now we have the bundles all loaded, lets try to initialize one of them and run the test method which should give us some bundle specific output
        Lookup bundleOneLookupImpl = initializeBundleLookupImpl(app, "org.example.BundleOneApacheFelix", "seb.LookupImpl");
        System.out.println("Attempting to retrieve from bundle one lookup: " + bundleOneLookupImpl.lookup("key"));

        Lookup bundleTwoLookupImpl = initializeBundleLookupImpl(app, "org.example.BundleTwoApacheFelix", "seb2.LookupImpl");
        System.out.println("Attempting to retrieve from bundle two lookup: " + bundleTwoLookupImpl.lookup("key"));

        System.out.println("Finished application...");

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
            return (Lookup) lookupImplClazz.getConstructors()[0].newInstance();
        } else {
            System.out.println("LookupImpl class not loaded");
            throw new Exception("LookupImpl class not loaded");
        }
    }
}
