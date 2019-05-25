<template>
    <div class="bvm-tile" @click="$emit('click', $event)">
        <span>Ledger {{id}}</span>
    </div>
</template>
<script>
export default {
    props: {
        id: Number
    }
};
</script>


