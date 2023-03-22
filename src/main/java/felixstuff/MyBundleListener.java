package felixstuff;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class MyBundleListener implements BundleListener {
    private BundleContext context;

    public MyBundleListener(BundleContext context) {
        this.context = context;
    }

    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        int eventType = event.getType();

        switch (eventType) {
            case BundleEvent.INSTALLED:
                System.out.println("Bundle " + bundle.getSymbolicName() + " installed.");
                break;
            case BundleEvent.STARTED:
                System.out.println("Bundle " + bundle.getSymbolicName() + " started.");
                break;
            case BundleEvent.STOPPED:
                System.out.println("Bundle " + bundle.getSymbolicName() + " stopped.");
                break;
            case BundleEvent.UPDATED:
                System.out.println("Bundle " + bundle.getSymbolicName() + " updated.");
                break;
            case BundleEvent.UNINSTALLED:
                System.out.println("Bundle " + bundle.getSymbolicName() + " uninstalled.");
                break;
        }
    }
}