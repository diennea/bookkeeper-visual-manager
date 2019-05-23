<template>
    <div class="bvm-tile-container">
        <Tile
            @click="$emit('item-clicked', item)"
            v-for="(item, index) in items"
            :id="item.id"
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

