<template>
    <div class="bvm-bookie">
        <div v-if="pageLoaded">
            <p>
                BookKeeper Visual Manager caches data on a local database in order to save resources on the Metadata Service (ZooKeeper).
            </p>
            <p>
                Last cache refresh was at <b>{{new Date(lastCacheRefresh)}}</b>.
            </p>
            <p>
                Cache status <b>{{status}}</b> (<a @click="refreshPage" style="cursor: pointer;">Refresh</a>)
            </p>
             <v-btn
                depressed
                large
                tile
                color="blue lighten-1 white--text"
                @click="refreshCache">
                Refresh now
            </v-btn>
            <div>
                <span>Current BookKeeper Client Configuration:</span>
                <textarea v-model='bookkeeperConfiguration' style='width: 100%; min-height: 200px;'></textarea>
            </div>
        </div>
        <Spinner v-else/>
    </div>
</template>
<script>
export default {
    data() {
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
    created() {
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

