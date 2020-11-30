<template>
    <v-card width="400" class="mx-auto mt-5">
        <v-card-title primary-title>
            <h4>Login</h4>
        </v-card-title>
        <v-card-text>
            <v-form>
                <v-text-field
                    v-model="username"
                    :error="hasError"
                    label="Username"
                    color="primary white--text"
                    prepend-icon="mdi-account-circle"
                />
                <v-text-field
                    v-model="password"
                    :error="hasError"
                    :type="showPassword ? 'text' : 'password'"
                    label="Password"
                    color="primary white--text"
                    prepend-icon="mdi-lock"
                    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    @click:append="showPassword = !showPassword"
                />
            </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions>
            <v-btn color="primary white--text" @click="performLogin">Login</v-btn>
        </v-card-actions>
    </v-card>
</template>
<script>
export default {
    data() {
        return {
            username: "admin",
            password: "admin",
            showPassword: false,
            hasError: false
        };
    },
    methods: {
        performLogin() {
            const {username, password} = this;
            this.$store.dispatch("login", { username, password })
                .then(response => {
                    if (!response.ok) {
                        this.hasError = true;
                    } else {
                        this.$router.push("/");
                    }
                })
                .catch(() => {
                    this.hasError = true;
                });
        }
    }
};
</script>
