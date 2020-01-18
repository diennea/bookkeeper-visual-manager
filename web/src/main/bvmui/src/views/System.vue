<template>
    <div class="bvm-system">
        <div class="mb-1">
            <span class="caption">
                BookKeeper Visual Manager caches data on a local database in
                order to save resources on the Metadata Service (ZooKeeper).
            </span>
        </div>
        <v-tabs
            v-model="tab"
            fixed-tabs
            background-color="blue lighten-1" dark>
            <v-tab>Cache</v-tab>
            <v-tab>Configuration</v-tab>
        </v-tabs>
        <v-tabs-items v-model="tab" class="mt-2">
            <v-tab-item>
                <v-row class="mb-2" no-gutters>
                   <span>Last cache refresh: <b>{{ new Date(lastCacheRefresh) }}</b></span>
                </v-row>
                <v-row align="center" no-gutters>
                    <v-col class="shrink">
                        <v-btn
                            depressed
                            large
                            tile
                            color="blue lighten-1 white--text"
                            @click="refreshCache">
                            Refresh Cache
                        </v-btn>
                    </v-col>
                    <v-col class="ml-3">
                        <span>Cache status <b>{{ status }}</b> (<a @click="refreshPage" style="cursor: pointer;">Refresh</a>)</span>
                    </v-col>
                </v-row>
            </v-tab-item>
            <v-tab-item>
                <v-data-table
                    :headers="headers"
                    :items="computedBookkeeperConfiguration"
                    :sort-by="['calories', 'fat']"
                    :sort-desc="[false, true]"
                    multi-sort
                    hide-default-footer
                    class="elevation-1 mb-5"
                ></v-data-table>
            </v-tab-item>
        </v-tabs-items>
    </div>
</template>
<script>
export default {
    data() {
        return {
            tab: null,
            headers: [
                { text: "Name", value: "name" },
                { text: "Value", value: "value" }
            ],
            desserts: [],
            lastCacheRefresh: 0,
            status: "unknown",
            bookkeeperConfiguration: {}
        };
    },
    methods: {
        refreshCache() {
            this.$request.get("api/cache/refresh").then(cacheInfo => {
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                this.bookkeeperConfiguration =
                    cacheInfo.bookkeeperConfiguration;
            });
        },
        refreshPage() {
            this.$request.get("api/cache/info").then(cacheInfo => {
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                this.bookkeeperConfiguration =
                    cacheInfo.bookkeeperConfiguration;
            });
        }
    },
    computed: {
        computedBookkeeperConfiguration() {
            if (!this.bookkeeperConfiguration) {
                return [];
            }
            let conf = [];
            for (let keyValue in this.bookkeeperConfiguration) {
                conf.push({
                    name: keyValue,
                    value: this.bookkeeperConfiguration[keyValue]
                });
            }
            return conf;
        }
    },
    created() {
        this.$request.get("api/cache/info").then(cacheInfo => {
            this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
            this.status = cacheInfo.status;
            this.bookkeeperConfiguration = cacheInfo.bookkeeperConfiguration;
        });
    }
};
</script>
