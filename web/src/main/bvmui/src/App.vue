<template>
    <v-app id="bkvm" v-if="$store.getters.isLogged">
        <Drawer />
        <Navbar />
        <Page />
    </v-app>
    <v-app v-else>
        <router-view key="login" class="bvm-view" />
    </v-app>
</template>
<script>
import Drawer from "@/components/Drawer";
import Navbar from "@/components/Navbar";
import Page from "@/components/Page";
export default {
    components: {
        Drawer,
        Navbar,
        Page
    }
};
</script>
