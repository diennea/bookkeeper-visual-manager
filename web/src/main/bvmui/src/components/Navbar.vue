<template>
  <v-app-bar app clipped-left color="primary">
    <v-app-bar-nav-icon v-show="$store.state.showDrawer" class="white--text" @click="toggleDrawer" />
    <span class="title ml-3 mr-5 white--text">
            Bookkeeper
            &nbsp;<span class="font-weight-light">Visual Manager</span>
            &nbsp;<span v-if="pageTitle" class="title">- {{ pageTitle }}</span>
        </span>

    <v-spacer />
    <v-btn small v-show="lastCacheRefreshStr" @click="refreshCache" :disabled="status !== 'IDLE'" color="primary">
      Last refresh: {{ lastCacheRefreshStr }}
      <v-icon right>
        mdi-cached
      </v-icon>
    </v-btn>
  </v-app-bar>
</template>
<script>
export default {
    props: {
        pageTitle: String
    },
    data() {
        return {
            status: "unknown",
            lastCacheRefreshStr: '',
        }
    },
    created() {
        this.updateInfo();
    },
    beforeDestroy() {
        if (this.interval) {
            clearInterval(this.interval);
        }
    },
    methods: {
        toggleDrawer() {
            this.$store.commit('toggleDrawer');
        },
        refreshCache() {
            this.$request.get("api/cache/refresh").then(
                cacheInfo => {
                    this.status = cacheInfo.status;
                    this.lastCacheRefreshStr = this.$library.formatTimeDiff(Date.now() - cacheInfo.lastCacheRefresh);
                }
            );
        },
        updateInfo() {
            this.$request.get("api/cache/info").then(
                cacheInfo => {
                    this.status = cacheInfo.status;
                    this.lastCacheRefreshStr = this.$library.formatTimeDiff(Date.now() - cacheInfo.lastCacheRefresh);
                    if (!this.interval) {
                        this.interval = setInterval(this.updateInfo, 900);
                    }
                }
            );
        }
    }
};
</script>
