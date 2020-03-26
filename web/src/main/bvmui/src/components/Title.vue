<template>
    <h1 class="bvm-title display-1 mb-3">{{ computedTitle }}</h1>
</template>
<script>
export default {
    props: {
        title: String,
        params: Object
    },
    computed: {
        computedTitle() {
            return this.$library.replacePlaceholders(this.title, this.params);
        }
    }
};
</script>
