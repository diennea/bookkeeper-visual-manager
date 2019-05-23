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
            let string = this.title;
            let placeholders = this.params;
            for (var placeholder in placeholders) {
                string = string.replace(
                    new RegExp("\\${" + placeholder + "}", "g"),
                    placeholders[placeholder]
                );
            }
            return string;
        }
    }
};
</script>

