<template>
    <v-form>
            <v-text-field
                v-model="username"
                label="Username">
            </v-text-field>
            <v-text-field
                v-model="password"
                label="Password"
                tile
                flat
                hide-details>
            </v-text-field>
            <v-btn
                depressed
                large
                tile
                class="mt-1"
                color="blue lighten-1 white--text"
                @click="performLogin">
                Login
            </v-btn>
        </v-form>
</template>
<script>
export default {
    data: function() {
        return {
            username: 'admin',
            password: 'admin'
        };
    },
    methods: {        
       performLogin: function() {
        let url = "api/auth/login?username="+this.username+"&password="+this.password;
        this.$request.get(url,
            res => {
                if (res.ok) {
                   this.$store.commit('loggedIn');
                   this.$router.push({
                                    name: "bookies"
                                });                                                   
                } else {
                    this.password = "";
                    alert('bad username or password');
                }
            },
            error => {
                this.$router.push({
                                    name: "bookies"
                                });
                            }
                    );
                }
    }
};
</script>

