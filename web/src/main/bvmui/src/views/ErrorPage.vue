<template>
    <div class="bvm-error">
        <h1>404</h1>
        <h2>Not Found</h2>
    </div>
</template>
<script>
export default {
    props: {
        errorCode: {
            type: Number,
            default: 404
        },
        errorMessage: {
            type: String,
            default: "Not Found"
        }
    }
}
</script>


