<template>
    <v-app-bar app clipped-left color="primary">
        <v-app-bar-nav-icon v-show="$store.state.showDrawer" class="white--text" @click="toggleDrawer" />
        <span class="title ml-3 mr-5 white--text">
            Bookkeeper
            &nbsp;<span class="font-weight-light">Visual Manager</span>
            &nbsp;<span v-if="pageTitle" class="title">- {{ pageTitle }}</span>
        </span>
    </v-app-bar>
</template>
<script>
export default {
    props: {
        pageTitle: String
    },
    methods: {
        toggleDrawer() {
            this.$store.commit('toggleDrawer');
        },
        showDrawer() {
            this.$store.getters.showDrawer;
        }
    }
};
</script>
