<template>
    <v-navigation-drawer v-model="$store.state.drawerExpanded" app clipped color="grey lighten-4">
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
        </v-list>        
        <span @click="logout()">Logout</span>    
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
                { icon: "mdi-cached", text: "System", path: "/cache" }
            ]
        };
    }, methods: {        
        logout() {
            let url = "api/auth/logout";
            this.$request.get(url,
            res => {
                this.$store.commit('loggedOut');
                this.$router.push({name: "login"});
            }, error => {                
              this.$store.commit('loggedOut');
              this.$router.push({
                name: "login"
              });
              }
            );
        }
    }
};
</script>

<style>
</style>