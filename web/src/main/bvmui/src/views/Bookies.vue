<template>
    <v-container >
        <v-row v-if="bookies.length > 0">
            <v-col cols="2" justify="start" >
            <v-select
                v-model="viewMode"
                :items="[{text: 'Grid', value: 'grid'}, {text: 'Topology', value: 'topology'}]"
                label="View"
                color="primary"
                class="my-1"
                outlined
                dense
            />
            </v-col>
            <v-col cols="2" justify="start" >
                <v-select
                    v-model="size"
                    :items="[4, 8, 16, 32]"
                    label="Show bookies"
                    color="primary"
                    class="my-1"
                    outlined
                    dense
                />
            </v-col>
            <v-col cols="2" >
                <v-pagination
                    v-show="pageLength > 1"
                    v-model="page"
                    :length="pageLength"
                    color="primary"
                />
            </v-col>
        </v-row>
    <v-dialog v-if="currentBookie" v-model="dialogInfo" max-width="500px">
            <v-card>
                <v-card-title>{{ currentBookie.bookieId }}</v-card-title>
                <v-card-subtitle>{{ currentBookie.clusterName }}</v-card-subtitle>
                <v-card-text>
                    <v-tabs
                        color="primary"
                        center-active>
                        <v-tab>Disk usage</v-tab>
                        <v-tab>Endpoints</v-tab>
                        <v-tab>Properties</v-tab>
                        <v-tab-item>
                            <v-list class="transparent">
                                <v-list-item>
                                    <v-list-item-title>Usage</v-list-item-title>
                                    <v-list-item-subtitle class="text-right">{{ $library.formatPercent(currentBookie.totalDiskSpace - currentBookie.freeDiskSpace, currentBookie.totalDiskSpace) }}&#37;</v-list-item-subtitle>
                                </v-list-item>
                                <v-list-item>
                                    <v-list-item-title>Free space</v-list-item-title>
                                    <v-list-item-subtitle class="text-right">{{ $library.formatBytes(currentBookie.freeDiskSpace) }}</v-list-item-subtitle>
                                </v-list-item>
                                <v-list-item>
                                    <v-list-item-title>Total space</v-list-item-title>
                                    <v-list-item-subtitle class="text-right">{{ $library.formatBytes(currentBookie.totalDiskSpace) }}</v-list-item-subtitle>
                                </v-list-item>
                            </v-list>
                        </v-tab-item>
                        <v-tab-item>
                            <v-simple-table class="mt-2 elevation-1">
                                <template #default>
                                    <tbody>
                                        <tr v-for="(endpoint, index) in currentBookie.endpoints" :key="index">
                                            <td>{{ index }}</td>
                                            <td>{{ endpoint }}</td>
                                        </tr>
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
                                :items="convertProperties(currentBookie.properties)"
                                hide-default-footer
                                class="mt-2 elevation-1"
                            />
                        </v-tab-item>
                    </v-tabs>
                </v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn color="primary" text @click="dialogInfo = false">Close</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-dialog v-if="currentBookie" v-model="gcInfo" max-width="500px">
            <v-card>
                <v-card-title>
                    <span class="headline">Bookie Info: {{ currentBookie.bookieId }} {{ currentBookie.clusterName }}</span>
                </v-card-title>
                <v-layout align-center justify-center column fill-height v-if="gcDetails === null">
                    <v-flex row align-center>
                        <v-progress-circular
                            :size="70"
                            :width="7"
                            color="primary"
                            indeterminate
                        />
                    </v-flex>
                </v-layout>
                <v-card-text v-else>
                    <v-tabs
                        color="primary"
                        center-active>
                        <v-tab>Garbage Collector</v-tab>
                        <v-tab-item>
                            <v-simple-table class="mt-2 elevation-1">
                                <template #default>
                                    <tbody>
                                        <tr v-for="(value, name, index) in gcDetails" :key="index">
                                            <td>{{ name }}</td>
                                            <td>{{ value }}</td>
                                        </tr>
                                    </tbody>
                                </template>
                            </v-simple-table>
                        </v-tab-item>
                    </v-tabs>
                </v-card-text>
                <v-card-actions>
                    <v-spacer />
                    <v-btn color="primary" text @click="gcInfo = false">Close</v-btn>
                    <v-btn color="primary" text @click="getGcDetails(currentBookie)">Refresh</v-btn>
                    <v-btn color="primary" text @click="confirmGC = true">Trigger GC</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-dialog v-if="currentBookie" v-model="confirmGC" max-width="290">
            <v-card>
                <v-card-title class="text-h5">
                  TRIGGER GC
                </v-card-title>
                <v-layout align-center justify-center column fill-height v-if="triggerStatus">
                    <v-flex row align-center>
                        <v-progress-circular
                            :size="70"
                            :width="7"
                            color="primary"
                            indeterminate
                        />
                    </v-flex>
                </v-layout>
                <v-card-text v-if="gcTriggerResult != null">
                    {{ currentDateTime() }} :  {{ gcTriggerResult }}
                </v-card-text>
                <v-card-text v-else>
                    Are you sure you want to proceed?
                </v-card-text>
              <v-card-actions>
                <v-btn
                  color="green darken-1"
                  text
                  :disabled="gcTriggerResult === null"
                  @click="closeGC"
                >
                  Close
                </v-btn>
                <v-btn
                  color="green darken-1"
                  text
                  :disabled="gcTriggerResult != null"
                  @click="triggerGC(currentBookie)"
                >
                  Confirm
                </v-btn>
              </v-card-actions>
            </v-card>
        </v-dialog>
        <v-row justify="start">
        <Bookie
            v-for="bookie in bookiesList"
            :bookie="bookie"
            :key="bookie.bookieId"
            @click="openBookie(bookie)"
            @click-info="openBookieInfo(bookie)"
            @click-gc="openGC(bookie)"
        />
        <BookiesTopology v-if="viewMode === 'topology'" :bookies="bookies" :bookiesTopology="bookiesTopology"
            @click="openBookie"
            @click-info="openBookieInfo"
            @click-gc="openGC"
        />
        </v-row>

    </v-container>
</template>
<script>
const DefaultPageSize = 32;
import Bookie from "@/components/Bookie";
import BookiesTopology from "@/components/BookiesTopology";
import qs from 'query-string';

export default {
    components: {
        Bookie,
        BookiesTopology
    },
    data() {
        return {
            bookiesTopology: {},
            page: 1,
            size: DefaultPageSize,
            bookies: [],
            bookiesCount: 0,
            currentBookie: null,
            dialogInfo: false,
            gcInfo: false,
            gcDetails: null,
            gcTriggerResult: null,
            triggerStatus: false,
            confirmGC: false,
            viewMode: 'grid'
        };
    },
    computed: {
        bookiesList() {
            if (this.viewMode !== 'grid') {
                return [];
            }
            return this.bookies
        },
        pageLength() {
            return Math.ceil(this.bookiesCount / this.size);
        },
    },
    watch: {
        async page(newPageValue) {
            return this.refreshBookies(newPageValue, this.size);
        },
        async size(newSizeValue) {
            this.page = 1;
            return this.refreshBookies(this.page, newSizeValue);
        }
    },
    async created() {
        this.refreshBookies();
    },
    methods: {
        async refreshBookies() {
            this.bookiesTopology = (await this.$request.get(`api/topology/all`)).bookies;
            const queryParameters = qs.stringify({page: this.page, size: this.size});
            const bookieResponse = await this.$request.get(`api/bookie/all?${queryParameters}`)
            this.bookies = bookieResponse.bookies;
            this.bookiesCount = bookieResponse.totalBookies;
        },

        async getGcDetails(bookie){
            const clusterId = bookie.clusterId;
            const bookieId = bookie.bookieId;
            const queryParameters = qs.stringify({clusterId, bookieId});
            const url = `api/gc/details?${queryParameters}`;
            const bookieResponse = await this.$request.get(url);
            if(bookieResponse.statusCode === 200) {
                this.gcDetails = JSON.parse(bookieResponse.message)[0];
            }
        },
        async triggerGC(bookie){
            const clusterId = bookie.clusterId;
            const bookieId = bookie.bookieId;
            const queryParameters = qs.stringify({clusterId, bookieId});
            const url = `api/gc/trigger?${queryParameters}`;
            const bookieResponse = await this.$request.get(url);
            this.gcTriggerResult = this.$library.removeDoubleQuote(bookieResponse.message);
        },
        openBookie(bookie) {
            const { clusterId, bookieId } = bookie;
            this.$router.push({
                name: "bookie-ledgers",
                params: { clusterId, bookieId }
            });
        },
        openBookieInfo(bookie) {
            this.currentBookie = bookie;
            this.dialogInfo = true;
        },
        openGC(bookie){
            this.getGcDetails(bookie);
            this.currentBookie = bookie;
            this.gcInfo = true;
        },
        closeGC(){
            this.gcTriggerResult = null;
            this.triggerStatus = false;
            this.confirmGC = false;
        },
        keyBookie(bookie) {
            return `${bookie.clusterId}|${bookie.bookieId}`;
        },
        convertProperties(properties) {
            let resultProperties = [];
            for (let key in properties) {
                resultProperties.push({
                    name: key,
                    value: properties[key]
                });
            }
            return resultProperties;
        },
        currentDateTime() {
            return new Date().toLocaleString();
        }
    }
};
</script>
