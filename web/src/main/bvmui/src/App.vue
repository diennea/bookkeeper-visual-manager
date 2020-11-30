<template>
    <v-app id="bkvm" v-if="$store.getters.isLogged">
        <Drawer />
        <Navbar :page-title="pageTitle" />
        <v-main>
            <v-container>
                <router-view class="bvm-view" :key="$route.path" />
            </v-container>
        </v-main>
    </v-app>
    <v-app v-else>
        <router-view key="login" class="bvm-view" />
    </v-app>
</template>
<script>
import Drawer from "@/components/Drawer";
import Navbar from "@/components/Navbar";
export default {
    components: {
        Drawer,
        Navbar
    },
    data() {
        return {
            title: this.$route.meta.title,
            params: this.$route.params,
        };
    },
    computed: {
        pageTitle() {
            let currentTitle = this.$library.replacePlaceholders(this.title, this.params);
            currentTitle = currentTitle ? `BKVM | ${currentTitle}` : "BKVM";
            document.title = currentTitle;
            return currentTitle;
        }
    },
    watch: {
        $route: function (targetRoute) {
            this.title = targetRoute.meta.title;
            this.params = targetRoute.params;
        },
    },
};
</script>
