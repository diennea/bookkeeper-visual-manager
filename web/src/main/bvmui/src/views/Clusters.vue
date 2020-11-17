<template>
    <div class="bvm-clusters">
        <v-dialog v-model="dialogCreate" max-width="700px">
            <template #activator="{ on, attrs }">
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
        <v-dialog v-if="currentCluster" v-model="dialogInfo" max-width="700px">
            <v-card>
                <v-card-title>
                    <span class="headline">Cluster {{ currentCluster.clusterName }}</span>
                </v-card-title>
                <v-card-text>
                    <v-tabs
                        color="blue lighten-1"
                        center-active>
                        <v-tab>Status on ZooKeeper</v-tab>
                        <v-tab>Clients Configuration</v-tab>
                        <v-tab-item>
                            <v-simple-table class="mt-2 elevation-1">
                                <template #default>
                                    <thead>
                                        <tr>
                                            <th class="text-left">Property</th>
                                            <th class="text-left">Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr> <td>autorecoveryEnabled</td> <td>{{ currentCluster.autorecoveryEnabled }}</td></tr>
                                        <tr> <td>Auditor</td> <td>{{ currentCluster.auditor }}</td></tr>
                                        <tr> <td>lostBookieRecoveryDelay</td> <td>{{ currentCluster.lostBookieRecoveryDelay }}</td></tr>
                                        <tr> <td>layoutManagerFactoryClass</td> <td>{{ currentCluster.layoutManagerFactoryClass }}</td></tr>
                                        <tr> <td>layoutFormatVersion</td> <td>{{ currentCluster.layoutFormatVersion }}</td></tr>
                                        <tr> <td>layoutManagerVersion</td> <td>{{ currentCluster.layoutManagerVersion }}</td></tr>
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
                                :items="currentCluster.computedBookkeeperConfiguration"
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
        <v-dialog v-model="dialogDelete" persistent max-width="290">
            <v-card>
                <v-card-title class="headline">
                Delete Cluster
                </v-card-title>
                <v-card-text>Are you really sure you want to delete the Bookkeeper cluster?</v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn
                        color="blue lighten-1"
                        text
                        @click="dialogDelete = false">No</v-btn>
                    <v-btn
                        color="red darken-1"
                        text
                        @click="deleteCluster(deleteClusterInfo.clusterId)">Yes</v-btn>
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
            <template #item.actions="{ item }">
                <v-icon @click.stop="promptDeleteCluster(item)">
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
            dialogInfo: false,
            dialogCreate: false,
            dialogDelete: false,
            clusters: [],
            clustersInfo: [],
            currentCluster: null,
            newClusterInfo: {
                name: '',
                metadataServiceUri: '',
                configuration: ''
            },
            deleteClusterInfo: {
                clusterId: 0
            }
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
            this.dialogCreate = false;
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
        promptDeleteCluster(clusterId) {
            this.dialogDelete = true;
            this.deleteClusterInfo.clusterId = clusterId;
        },
        deleteCluster({ clusterId }) {
            this.$request.post(`api/cluster/delete/${clusterId}`)
                .then(() => {
                    this.refreshClusters()
                }).finally(() => this.dialogDelete = false);
        },
        showCluster({ clusterId }) {
            this.dialogInfo = true;
            this.currentCluster = this.clustersInfo.find(c => c.clusterId === clusterId);
        },
        async refreshClusters() {
            this.clusters = await this.$request.get("api/cluster/all");
            const clusterStatus = await this.$request.get("api/cluster/status");

            let clustersInfo = [];
            for (let status of clusterStatus) {
                let computedBookkeeperConfiguration = [];

                let bookkeeperConfiguration = status.bookkeeperConfiguration;
                for (let key in bookkeeperConfiguration) {
                    computedBookkeeperConfiguration.push({
                        name: key,
                        value: bookkeeperConfiguration[key]
                    });
                }
                var cluster = {
                    ...status,
                    computedBookkeeperConfiguration
                };
                clustersInfo.push(cluster);
            }
            this.clustersInfo = clustersInfo;
            this.$store.commit('showDrawer', this.clusters.length > 0);
        }
    }
};
</script>
