<template>
    <div class="bvm-bookie">
        <div v-if="pageLoaded">
            BookKeeper Visual Manager caches data on a local database in order to save resources on the Metadata Service (ZooKeeper).
            <br>
            Last cache refresh was at <b>{{new Date(lastCacheRefresh)}}</b>.
            <br>
            Cache status <b>{{status}}</b> (<a @click="refreshPage" style="cursor: pointer;">Refresh</a>)
            <br><br><br>
            <a @click="refreshCache" style="cursor: pointer;">Reload now Bookie state and Ledger state</a>
            <br><br><br>
            <span>Current BookKeeper Client Configuration:</span>
            <textarea v-model='bookkeeperConfiguration' style='width: 100%; min-height: 200px;'></textarea>
        </div>
        <Spinner v-else/>
    </div>
</template>
<script>
export default {
    data: function() {
        return {
            pageLoaded: false,
            lastCacheRefresh: 0,
            status: "unknown",
            bookkeeperConfiguration: ""
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
            },
            error => {
                this.$router.push({
                    name: "error"
                });
            }        
        );
        }
    },
    created: function() {
        this.$request.get(
            "api/cache/info",
            cacheInfo => {
                this.pageLoaded = true;
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
                this.status = cacheInfo.status;
                this.bookkeeperConfiguration = cacheInfo.bookkeeperConfiguration;
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

