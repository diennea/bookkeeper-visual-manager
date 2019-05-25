<template>
    <main class="bvm-page-container">
        <Title v-if="pageTitle" :title="pageTitle" :params="this.$route.params"/>
        <router-view :key="$route.path" class="bvm-view"/>
    </main>
</template>
<script>
export default {
    data: function() {
        return {
            pageTitle: this.$route.meta.title,
            pageParams: this.$route.params
        };
    },
    watch: {
        $route: function(to, from) {
            this.pageTitle = to.meta.title;
            this.pageParams = to.params;
        }
    }
};
</script>

