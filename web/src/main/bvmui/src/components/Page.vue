<template>
    <v-content>
        <v-container>
            <Title v-if="pageTitle" :title="pageTitle" :params="this.$route.params" />
            <router-view class="bvm-view" :key="$route.path" />
        </v-container>
    </v-content>
</template>
<script>
import Title from "./Title";
export default {
    components: {
        Title
    },
    data() {
        return {
            pageTitle: this.$route.meta.title,
            pageParams: this.$route.params
        };
    },
    watch: {
        $route: function (to) {
            this.pageTitle = to.meta.title;
            this.pageParams = to.params;
        }
    }
};
</script>
