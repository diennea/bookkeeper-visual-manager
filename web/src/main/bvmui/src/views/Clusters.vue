<template>
    <div class="bvm-clusters">
        <v-dialog v-model="dialog" persistent max-width="600px">
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
                            <v-col cols="12">
                                <v-text-field
                                    v-model="clusterInfo.name"
                                    label="Name"
                                    required
                                    dense
                                />
                            </v-col>
                        </v-row>
                        <v-row>
                            <v-col cols="12">
                                <v-text-field
                                    v-model="clusterInfo.metadataServiceUri"
                                    label="Metadata service URI"
                                    hint="example of helper text only on focus"
                                    required
                                    dense
                                />
                            </v-col>
                        </v-row>
                    </v-container>
                </v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn color="blue darken-1" text @click="dialog = false">Close</v-btn>
                    <v-btn color="blue darken-1" text @click="addCluster">Add</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
         <v-select
            :value="$store.getters.clusterName"
            @change="changeCluster"
            :items="clusterNames"
            label="Select the cluster"
            dense
            solo
        ></v-select>
        <v-data-table
            :headers="headers"
            :items="clusters"
            multi-sort
            hide-default-footer
            class="mt-2 elevation-1">
            <template v-slot:item.actions="{ item }">
                <v-icon @click="deleteCluster(item)">
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
            clusters: [],
            clusterInfo: {
                name: '',
                metadataServiceUri: ''
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
    methods: {
        addCluster() {
            this.$request.post("api/cluster/add", {
                name: this.clusterInfo.name,
                metadataServiceUri: this.clusterInfo.metadataServiceUri,
            }).then(() => {
                this.dialog = false;
                this.clusterInfo.name = '';
                this.clusterInfo.metadataServiceUri = '';
                this.refreshClusters();
            })
        },
        deleteCluster({name}) {
            this.$request.post(`api/cluster/delete/${name}`)
                .then(() => this.refreshClusters());

        },
        changeCluster(name) {
            this.$store.commit('updateCluster', { name })
        },
        async refreshClusters() {
            this.clusters = await this.$request.get("api/cluster/all");
        }
    },
    created() {
        this.refreshClusters();
    }
};
</script>
