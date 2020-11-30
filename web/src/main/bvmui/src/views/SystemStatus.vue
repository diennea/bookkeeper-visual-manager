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
            color="green lighten-1 white--text"
            @click="refreshCache"
            :loading="status === 'WORKING'">
            Reload metadata from ZooKeeper
        </v-btn>
        <p class="my-2">
            Background worker status <b>{{ status }}</b>
        </p>
        <p v-if="lastCacheRefresh" class="caption my-2">
            Last reload from ZooKeeper was at <b>{{ new Date(lastCacheRefresh) }}</b>
        </p>
        <p v-if="metadataRefreshPeriod && metadataRefreshPeriod > 0" class="caption my-2">
            Automatic refresh period <b>{{ metadataRefreshPeriod }} seconds</b>.
        </p>
        <p v-else class="caption my-2">
            Automatic refresh period <b>disabled</b>.
        </p>
    </div>
</template>
<script>
const StatusMode = {
    IDLE: 'IDLE',
    WORKING: 'WORKING'
}
const RefreshRate = 2000;
export default {
    data() {
        return {
            status: "unknown",
            lastCacheRefresh: 0,
            metadataRefreshPeriod: 0
        };
    },
    created() {
        this.refreshPage();
    },
    beforeDestroy() {
        if (this.interval) {
            clearInterval(this.interval);
        }
    },
    methods: {
        refreshCache() {
            this.$request.get("api/cache/refresh").then(
                cacheInfo => {
                    this.status = cacheInfo.status;
                    this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                    this.metadataRefreshPeriod = cacheInfo.metadataRefreshPeriod;
                }
            );
            this.interval = setInterval(this.refreshPage, RefreshRate);
        },
        refreshPage() {
            this.$request.get("api/cache/info").then(
                cacheInfo => {
                    this.status = cacheInfo.status;
                    this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                    this.metadataRefreshPeriod = cacheInfo.metadataRefreshPeriod;

                    switch (this.status) {
                        case StatusMode.IDLE:
                            if (this.interval) {
                                clearInterval(this.interval);
                            }
                            break;
                        case StatusMode.WORKING:
                            if (!this.interval) {
                                this.interval = setInterval(this.refreshPage, RefreshRate);
                            }
                            break;
                    }
                }
            );
        }
    }
};
</script>
