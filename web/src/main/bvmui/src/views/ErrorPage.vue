<template>
    <div class="bvm-error">
        <h1>{{ code }}</h1>
        <h2>{{ message }}</h2>
    </div>
</template>
<script>
const ErrorMessages = Object.freeze({
    404: "Not found",
    500: "Internal Error",
    403: "Operation denied"
});

export default {
    data() {
        return {
            code: 404
        };
    },
    computed: {
        message() {
            return ErrorMessages[this.code] || "Error";
        }
    },
    created() {
        this.code = this.$route.params.code || 404;
    }
};
</script>
