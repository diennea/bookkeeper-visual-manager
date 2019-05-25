<template>
    <div class="bvm-card-container">
        <Card
            @click="$emit('item-clicked', item)"
            v-for="(item, index) in items"
            :data="item"
            :key="index"
        />
    </div>
</template>
<script>
export default {
    props: {
        items: Array
    }
};
</script>

