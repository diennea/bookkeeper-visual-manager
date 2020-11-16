<template>
    <div class="bvm-clusters">
        <v-dialog v-model="dialog" persistent max-width="700px">
            <template v-slot:activator="{ on, attrs }">
                <v-btn
                    depressed
                    large
                    tile
                    class="mt-1"
                    color="blue lighten-1 white--text"
                    v-bind="attrs"
                    v-on="on">
                    Insert
                </v-btn>
            </template>
            <v-card>
                <v-card-title>
                    <span class="headline">Add cluster</span>
                </v-card-title>
                <v-card-text>
                    <v-container>
                        <v-row>
                            <v-col cols="4">
                                <v-text-field
                                    v-model="newClusterInfo.name"
                                    label="Name"
                                    required
                                    dense
                                />
                            </v-col>
                            <v-col cols="8">
                                <v-text-field
                                    v-model="newClusterInfo.metadataServiceUri"
                                    label="Metadata service URI"
                                    required
                                    dense
                                />
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="12">
                                <v-textarea
                                    v-model="newClusterInfo.configuration"
                                    label="Configuration"
                                    hint="Properties file passed as configuration to the client connection"
                                />
                            </v-col>
                        </v-row>
                    </v-container>
                </v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn color="blue lighten-1" text @click="closeDialog">Close</v-btn>
                    <v-btn color="blue lighten-1" text @click="addCluster">Add</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-dialog v-if="selectedClusterInfo" v-model="dialogInfo" persistent max-width="700px">
            <v-card>
                <v-card-title>
                    <span class="headline">Cluster {{ selectedClusterInfo.clusterName }}</span>
                </v-card-title>
                <v-card-text>
                    <v-tabs
                        color="blue lighten-1"
                        center-active>
                        <v-tab>Status on ZooKeeper</v-tab>
                        <v-tab>Clients Configuration</v-tab>
                        <v-tab-item>
                            <v-simple-table class="mt-2 elevation-1">
                                <template v-slot:default>
                                    <thead>
                                        <tr>
                                            <th class="text-left">Property</th>
                                            <th class="text-left">Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr> <td>autorecoveryEnabled</td> <td>{{ selectedClusterInfo.autorecoveryEnabled }}</td></tr>
                                        <tr> <td>Auditor</td> <td>{{ selectedClusterInfo.auditor }}</td></tr>
                                        <tr> <td>lostBookieRecoveryDelay</td> <td>{{ selectedClusterInfo.lostBookieRecoveryDelay }}</td></tr>
                                        <tr> <td>layoutManagerFactoryClass</td> <td>{{ selectedClusterInfo.layoutManagerFactoryClass }}</td></tr>
                                        <tr> <td>layoutFormatVersion</td> <td>{{ selectedClusterInfo.layoutFormatVersion }}</td></tr>
                                        <tr> <td>layoutManagerVersion</td> <td>{{ selectedClusterInfo.layoutManagerVersion }}</td></tr>
                                    </tbody>
                                </template>
                            </v-simple-table>
                        </v-tab-item>
                        <v-tab-item>
                            <v-data-table
                                :headers="[
                                    { text: 'Property', value: 'name' },
                                    { text: 'Value', value: 'value' }
                                ]"
                                :items="selectedClusterInfo.computedBookkeeperConfiguration"
                                multi-sort
                                hide-default-footer
                                class="mt-2 elevation-1"
                            />
                        </v-tab-item>
                    </v-tabs>
                </v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn color="blue lighten-1" text @click="dialogInfo = false">Close</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-data-table
            :headers="headers"
            :items="clusters"
            multi-sort
            hide-default-footer
            class="mt-2 elevation-1"
            @click:row="showCluster">
            <template v-slot:item.actions="{ item }">
                <v-icon @click.stop="deleteCluster(item)">
                    mdi-delete
                </v-icon>
            </template>
        </v-data-table>
    </div>
</template>
<script>
export default {
    data() {
        return {
            dialog: false,
            dialogInfo: false,
            clusters: [],
            clustersInfo: [],
            newClusterInfo: {
                name: '',
                metadataServiceUri: '',
                configuration: ''
            },
            selectedClusterInfo: null
        }
    },
    computed: {
        headers() {
            return [
                { text: 'Name', value: 'name' },
                { text: 'Metadata Service Uri', width: '70%', value: 'metadataServiceUri', sortable: false },
                { text: 'Actions', width: '10%', value: 'actions', align: 'center', sortable: false },
            ]
        },
        clusterNames() {
            return this.clusters.map(cluster => cluster.name);
        }
    },
    created() {
        this.refreshClusters();
    },
    methods: {
        closeDialog() {
            this.dialog = false;
            this.newClusterInfo.name = '';
            this.newClusterInfo.metadataServiceUri = '';
            this.newClusterInfo.configuration = '';
        },
        addCluster() {
            this.$request.post("api/cluster/add", this.newClusterInfo)
                .then(() => {
                    this.closeDialog();
                    this.refreshClusters();
                });
        },
        deleteCluster({ clusterId }) {
            this.$request.post(`api/cluster/delete/${clusterId}`)
                .then(() => {
                    this.refreshClusters()
                });
        },
        showCluster({ clusterId }) {
            this.dialogInfo = true;
            this.selectedClusterInfo = this.clustersInfo.find(c => c.clusterId === clusterId);
        },
        async refreshClusters() {
            this.clusters = await this.$request.get("api/cluster/all");
            let cacheInfo = await this.$request.get("api/cache/info");
            this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
            this.status = cacheInfo.status;
            let clustersInfo = [];
            for (let clusterInfo of cacheInfo.clusters) {
                let computedConfiguration = [];
                for (let keyValue in clusterInfo.bookkeeperConfiguration) {
                    computedConfiguration.push({
                        name: keyValue,
                        value: clusterInfo.bookkeeperConfiguration[keyValue]
                    });
                }
                var cluster = {
                    clusterId: clusterInfo.clusterId,
                    clusterName: clusterInfo.clusterName,
                    auditor: clusterInfo.auditor,
                    autorecoveryEnabled: clusterInfo.autorecoveryEnabled,
                    lostBookieRecoveryDelay: clusterInfo.lostBookieRecoveryDelay,
                    layoutFormatVersion: clusterInfo.layoutFormatVersion,
                    layoutManagerFactoryClass: clusterInfo.layoutManagerFactoryClass,
                    layoutManagerVersion: clusterInfo.layoutManagerVersion,
                    bookkeeperConfiguration: clusterInfo.bookkeeperConfiguration,
                    computedBookkeeperConfiguration: computedConfiguration
                };
                clustersInfo.push(cluster);
            }
            this.clustersInfo = clustersInfo;
            this.$store.commit('showDrawer', this.clusters.length > 0);
        }
    }
};
</script>
