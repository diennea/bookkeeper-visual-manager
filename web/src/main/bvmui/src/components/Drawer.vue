<template>
    <v-navigation-drawer
        v-model="$store.state.drawerExpanded"
        v-if="$store.state.showDrawer"
        app clipped color="grey lighten-4">
        <v-list dense class="grey lighten-4">
            <template v-for="(item, i) in items">
                <v-list-item :key="i" :to="item.path">
                    <v-list-item-action>
                        <v-icon>{{ item.icon }}</v-icon>
                    </v-list-item-action>
                    <v-list-item-content>
                        <v-list-item-title class="grey--text">{{ item.text }}</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </template>
            <v-list-item @click="performLogout">
                <v-list-item-action>
                    <v-icon>mdi-logout</v-icon>
                </v-list-item-action>
                <v-list-item-content>
                    <v-list-item-title class="grey--text">Logout</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
        </v-list>
    </v-navigation-drawer>
</template>
<script>
export default {
    data() {
        return {
            drawer: true,
            items: [
                { icon: "mdi-server", text: "Bookies", path: "/bookies" },
                { icon: "mdi-memory", text: "Ledgers", path: "/ledgers" },
                { icon: "mdi-cached", text: "System", path: "/systemstatus" },
                { icon: "mdi-lan-connect", text: "Clusters", path: "/clusters" },
            ]
        };
    },
    methods: {
        performLogout() {
            this.$store.dispatch("logout").finally(() => {
                this.$router.push("/login");
            });
        }
    }
};
</script>
