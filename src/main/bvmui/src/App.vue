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
import axios from "axios";
import Drawer from "@/components/Drawer";
import Navbar from "@/components/Navbar";
import Page from "@/components/Page";

export default {
    components: {
        Drawer,
        Navbar,
        Page
    },
    created() {
        axios.interceptors.response.use(
            response => {
                return response;
            },
            function(err) {
                return new Promise((resolve, reject) => {
                    if (
                        err.status === 401 &&
                        err.config &&
                        !err.config.__isRetryRequest
                    ) {
                        this.$store.dispatch(logout);
                    } else if (err.status === 500) {
                        this.$router.replace({ name: "error" });
                    }
                    throw err;
                });
            }
        );
    }
};
</script>
