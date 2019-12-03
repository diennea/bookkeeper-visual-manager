<template>
    <div class="bvm-tile-container">
        <Tile
            @click="$emit('item-clicked', item.id)"
            v-for="item in items"
            :item="item"
            :key="item.id"
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

