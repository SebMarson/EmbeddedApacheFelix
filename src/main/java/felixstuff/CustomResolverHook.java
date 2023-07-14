package felixstuff;

import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import java.util.Collection;

public class CustomResolverHook implements ResolverHook {

    @Override
    public void filterResolvable(Collection<BundleRevision> collection) {
        System.out.println("filterResolvable hit");
        for (BundleRevision revision : collection) {
            System.out.println("Revision symbolicname: " + revision.getSymbolicName());

        }
    }

    @Override
    public void filterSingletonCollisions(BundleCapability bundleCapability, Collection<BundleCapability> collection) {
        System.out.println("filterSingletonCollisions hit");
    }

    @Override
    public void filterMatches(BundleRequirement bundleRequirement, Collection<BundleCapability> collection) {
        System.out.println("filterMatches hit");
    }

    @Override
    public void end() {

    }
}
