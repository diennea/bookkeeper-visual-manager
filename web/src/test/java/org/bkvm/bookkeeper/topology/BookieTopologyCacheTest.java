package org.bkvm.bookkeeper.topology;

import static org.junit.Assert.assertEquals;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class BookieTopologyCacheTest {

    @Test
    public void test() {
        assertEquals(
                Set.of("pod-name", "pod-name.statefulset", "pod-name.statefulset.namespace",
                        "pod-name.statefulset.namespace.svc", "pod-name.statefulset.namespace.svc.cluster",
                        "pod-name.statefulset.namespace.svc.cluster.local"),
                computePossibleBookiePodNames("pod-name.statefulset.namespace.svc.cluster.local:3181"));
    }

    private Set<String> computePossibleBookiePodNames(String bookieId) {
        Set<String> set = new HashSet<>();
        BookieTopologyCache.computePossibleBookiePodNames(
                bookieId, set);
        return set;
    }

}