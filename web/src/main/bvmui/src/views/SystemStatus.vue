<template>
    <div class="bvm-bookie">
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
