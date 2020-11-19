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
                    <span class="headline">{{ editClusterInfo.clusterId > 0 ? 'Edit cluster' : 'Add cluster' }}</span>
                </v-card-title>
                <v-card-text>
                    <v-container>
                        <v-row>
                            <v-col cols="4">
                                <v-text-field
                                    v-model="editClusterInfo.name"
                                    label="Name"
                                    required
                                    dense
                                />
                            </v-col>
                            <v-col cols="8">
                                <v-text-field
                                    v-model="editClusterInfo.metadataServiceUri"
                                    label="Metadata service URI"
                                    required
                                    :disabled="editClusterInfo.clusterId > 0"
                                    dense
                                />
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="12">
                                <v-textarea
                                    v-model="editClusterInfo.configuration"
                                    :disabled="editClusterInfo.clusterId > 0"
                                    label="Configuration"
                                    hint="Properties file passed as configuration to the client connection"
                                />
                            </v-col>
                        </v-row>
                    </v-container>
                </v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn depressed color="blue lighten-1" text @click="closeEdit">Close</v-btn>
                    <v-btn depressed color="blue lighten-1" text @click="editCluster" :loading="loading">{{ editClusterInfo.clusterId > 0 ? 'Edit' : 'Add' }}</v-btn>
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
        <v-dialog v-model="dialogDelete" persistent max-width="350">
            <v-card>
                <v-card-title class="headline">
                Delete Cluster {{ deleteClusterInfo.name }}
                </v-card-title>
                <v-card-text>Are you really sure you want to delete the Bookkeeper cluster?</v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn color="blue lighten-1" text @click="closeDelete">No</v-btn>
                    <v-btn color="red darken-1" text @click="deleteCluster(deleteClusterInfo)" :loading="loading">Delete</v-btn>
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
                <v-icon @click.stop="promptEditCluster(item)">
                    mdi-pencil
                </v-icon>
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
            loading: false,
            dialogInfo: false,
            dialogCreate: false,
            dialogDelete: false,
            clusters: [],
            clustersInfo: [],
            currentCluster: null,
            editClusterInfo: {
                clusterId: 0,
                name: '',
                metadataServiceUri: '',
                configuration: ''
            },
            deleteClusterInfo: {
                clusterId: 0,
                name: ''
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
        closeEdit() {
            this.dialogCreate = false;
            this.editClusterInfo.clusterId = 0;
            this.editClusterInfo.name = '';
            this.editClusterInfo.metadataServiceUri = '';
            this.editClusterInfo.configuration = '';
        },
        editCluster() {
            const newCluster = this.editClusterInfo.clusterId === 0;
            const url = newCluster ? "api/cluster/add" : "api/cluster/edit";

            this.loading = true;
            this.$request.post(url, this.editClusterInfo)
                .then(() => {
                    this.refreshClusters();
                    if (newCluster) {
                        this.$store.commit('incrementClusterCount');
                    }
                }).finally(() => {
                    this.closeEdit();
                    this.loading = false;
                });
        },
        promptEditCluster(item) {
            this.dialogCreate = true;
            this.editClusterInfo.clusterId = item.clusterId;
            this.editClusterInfo.name = item.name;
            this.editClusterInfo.metadataServiceUri = item.metadataServiceUri;
            this.editClusterInfo.configuration = item.configuration;
        },
        promptDeleteCluster(item) {
            this.dialogDelete = true;
            this.deleteClusterInfo.clusterId = item.clusterId;
            this.deleteClusterInfo.name = item.name;
        },
        closeDelete() {
            this.dialogDelete = false;
            this.deleteClusterInfo.clusterId = 0;
            this.deleteClusterInfo.name = '';
        },
        deleteCluster({ clusterId }) {
            this.loading = true;
            this.$request.post(`api/cluster/delete/${clusterId}`)
                .then(() => {
                    this.refreshClusters()
                    this.$store.commit('decrementClusterCount');
                }).finally(() => {
                    this.closeDelete();
                    this.loading = false;
                });
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
