<template>
    <main class="bvm-page-container">
        <Title v-if="pageTitle" :title="pageTitle" :params="this.$route.params"/>
        <transition>
            <router-view class="bvm-view"/>
        </transition>
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

