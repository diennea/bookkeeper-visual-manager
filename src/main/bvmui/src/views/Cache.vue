<template>
    <div class="bvm-bookie">
        <div v-if="pageLoaded">
            Cache refresh at {{new Date(lastCacheRefresh)}}
            <br>
            <a @click="refreshCache" >Refresh now !</a>
        </div>
        <Spinner v-else/>
    </div>
</template>
<script>
export default {
    data: function() {
        return {
            pageLoaded: false,
            lastCacheRefresh: 0
        };
    },
    methods: {
        refreshCache() {
            this.$request.get(
            "api/cache/refresh",
            cacheInfo => {
                this.pageLoaded = true;
                this.lastCacheRefresh = cacheInfo.lastCacheRefresh;
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

