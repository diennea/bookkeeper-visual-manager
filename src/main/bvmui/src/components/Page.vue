<template>
    <main class="bvm-page-container">
        <Title v-if="pageTitle" :title="pageTitle"/>
        <transition>
            <router-view class="bvm-view"/>
        </transition>
    </main>
</template>
<script>
export default {
    data: function() {
        return {
            pageTitle: this.$route.meta.title
        };
    },
    watch: {
        $route: function(to, from) {
            this.pageTitle = to.meta.title;
        }
    }
};
</script>

