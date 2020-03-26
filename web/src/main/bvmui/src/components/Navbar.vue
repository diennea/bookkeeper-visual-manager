<template>
    <v-app-bar app clipped-left color="blue lighten-1">
        <v-app-bar-nav-icon class="white--text" @click="toggleDrawer" />
        <span class="title ml-3 mr-5 white--text">
            Bookkeeper&nbsp;<span class="font-weight-light">Visual Manager</span>
        </span>
    </v-app-bar>
</template>
<script>
export default {
    methods: {
        toggleDrawer() {
            this.$store.commit('toggleDrawer');
        }
    }
};
</script>
