<template>
    <div class="bvm-bookie">
        <v-tabs
            background-color="blue lighten-1"
            center-active
            dark>
            <v-tab>Cache Status</v-tab>
            <v-tab>Cluster Status on ZooKeeper</v-tab>
            <v-tab>Client Configuration</v-tab>
             <v-tab-item>
                <p class="caption my-2">
                    BookKeeper Visual Manager caches data on a local database
                    in order to save resources on the Metadata Service (ZooKeeper).
                    You have to manually request a reload from ZooKeeper.
                </p>
                <v-btn
                    depressed
                    large
                    tile
                    color="blue lighten-1 white--text"
                    @click="refreshPage">
                    Reload page
                </v-btn>
                <v-btn
                    depressed
                    large
                    tile
                    color="green lighten-1 white--text"
                    @click="refreshCache">
                    Reload metadata from ZooKeeper
                </v-btn>
                <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                        <span class="ml-2" v-on="on">Background worker status <b>{{ status }}</b></span>
                    </template>
                    <span>Last reload from ZooKeeper was at <b>{{ new Date(lastCacheRefresh ) }}</b></span>
                </v-tooltip>
            </v-tab-item>
            <v-tab-item>
<!--                <div v-for="cluster in clusters" v-bind:key=":cluster">
                    <div>{{ cluster.clusterName }}</div>
                <v-simple-table class="mt-2 elevation-1">
                    <template v-slot:default>
                        <thead>
                            <tr>
                                <th class="text-left">Property</th>
                                <th class="text-left">Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr> <td>autorecoveryEnabled</td> <td>{{ cluster.autorecoveryEnabled }}</td></tr>
                            <tr> <td>Auditor</td> <td>{{ cluster.auditor }}</td></tr>
                            <tr> <td>lostBookieRecoveryDelay</td> <td>{{ cluster.lostBookieRecoveryDelay }}</td></tr>
                            <tr> <td>layoutManagerFactoryClass</td> <td>{{ cluster.layoutManagerFactoryClass }}</td></tr>
                            <tr> <td>layoutFormatVersion</td> <td>{{ cluster.layoutFormatVersion }}</td></tr>
                            <tr> <td>layoutManagerVersion</td> <td>{{ cluster.layoutManagerVersion }}</td></tr>
                        </tbody>
                    </template>
                </v-simple-table>
                </div>                -->
            </v-tab-item>
            <v-tab-item>
                <div v-for="cluster in clusters" :key="cluster">
                <span class="caption mt-2">Client configuration used for cluster {{ cluster.clusterName }}</span>
                <v-data-table
                    :headers="headers"
                    :items="cluster.computedBookkeeperConfiguration"
                    multi-sort
                    hide-default-footer
                    class="mt-2 elevation-1"
                />
                </div>
            </v-tab-item>
        </v-tabs>
    </div>
</template>
<script>
export default {
    data() {
        return {
            headers: [
                { text: "Property", value: "name" },
                { text: "Value", value: "value" }
            ],
            lastCacheRefresh: 0,
            status: "unknown",
            clusters: []
        };
    },
    created() {
        this.$request.get("api/cache/info").then(
            cacheInfo => {
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                let clusters = [];
                for (let clusterInfo in cacheInfo.clusters) {
                    let computedConfiguration = [];
                    for (let keyValue in clusterInfo.bookkeeperConfiguration) {
                        computedConfiguration.push({
                            name: keyValue,
                            value: clusterInfo.bookkeeperConfiguration[keyValue]
                        });
                    }
                    var cluster = {
                        clusterName: clusterInfo.clusterName,
                        bookkeeperConfiguration: clusterInfo.bookkeeperConfiguration,
                        auditor: clusterInfo.auditor,
                        autorecoveryEnabled: clusterInfo.autorecoveryEnabled,
                        lostBookieRecoveryDelay: clusterInfo.lostBookieRecoveryDelay,
                        layoutFormatVersion: clusterInfo.layoutFormatVersion,
                        layoutManagerFactoryClass: clusterInfo.layoutManagerFactoryClass,
                        layoutManagerVersion: clusterInfo.layoutManagerVersion,
                        computedBookkeeperConfiguration: computedConfiguration
                    };
                    clusters.push(cluster);
                }
                this.clusters = clusters;
                this.pageLoaded = true;
            }
        );
    },
    methods: {
        refreshCache() {
            this.$request.get("api/cache/refresh").then(
                cacheInfo => {
                    this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                    this.status = cacheInfo.status;
                    let clusters = [];
                    for (let clusterInfo in cacheInfo.clusters) {
                        let computedConfiguration = [];
                        for (let keyValue in clusterInfo.bookkeeperConfiguration) {
                            computedConfiguration.push({
                                name: keyValue,
                                value: clusterInfo.bookkeeperConfiguration[keyValue]
                            });
                        }
                        var cluster = {
                            clusterName: clusterInfo.clusterName,
                            bookkeeperConfiguration: clusterInfo.bookkeeperConfiguration,
                            auditor: clusterInfo.auditor,
                            autorecoveryEnabled: clusterInfo.autorecoveryEnabled,
                            lostBookieRecoveryDelay: clusterInfo.lostBookieRecoveryDelay,
                            layoutFormatVersion: clusterInfo.layoutFormatVersion,
                            layoutManagerFactoryClass: clusterInfo.layoutManagerFactoryClass,
                            layoutManagerVersion: clusterInfo.layoutManagerVersion,
                            computedBookkeeperConfiguration: computedConfiguration
                        };
                        clusters.push(cluster);
                    }
                    this.clusters = clusters;
                });
        },
        refreshPage() {
            this.$request.get("api/cache/info").then(
                cacheInfo => {
                    this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                    this.status = cacheInfo.status;
                    let clusters = [];
                    for (let clusterInfo in cacheInfo.clusters) {
                        let computedConfiguration = [];
                        for (let keyValue in clusterInfo.bookkeeperConfiguration) {
                            computedConfiguration.push({
                                name: keyValue,
                                value: clusterInfo.bookkeeperConfiguration[keyValue]
                            });
                        }
                        var cluster = {
                            clusterName: clusterInfo.clusterName,
                            bookkeeperConfiguration: clusterInfo.bookkeeperConfiguration,
                            auditor: clusterInfo.auditor,
                            autorecoveryEnabled: clusterInfo.autorecoveryEnabled,
                            lostBookieRecoveryDelay: clusterInfo.lostBookieRecoveryDelay,
                            layoutFormatVersion: clusterInfo.layoutFormatVersion,
                            layoutManagerFactoryClass: clusterInfo.layoutManagerFactoryClass,
                            layoutManagerVersion: clusterInfo.layoutManagerVersion,
                            computedBookkeeperConfiguration: computedConfiguration
                        };
                        clusters.push(cluster);
                    }
                    this.clusters = clusters;
                });
        }
    }
};
</script>
