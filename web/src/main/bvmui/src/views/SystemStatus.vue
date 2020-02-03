<template>
    <div class="bvm-bookie">
        <div v-if="pageLoaded">            
            
        
            <v-tabs
                background-color="light-blue"
                center-active
                dark>
                <v-tab>Cluster Status on ZooKeeper</v-tab>
                <v-tab>Client Configuration</v-tab>
                <v-tab>Cache Status</v-tab>
                <v-tab-item>
                    <v-simple-table>
                        <template v-slot:default>
                            <thead>
                                <tr>
                                <th class="text-left">Property</th>
                                <th class="text-left">Value</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr> <td>autorecoveryEnabled</td> <td>{{autorecoveryEnabled}}</td></tr>
                                <tr> <td>Auditor</td> <td>{{auditor}}</td></tr>
                                <tr> <td>lostBookieRecoveryDelay</td> <td>{{lostBookieRecoveryDelay}}</td></tr>
                                <tr> <td>layoutManagerFactoryClass</td> <td>{{layoutManagerFactoryClass}}</td></tr>
                                <tr> <td>layoutFormatVersion</td> <td>{{layoutFormatVersion}}</td></tr>
                                <tr> <td>layoutManagerVersion</td> <td>{{layoutManagerVersion}}</td></tr>
                            </tbody>
                        </template>
                    </v-simple-table>
                </v-tab-item>
    
                <v-tab-item>
                    <p>Client configuration used by this BookKeeper Visual Manager instance</p>
                    <v-textarea v-model="bookkeeperConfiguration" readonly />            
                </v-tab-item>

                <v-tab-item>
                    <p>
                        BookKeeper Visual Manager caches data on a local database
                        in order to save resources on the Metadata Service (ZooKeeper).
                        You have to manually request a reload from ZooKeeper.
                    </p>
                    <v-tooltip bottom>
                        <template v-slot:activator="{ on }">
                            <span v-on="on">Background worked status <b>{{status}}</b></span>
                        </template>
                        <span>Last reload from ZooKeeper was at <b>{{new Date(lastCacheRefresh)}}</b></span>
                    </v-tooltip>
                    <v-divider />
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
                </v-tab-item>
            </v-tabs>
        </div>
        <Spinner v-else/>
    </div>
</template>
<script>
import Spinner from "@/components/Spinner"
export default {
    components: {
        Spinner
    },
    data() {
        return {
            pageLoaded: false,
            lastCacheRefresh: 0,
            status: "unknown",
            bookkeeperConfiguration: "",
            auditor: "",
            autorecoveryEnabled: false,
            lostBookieRecoveryDelay: 0,
            layoutFormatVersion: -1,
            layoutManagerFactoryClass: "",
            layoutManagerVersion: -1
        };
    },
    methods: {
        refreshCache() {
            this.$request.get(
            "api/cache/refresh",
            cacheInfo => {
                this.pageLoaded = true;
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                this.bookkeeperConfiguration = cacheInfo.bookkeeperConfiguration;
                this.auditor = cacheInfo.auditor;
                this.autorecoveryEnabled = cacheInfo.autorecoveryEnabled;
                this.lostBookieRecoveryDelay = cacheInfo.lostBookieRecoveryDelay;
                this.layoutFormatVersion = cacheInfo.layoutFormatVersion;
                this.layoutManagerFactoryClass = cacheInfo.layoutManagerFactoryClass;
                this.layoutManagerVersion = cacheInfo.layoutManagerVersion;
            },
            error => {
                this.$router.push({
                    name: "error"
                });
            }
        );
        },
        refreshPage() {
            this.$request.get(
            "api/cache/info",
            cacheInfo => {
                this.pageLoaded = true;
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                this.bookkeeperConfiguration = cacheInfo.bookkeeperConfiguration;
                this.auditor = cacheInfo.auditor;
                this.autorecoveryEnabled = cacheInfo.autorecoveryEnabled;
                this.lostBookieRecoveryDelay = cacheInfo.lostBookieRecoveryDelay;
                this.layoutFormatVersion = cacheInfo.layoutFormatVersion;
                this.layoutManagerFactoryClass = cacheInfo.layoutManagerFactoryClass;
                this.layoutManagerVersion = cacheInfo.layoutManagerVersion;
            },
            error => {
                this.$router.push({
                    name: "error"
                });
            }        
        );
        }
    },
    created() {
        this.$request.get(
            "api/cache/info",
            cacheInfo => {
                this.pageLoaded = true;
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                this.bookkeeperConfiguration = cacheInfo.bookkeeperConfiguration;
                this.auditor = cacheInfo.auditor;
                this.autorecoveryEnabled = cacheInfo.autorecoveryEnabled;
                this.lostBookieRecoveryDelay = cacheInfo.lostBookieRecoveryDelay;
                this.layoutFormatVersion = cacheInfo.layoutFormatVersion;
                this.layoutManagerFactoryClass = cacheInfo.layoutManagerFactoryClass;
                this.layoutManagerVersion = cacheInfo.layoutManagerVersion;
            },
            error => {
                this.$router.push({
                    name: "error"
                });
            }
        );
    }
};
</script>

