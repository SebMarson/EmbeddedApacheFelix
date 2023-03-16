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

        Class lookupImplClazz = app.getTestClass("seb.LookupImpl");

        if (lookupImplClazz != null) {
            System.out.println("Loaded class from bundle: " + lookupImplClazz.getName());

            // Attempt to initialize
            Lookup initializedLookup = (Lookup) lookupImplClazz.getConstructors()[0].newInstance();
            System.out.println("Attempting to retrieve from initialized lookup: " + initializedLookup.lookup("key"));
        } else {
            System.out.println("LookupImpl class not loaded");
        }

        System.out.println("Finished application...");

        app.shutdownApplication();
    }
}
