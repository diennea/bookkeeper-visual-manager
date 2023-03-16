/*
 * Licensed to Diennea S.r.l. under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Diennea S.r.l. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.bkvm.bookkeeper.topology;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bkvm.cache.Bookie;
import org.bkvm.cache.MetadataCache;
import org.bkvm.config.ConfigurationStore;
import org.bkvm.config.ServerConfiguration;

public class BookieTopologyCache {

    private static final Logger LOG = Logger.getLogger(BookieTopologyCache.class.getName());

    private final MetadataCache metadataCache;
    private final KubernetesClient client;
    private Map<String, BookieTopology> cachedTopology;

    public BookieTopologyCache(ConfigurationStore configurationStore, MetadataCache metadataCache) {
        this.metadataCache = metadataCache;

        final boolean enabled =
                Boolean.parseBoolean(
                        configurationStore.getProperty(ServerConfiguration.PROPERTY_ENABLE_BOOKIES_TOPOLOGY,
                                ServerConfiguration.PROPERTY_ENABLE_BOOKIES_TOPOLOGY_DEFAULT));


        LOG.log(Level.INFO, "Bookies topology enabled: {0}", enabled);
        if (enabled) {
            client = new KubernetesClientBuilder()
                    .withConfig(Config.autoConfigure(null))
                    .build();
        } else {
            client = null;
        }
    }

    @Data
    @AllArgsConstructor
    private static final class NodeTopology {
        String region;
        String zone;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class BookieTopology {

        private String region;
        private String zone;
        private String node;

    }


    public synchronized Map<String, BookieTopology> refreshBookiesTopology() {
        if (client == null) {
            return null;
        }
        final Map<String, BookieTopology> newTopology = collectTopology();
        cachedTopology = newTopology;
        return newTopology;
    }

    public Map<String, BookieTopology> getBookiesTopology() {
        if (client == null) {
            return null;
        }
        return cachedTopology;
    }

    private Map<String, BookieTopology> collectTopology() {
        final Collection<Bookie> allBookies = metadataCache.listBookies();
        LOG.log(Level.INFO, "Collecting bookie topology for {0} bookies", allBookies.size());
        final Map<String, Set<String>> bookiePossibleNames = allBookies.stream().map(bookie -> {
            final Bookie.BookieInfo bookieInfo = Bookie.parseBookieInfo(bookie.getBookieInfo());
            return Map.entry(bookie.getBookieId(),
                    computePossibleBookiePodNames(bookie.getBookieId(), bookieInfo.getEndpoints()));
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        Map<String, BookieTopology> bookiesTopologies = new HashMap<>();


        Map<String, NodeTopology> nodeTopologyMap = new HashMap<>();

        client.pods().inAnyNamespace().list().getItems().stream()
                .forEach(pod -> {
                    for (Map.Entry<String, Set<String>> bookieNames : bookiePossibleNames.entrySet()) {
                        if (bookieNames.getValue().contains(pod.getSpec().getHostname())) {
                            final String nodeName = pod.getSpec().getNodeName();
                            final NodeTopology
                                    nodeTopology = getNodeTopology(nodeName, nodeTopologyMap);
                            bookiesTopologies.put(bookieNames.getKey(), BookieTopology.builder()
                                    .node(nodeName)
                                    .zone(nodeTopology.getZone())
                                    .region(nodeTopology.getRegion())
                                    .build()
                            );
                        }
                    }
                });

        allBookies.stream().filter(b -> !bookiesTopologies.containsKey(b.getBookieId())).forEach(b -> {
            bookiesTopologies.put(b.getBookieId(), BookieTopology.builder().build());
        });

        return bookiesTopologies;
    }


    private static Set<String> computePossibleBookiePodNames(String bookieId, List<Bookie.EndpointInfo> endpoints) {
        Set<String> possiblePodNames = new HashSet<>();
        computePossibleBookiePodNames(bookieId, possiblePodNames);
        if (endpoints != null) {
            for (Bookie.EndpointInfo endpoint : endpoints) {
                if (endpoint.getAddress() != null && endpoint.getAddress().startsWith("bookie-rpc://")) {
                    String podName = endpoint.getAddress().substring("bookie-rpc://".length());
                    computePossibleBookiePodNames(podName, possiblePodNames);
                }
            }
        }
        return possiblePodNames;
    }


    static void computePossibleBookiePodNames(String bookieId, Set<String> possiblePodNames) {
        final String hostNoPort = bookieId.split(":")[0];

        final String[] split = hostNoPort.split("\\.");
        for (int i = 1; i < split.length + 1; i++) {

            List<String> names = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                names.add(split[j]);
            }
            String name = names.stream().collect(Collectors.joining("."));
            possiblePodNames.add(name);
        }
    }

    private NodeTopology getNodeTopology(String node, Map<String, NodeTopology> nodeTopologyCache) {
        if (node == null) {
            return new NodeTopology(null, null);
        }
        return nodeTopologyCache.computeIfAbsent(node, n -> {
            try {
                final Map<String, String> nodeLabels =
                        client.nodes().withName(n).get().getMetadata().getLabels();
                return new NodeTopology(nodeLabels.get("failure-domain.beta.kubernetes.io/region"),
                        nodeLabels.get("failure-domain.beta.kubernetes.io/zone"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
