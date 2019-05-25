<template>
    <div class="bvm-title">
        <h1>{{computedTitle}}</h1>
    </div>
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

